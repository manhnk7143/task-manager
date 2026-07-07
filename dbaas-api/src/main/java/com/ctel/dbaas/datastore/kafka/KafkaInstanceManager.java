package com.ctel.dbaas.datastore.kafka;

import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.common.enums.ComputeStatus;
import com.ctel.dbaas.common.enums.DatastoreMode;
import com.ctel.dbaas.common.enums.Role;
import com.ctel.dbaas.config.RabbitMQConfig;
import com.ctel.dbaas.datastore.BuildReqTaskMng;
import com.ctel.dbaas.datastore.BuildRequestTaskManager;
import com.ctel.dbaas.datastore.DatastoreInstanceAbstract;
import com.ctel.dbaas.datastore.kafka.model.KafkaCluster;
import com.ctel.dbaas.datastore.kafka.model.KafkaSingleNode;
import com.ctel.dbaas.dto.cmc_cloud.SubnetInfo;
import com.ctel.dbaas.dto.cmc_cloud.VpcInfo;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.common.TaskManagerRequest;
import com.ctel.dbaas.dto.compute.CreateComputeInfo;
import com.ctel.dbaas.dto.compute.FlavorInfo;
import com.ctel.dbaas.dto.instance.InstanceCreateReq;
import com.ctel.dbaas.dto.instance.InstanceDetail;
import com.ctel.dbaas.dto.instance.InstanceInfo;
import com.ctel.dbaas.dto.instance.ResourceCompute;
import com.ctel.dbaas.entity.dbaas.*;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.*;
import com.ctel.dbaas.service.AgentFirmwareService;
import com.ctel.dbaas.service.CmcCloudService;
import com.ctel.dbaas.utils.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class KafkaInstanceManager implements DatastoreInstanceAbstract {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ComputeRepository computeRepository;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private AgentFirmwareService agentFirmwareService;
    @Autowired
    private VolumeRepository volumeRepository;
    @Autowired
    private NetworkRepository networkRepository;
    @Autowired
    private BackupStrategyRepository backupStrategyRepository;
    @Autowired
    private BackupRepository backupRepository;
    @Autowired
    private CmcCloudService cmcCloudService;

    @SneakyThrows
    @Override
    public void createInstance(InstanceCreateReq request, RequestInfo requestCtx) {
        InstanceCreateReq.DtValid req = request.getDtValid();
        DatastoreMode.Kafka kafkaMode = DatastoreMode.Kafka.get(req.getDatastoreMode());
        TaskManagerRequest taskManagerRequest = null;
        if (DatastoreMode.Kafka.SINGLE_NODE.equals(kafkaMode)) {
            KafkaSingleNode singleNode = objectMapper.readValue(request.getRequestMetadata(), KafkaSingleNode.class);
            taskManagerRequest = this.singleNodeCreate(req, singleNode, requestCtx);
        } else if (DatastoreMode.Kafka.CLUSTER.equals(kafkaMode)) {
            KafkaCluster cluster = objectMapper.readValue(request.getRequestMetadata(), KafkaCluster.class);
            taskManagerRequest = this.clusterCreate(req, cluster, requestCtx);
        }

        if (taskManagerRequest == null) {
            throw new AppException(new ErrorResponse("Unknown"));
        }

        rabbitTemplate.convertAndSend(RabbitMQConfig.DBAAS_TASK_MANAGER, RabbitMQConfig.ROUTING_KEY_TASK_MANAGER,
                taskManagerRequest.toMap());
    }

    @SneakyThrows
    private TaskManagerRequest singleNodeCreate(InstanceCreateReq.DtValid req, KafkaSingleNode instanceInfo, RequestInfo requestCtx) {
        AgentFirmwareEntity agentFirmware = this.getAgentFirmwareLatest();
        CreateComputeInfo standalone = this.createComputeRecord(req.getInstanceId(), req.getFlavorId(),
                Role.Kafka.BROKER.getName(), agentFirmware.getId(), agentFirmware.getBuildNumber(), instanceInfo.getZone());

        JSONObject instanceRequest = new JSONObject();
        instanceRequest.put("CheckPrerequisitesSingleNode", BuildReqTaskMng.checkPrerequisites(req.getInstanceId(),
                standalone.getComputeId(), req.getFlavorId(), req.getGlanceImageTag(), req.getNetworkId(), req.getSubnetId(),
                req.getDatastoreCode()));

        // create network cluster
        JSONObject networkSingleNode = BuildRequestTaskManager.createNetwork(req.getNetworkId(),
                req.getSubnetId(), req.getDatastoreCode(), req.getSecurityGroupIds());
        JSONArray portsKafka = new JSONArray();
        portsKafka.put(9093);
        portsKafka.put(9094);
        portsKafka.put(9095);
        networkSingleNode.put("portsKafka", portsKafka);
        instanceRequest.put("CreateNetworkSingleNode", networkSingleNode);

        // create volume
        JSONObject volumeCluster = BuildRequestTaskManager.createVolume(req.getVolumeSize());
        instanceRequest.put("CreateVolumeSingleNode", volumeCluster);

        // generate cloud-init
        JSONObject cloudInitSingleNode = BuildRequestTaskManager.generateCloudInit(
                req.getDatastoreCode(), req.getDatastoreVersion(), req.getDatastoreMode(), null, null);
        instanceRequest.put("GenerateCloudInitSingleNode", cloudInitSingleNode);

        // create server
        instanceRequest.put("CreateComputeSingleNode", BuildReqTaskMng.createCompute(req.getInstanceId(),
                standalone.getComputeId(), instanceInfo.getZone(), req.getDatastoreCode()));

        TaskManagerRequest taskManagerReq = new TaskManagerRequest();
        taskManagerReq.setServiceId("create_kafka_single_node");
        taskManagerReq.setData(instanceRequest.toString());
        log.info("json kafka single node send to task-manager => {}", CommonUtils.toJson(taskManagerReq.toMap()));

        return taskManagerReq;
    }

    @SneakyThrows
    private TaskManagerRequest clusterCreate(InstanceCreateReq.DtValid req, KafkaCluster clusterInfo, RequestInfo requestCtx) {
        AgentFirmwareEntity agentFirmware = this.getAgentFirmwareLatest();
        JSONObject clusterRequest = new JSONObject();

        JSONArray brokerComputeIds = new JSONArray();
        List<String> zones = new ArrayList<>(clusterInfo.getZones());
        int zoneSize = zones.size();
        int quantityBrokers = zoneSize * clusterInfo.getBrokersPerZone();
        for (int i = 0; i < quantityBrokers; i++) {
            CreateComputeInfo brokerServer = this.createComputeRecord(req.getInstanceId(), req.getFlavorId(),
                    Role.Kafka.BROKER.getName(), agentFirmware.getId(), agentFirmware.getBuildNumber(),
                    zones.get(i % zoneSize));
            brokerComputeIds.put(brokerServer.getComputeId());
        }

        // check prerequisites cluster
        JSONObject checkPrerequisites = BuildRequestTaskManager.checkPrerequisites(req.getInstanceId(),
                req.getFlavorId(), req.getGlanceImageTag(), req.getNetworkId(), req.getSubnetId(), req.getDatastoreCode());
        checkPrerequisites.put("brokerComputeIds", brokerComputeIds);
        clusterRequest.put("CheckPrerequisitesCluster", checkPrerequisites);

        // create network cluster
        JSONObject networkCluster = BuildRequestTaskManager.createNetwork(req.getNetworkId(),
                req.getSubnetId(), req.getDatastoreCode(), req.getSecurityGroupIds());
        JSONArray portsKafka = new JSONArray();
        portsKafka.put(9093);
        portsKafka.put(9094);
        portsKafka.put(9095);
        networkCluster.put("portsKafka", portsKafka);
        clusterRequest.put("CreateNetworkCluster", networkCluster);

        // create volume cluster
        JSONObject volumeCluster = BuildRequestTaskManager.createVolume(req.getVolumeSize());
        clusterRequest.put("CreateVolumeCluster", volumeCluster);

        // generate cloud-init cluster
        JSONObject cloudInitMasterCluster = BuildRequestTaskManager.generateCloudInit(
                req.getDatastoreCode(), req.getDatastoreVersion(), req.getDatastoreMode(), null, null);
        clusterRequest.put("GenerateCloudInitCluster", cloudInitMasterCluster);

        // create compute cluster
        JSONObject computeCluster = BuildRequestTaskManager.createCompute(req.getDatastoreCode());
        clusterRequest.put("CreateComputeCluster", computeCluster);

        TaskManagerRequest taskManagerReq = new TaskManagerRequest();
        taskManagerReq.setServiceId("create_kafka_cluster");
        taskManagerReq.setData(clusterRequest.toString());
        log.info("json kafka cluster send to task-manager => {}", CommonUtils.toJson(taskManagerReq.toMap()));

        return taskManagerReq;
    }

    private CreateComputeInfo createComputeRecord(String instanceId, String flavorId, String role, String agentFirmwareId,
                                                  Integer buildNumber, String zoneName) {
        ComputeEntity compute = new ComputeEntity();
        compute.setFlavorId(flavorId);
        compute.setRole(role);
        compute.setStatus(ComputeStatus.BUILDING.getName());
        compute.setInstanceId(instanceId);
        compute.setProjectId(GrpcCtx.getReqCtx().getProjectId());
        compute.setOrgId(GrpcCtx.getReqCtx().getOrgId());
        compute.setRegionId(GrpcCtx.getReqCtx().getRegionId());
        compute.setZoneName(zoneName);
        computeRepository.save(compute);

        AgentEntity agent = new AgentEntity();
        agent.setName("agent-compute-" + compute.getId());
        agent.setEncryptedKey(CommonUtils.generateString(32));
        agent.setAgentVersion(String.valueOf(buildNumber == null ? 0 : buildNumber));
        agent.setAgentFirmwareId(agentFirmwareId);
        agent.setInstanceId(instanceId);
        agent.setComputeId(compute.getId());
        agent.setProjectId(GrpcCtx.getReqCtx().getProjectId());
        agent.setOrgId(GrpcCtx.getReqCtx().getOrgId());
        agent.setStatus("NONE");
        agentRepository.save(agent);

        return new CreateComputeInfo(compute.getId(), agent.getId(), agent.getEncryptedKey());
    }

    @Override
    public InstanceDetail getInstanceDetail(InstanceInfo instanceInfo, RequestInfo requestCtx) {
        InstanceEntity instance = instanceInfo.getInstance();
        List<ComputeEntity> lstCompute = computeRepository.findAllByInstanceId(instanceInfo.getInstance().getId());
        if (lstCompute.isEmpty()) {
            throw new AppException(new ErrorResponse("Not found compute in this instance"));
        }

        InstanceDetail instanceDetail = new InstanceDetail();
        instanceDetail.setId(instance.getId());
        instanceDetail.setInstanceName(instance.getName());
        instanceDetail.setDatastoreName(instanceInfo.getDatastoreName());
        instanceDetail.setDatastoreVersion(instanceInfo.getDatastoreVersion());
        instanceDetail.setDatastoreMode(instanceInfo.getDatastoreModeName());
        instanceDetail.setGroupConfigId(instance.getGroupConfigurationId() == null ? "" : instance.getGroupConfigurationId());
        instanceDetail.setStatus(instance.getStatus().toLowerCase());
        instanceDetail.setCreated(instance.getCreatedAt().toString());
        instanceDetail.setUpdated(instance.getUpdatedAt().toString());
        instanceDetail.setSecurityClientIds(instance.getNeutronSecurityGroupClientIds());

        if (instance.getSubnetId() != null) {
            SubnetInfo subnetInfo = cmcCloudService.getSubnet(instance.getSubnetId(), requestCtx);
            if (subnetInfo != null) {
                instanceDetail.setSubnetId(instance.getSubnetId());
                instanceDetail.setSubnetName(subnetInfo.getName());
                VpcInfo vpcInfo = cmcCloudService.getVpc(subnetInfo.getVpcId(), requestCtx);
                if (vpcInfo != null) {
                    instanceDetail.setVpcId(vpcInfo.getId());
                    instanceDetail.setVpcName(vpcInfo.getName());
                }
            }
        }

        if (instance.getFlavorId() != null) {
            FlavorInfo flavorInfo = cmcCloudService.getFlavor(instance.getFlavorId(), requestCtx.getToken(), requestCtx.getRegionId());
            if (flavorInfo != null) {
                instanceDetail.setFlavorId(instance.getFlavorId());
                instanceDetail.setFlavorName(flavorInfo.getFlavorName());
                instanceDetail.setVCpu(flavorInfo.getVCpus());
                instanceDetail.setRam(flavorInfo.getRam());
                instanceDetail.setDisk(flavorInfo.getDisk());
            }
        }
        instanceDetail.setVolumeSize(instance.getVolumeSize());

        List<InstanceDetail.ComputeInstanceInfo> serverInfo = new ArrayList<>();
        for (ComputeEntity server : lstCompute) {
            serverInfo.add(this.setResource(server, requestCtx.getToken()));
        }
        instanceDetail.setDataDetail(CommonUtils.toJson(new InstanceDetail.Kafka(serverInfo)));

        return instanceDetail;
    }

    private InstanceDetail.ComputeInstanceInfo setResource(ComputeEntity compute, String token) {
        InstanceDetail.ComputeInstanceInfo masterDetail = new InstanceDetail.ComputeInstanceInfo();
        masterDetail.setId(compute.getId());
        masterDetail.setRole(compute.getRole());
        masterDetail.setMonitorResourceId(compute.getMonitorResourceId() != null ? compute.getMonitorResourceId() : "");

        ResourceCompute resourceServer = this.getResourceOfCompute(compute, token);
        masterDetail.setIpAddress(resourceServer.getIpAddress());
        masterDetail.setVCpus(resourceServer.getVCpus());
        masterDetail.setRam(resourceServer.getRam());
        masterDetail.setDisk(resourceServer.getDisk());
        masterDetail.setVolumeSize(resourceServer.getVolumeSize());
        masterDetail.setStatus(compute.getStatus().toUpperCase());
        masterDetail.setOsServerId(compute.getNovaInstanceId());
        masterDetail.setZoneName(compute.getZoneName());

        return masterDetail;
    }

    private ResourceCompute getResourceOfCompute(ComputeEntity compute, String token) {
        ResourceCompute res = new ResourceCompute();

        FlavorInfo flavorInfo = cmcCloudService.getFlavor(compute.getFlavorId(), token, compute.getRegionId());
        res.setDisk(flavorInfo.getDisk());
        res.setVCpus(flavorInfo.getVCpus());
        res.setRam(flavorInfo.getRam());

        VolumeEntity volumeEntity = volumeRepository.findFirstByComputeId(compute.getId());
        if (volumeEntity != null) {
            res.setVolumeSize(res.getVolumeSize() + volumeEntity.getSize());
        }

        NetworkEntity networkUser = networkRepository.findFirstByComputeIdAndMode(compute.getId(), "user");
        if (networkUser != null) {
            res.setIpAddress(networkUser.getIpAddress());
        }

        return res;
    }

    private AgentFirmwareEntity getAgentFirmwareLatest() {
        AgentFirmwareEntity agentFirmware = agentFirmwareService.getAgentFirmwareLatest();
        if (agentFirmware == null) {
            throw new AppException(new ErrorResponse("Not found agent firmware"));
        }

        return agentFirmware;
    }
}
