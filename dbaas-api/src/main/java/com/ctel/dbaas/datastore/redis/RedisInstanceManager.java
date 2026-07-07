package com.ctel.dbaas.datastore.redis;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.common.enums.ComputeStatus;
import com.ctel.dbaas.common.enums.DatastoreMode;
import com.ctel.dbaas.common.enums.Role;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.config.RabbitMQConfig;
import com.ctel.dbaas.datastore.BuildReqTaskMng;
import com.ctel.dbaas.datastore.BuildRequestTaskManager;
import com.ctel.dbaas.datastore.DatastoreInstanceAbstract;
import com.ctel.dbaas.datastore.redis.model.RedisCluster;
import com.ctel.dbaas.datastore.redis.model.RedisMasterSlave;
import com.ctel.dbaas.datastore.redis.model.RedisStandalone;
import com.ctel.dbaas.dto.backup.S3StorageConfigDto;
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
import com.ctel.dbaas.utils.CryptoUtils;
import com.ctel.dbaas.utils.S3Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Log4j2
public class RedisInstanceManager implements DatastoreInstanceAbstract {
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
        DatastoreMode.Redis redisMode = DatastoreMode.Redis.get(req.getDatastoreMode());
        TaskManagerRequest taskManagerRequest = null;
        if (DatastoreMode.Redis.MASTER_SLAVE.equals(redisMode)) {
            RedisMasterSlave redisMasterSlave = objectMapper
                    .readValue(request.getRequestMetadata(), RedisMasterSlave.class);
            redisMasterSlave.setNumOfSlaves(2);

            taskManagerRequest = this.masterSlaveCreate(req, redisMasterSlave, requestCtx);
        } else if (DatastoreMode.Redis.STANDALONE.equals(redisMode)) {
            RedisStandalone redisStandalone = objectMapper
                    .readValue(request.getRequestMetadata(), RedisStandalone.class);

            taskManagerRequest = this.standaloneCreate(req, redisStandalone, requestCtx);
        } else if (DatastoreMode.Redis.CLUSTER.equals(redisMode)) {
            RedisCluster redisCluster = objectMapper
                    .readValue(request.getRequestMetadata(), RedisCluster.class);
            redisCluster.setNumOfMasterServer(3);

            taskManagerRequest = this.clusterCreate(req, redisCluster, requestCtx);
        }

        if (taskManagerRequest == null) {
            throw new AppException(new ErrorResponse("Unknown"));
        }

        rabbitTemplate.convertAndSend(RabbitMQConfig.DBAAS_TASK_MANAGER, RabbitMQConfig.ROUTING_KEY_TASK_MANAGER,
                taskManagerRequest.toMap());
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
        instanceDetail.setGroupConfigId(instance.getGroupConfigurationId());
        instanceDetail.setStatus(instance.getStatus().toUpperCase());
        instanceDetail.setCreated(instance.getCreatedAt().toString());
        instanceDetail.setUpdated(instance.getUpdatedAt().toString());
        instanceDetail.setSecurityClientIds(instance.getNeutronSecurityGroupClientIds());

        //
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
        //

        DatastoreMode.Redis mode = DatastoreMode.Redis.get(instanceInfo.getDatastoreModeCode());
        switch (mode) {
            case STANDALONE -> {
                ComputeEntity masterCompute = lstCompute.get(0);
                instanceDetail.setDataDetail(CommonUtils.toJson(new InstanceDetail.RedisStandalone(this.setResource(masterCompute, requestCtx.getToken()))));
            }
            case MASTER_SLAVE -> {
                List<InstanceDetail.ComputeInstanceInfo> slavesDetail = new ArrayList<>();
                ComputeEntity masterCompute = lstCompute.stream().filter(c -> Role.Redis.MASTER.getName().equals(c.getRole())).findFirst().orElse(new ComputeEntity());
                List<ComputeEntity> lstSlaveCompute = lstCompute.stream().filter(c -> Role.Redis.SLAVE.getName().equals(c.getRole())).toList();
                for (ComputeEntity slaveCompute : lstSlaveCompute) {
                    slavesDetail.add(this.setResource(slaveCompute, requestCtx.getToken()));
                }
                instanceDetail.setDataDetail(CommonUtils.toJson(new InstanceDetail.RedisMasterSlave(this.setResource(masterCompute, requestCtx.getToken()), slavesDetail)));
            }
            case CLUSTER -> {
                List<InstanceDetail.ComputeInstanceInfo> mastersInfo = lstCompute.stream()
                        .filter(c -> Role.Redis.MASTER.getName().equals(c.getRole())).map(c -> this.setResource(c, requestCtx.getToken())).toList();
                List<InstanceDetail.ComputeInstanceInfo> slavesInfo = lstCompute.stream()
                        .filter(c -> Role.Redis.SLAVE.getName().equals(c.getRole())).map(c -> this.setResource(c, requestCtx.getToken())).toList();
                InstanceDetail.RedisCluster redisCluster = new InstanceDetail.RedisCluster(mastersInfo, slavesInfo);
                instanceDetail.setDataDetail(CommonUtils.toJson(redisCluster));
            }
        }

        return instanceDetail;
    }

    @SneakyThrows
    private TaskManagerRequest clusterCreate(InstanceCreateReq.DtValid req, RedisCluster clusterInfo, RequestInfo requestCtx) {
        AgentFirmwareEntity agentFirmware = this.getAgentFirmwareLatest();
        String urlBackup = this.generateUrlDownloadBackup(req.getBackupId(), requestCtx.getOrgId());
        String redisPassword = clusterInfo.getPassword();
        JSONObject clusterRequest = new JSONObject();

        List<String> zones = new ArrayList<>(clusterInfo.getZones());
        int zoneSize = zones.size();

        // create master and slave servers
        JSONArray masterComputeIds = new JSONArray();
        for (int i = 0; i < clusterInfo.getNumOfMasterServer(); i++) {
            CreateComputeInfo masterServer = this.createComputeRecord(req.getInstanceId(), req.getFlavorId(),
                    Role.Redis.MASTER.getName(), agentFirmware.getId(), agentFirmware.getBuildNumber(),
                    zones.get(i % zoneSize));
            masterComputeIds.put(masterServer.getComputeId());
        }

        JSONArray slaveComputeIds = new JSONArray();
        int numOfReplicaServer = clusterInfo.getNumOfMasterServer() * clusterInfo.getReplicas();
        for (int i = 0; i < numOfReplicaServer; i++) {
            CreateComputeInfo slaveServer = this.createComputeRecord(req.getInstanceId(), req.getFlavorId(),
                    Role.Redis.SLAVE.getName(), agentFirmware.getId(), agentFirmware.getBuildNumber(), zones.get(i % zoneSize));
            slaveComputeIds.put(slaveServer.getComputeId());
        }

        // check prerequisites cluster
        JSONObject checkPrerequisites = BuildRequestTaskManager.checkPrerequisites(req.getInstanceId(),
                req.getFlavorId(), req.getGlanceImageTag(), req.getNetworkId(), req.getSubnetId(), req.getDatastoreCode());
        checkPrerequisites.put("masterComputeIds", masterComputeIds);
        checkPrerequisites.put("slaveComputeIds", slaveComputeIds);

        // for test
        String regionId = EnvConfig.TEST_REGION_DEV;
        if ("dev".equals(regionId)) {
            checkPrerequisites.put("zoneMaster", "nova");
            checkPrerequisites.put("zoneSlave", "nova");
        }
        // end
        clusterRequest.put("CheckPrerequisitesCluster", checkPrerequisites);

        clusterRequest.put("LoadConfigGroupCluster", BuildRequestTaskManager.loadConfigGroup(req.getGroupConfigurationId()));

        // create network cluster
        JSONObject networkCluster = BuildRequestTaskManager.createNetwork(req.getNetworkId(),
                req.getSubnetId(), req.getDatastoreCode(), req.getSecurityGroupIds());
        networkCluster.put("busPortRedis", Constant.BUS_PORT_DEFAULT_REDIS);
        clusterRequest.put("CreateNetworkCluster", networkCluster);

        // create volume cluster
        JSONObject volumeCluster = BuildRequestTaskManager.createVolume(req.getVolumeSize());
        clusterRequest.put("CreateVolumeCluster", volumeCluster);

        // generate cloud-init cluster
        JSONObject cloudInitMasterCluster = BuildRequestTaskManager.generateCloudInit(
                req.getDatastoreCode(), req.getDatastoreVersion(), req.getDatastoreMode(), redisPassword, urlBackup);
        cloudInitMasterCluster.put("busPortRedis", Constant.BUS_PORT_DEFAULT_REDIS);
        cloudInitMasterCluster.put("replicas", clusterInfo.getReplicas());
        cloudInitMasterCluster.put("backupMode", req.getBackupMode());
        clusterRequest.put("GenerateCloudInitCluster", cloudInitMasterCluster);

        // create compute cluster
        JSONObject computeCluster = BuildRequestTaskManager.createCompute(req.getDatastoreCode());
        clusterRequest.put("CreateComputeCluster", computeCluster);

        TaskManagerRequest taskManagerReq = new TaskManagerRequest();
        taskManagerReq.setServiceId("create_redis_cluster");
        taskManagerReq.setData(clusterRequest.toString());
        log.info("json redis cluster send to task-manager => {}", CommonUtils.toJson(taskManagerReq.toMap()));

        return taskManagerReq;
    }

    @SneakyThrows
    private TaskManagerRequest masterSlaveCreate(InstanceCreateReq.DtValid req, RedisMasterSlave modeInfo, RequestInfo requestCtx) {
        AgentFirmwareEntity agentFirmware = this.getAgentFirmwareLatest();
        String urlBackup = this.generateUrlDownloadBackup(req.getBackupId(), requestCtx.getOrgId());

        List<String> zones = new ArrayList<>(modeInfo.getZones());
        int zoneSize = zones.size();
        String zoneMaster = zones.get(0);

        CreateComputeInfo masterCompute = this.createComputeRecord(req.getInstanceId(),
                req.getFlavorId(), Role.Redis.MASTER.getName(), agentFirmware.getId(),
                agentFirmware.getBuildNumber(), zoneMaster);

        JSONObject masterSlaveRequest = new JSONObject();
        masterSlaveRequest.put("CheckPrerequisitesMasterSlave", BuildReqTaskMng.checkPrerequisites(req.getInstanceId(),
                masterCompute.getComputeId(), req.getFlavorId(), req.getGlanceImageTag(), req.getNetworkId(), req.getSubnetId(),
                req.getDatastoreCode()));

        masterSlaveRequest.put("LoadConfigGroupMasterSlave",
                BuildReqTaskMng.loadConfigGroup(req.getInstanceId(), req.getGroupConfigurationId(), masterCompute.getComputeId()));

        masterSlaveRequest.put("CreateNetworkMasterCluster", BuildReqTaskMng.createNetwork(
                req.getInstanceId(), masterCompute.getComputeId(), req.getNetworkId(),
                req.getSubnetId(), null, req.getDatastoreCode(), req.getSecurityGroupIds()));

        masterSlaveRequest.put("CreateVolumeMasterCluster", BuildReqTaskMng.createVolume(
                req.getInstanceId(), masterCompute.getComputeId(), req.getVolumeSize(), zoneMaster));

        JSONObject cloudInitMaster = BuildReqTaskMng.generateCloudInit(
                req.getDatastoreCode(), req.getDatastoreVersion(), req.getDatastoreMode(), req.getInstanceId(),
                Role.Redis.MASTER.getName(), masterCompute.getAgentId(), masterCompute.getEncryptedKey(),
                modeInfo.getPassword(), masterCompute.getComputeId(), urlBackup);
        cloudInitMaster.put("backupMode", req.getBackupMode());
        masterSlaveRequest.put("GenerateCloudInitMasterCluster", cloudInitMaster);

        masterSlaveRequest.put("CreateComputeMasterCluster",
                BuildReqTaskMng.createCompute(req.getInstanceId(), masterCompute.getComputeId(), zoneMaster,
                        req.getDatastoreCode()));

        for (int i = 0; i < 2; i++) {
            CreateComputeInfo slaveCompute = this.createComputeRecord(req.getInstanceId(),
                    req.getFlavorId(), Role.Redis.SLAVE.getName(), agentFirmware.getId(), agentFirmware.getBuildNumber(),
                    zones.get(i % zoneSize));

            masterSlaveRequest.put("CreateNetworkSlave" + i + "Cluster", BuildReqTaskMng.createNetwork(
                    req.getInstanceId(), slaveCompute.getComputeId(), req.getNetworkId(),
                    req.getSubnetId(), null, req.getDatastoreCode(), req.getSecurityGroupIds()));

            masterSlaveRequest.put("CreateVolumeSlave" + i + "Cluster",
                    BuildReqTaskMng.createVolume(req.getInstanceId(), slaveCompute.getComputeId(), req.getVolumeSize(),
                            zones.get(i % zoneSize)));

            masterSlaveRequest.put("GenerateCloudInitSlave" + i + "Cluster", BuildReqTaskMng.generateCloudInit(
                    req.getDatastoreCode(), req.getDatastoreVersion(), req.getDatastoreMode(), req.getInstanceId(),
                    Role.Redis.SLAVE.getName(), slaveCompute.getAgentId(), slaveCompute.getEncryptedKey(), modeInfo.getPassword(),
                    slaveCompute.getComputeId(), null));

            masterSlaveRequest.put("CreateComputeSlave" + i + "Cluster",
                    BuildReqTaskMng.createCompute(req.getInstanceId(), slaveCompute.getComputeId(),
                            zones.get(i % zoneSize), req.getDatastoreCode()));
        }

        TaskManagerRequest taskManagerReq = new TaskManagerRequest();
        taskManagerReq.setServiceId("create_redis_master_slave");
        taskManagerReq.setData(masterSlaveRequest.toString());
        log.info("json master_slave send to task-manager => {}", CommonUtils.toJson(taskManagerReq.toMap()));

        return taskManagerReq;
    }

    @SneakyThrows
    private TaskManagerRequest standaloneCreate(InstanceCreateReq.DtValid req, RedisStandalone standaloneInfo, RequestInfo requestCtx) {
        AgentFirmwareEntity agentFirmware = this.getAgentFirmwareLatest();
        String urlBackup = this.generateUrlDownloadBackup(req.getBackupId(), requestCtx.getOrgId());

        CreateComputeInfo standalone = this.createComputeRecord(req.getInstanceId(), req.getFlavorId(),
                Role.Redis.MASTER.getName(), agentFirmware.getId(), agentFirmware.getBuildNumber(), standaloneInfo.getZone());

        JSONObject standaloneRequest = new JSONObject();
        standaloneRequest.put("CheckPrerequisitesStandalone", BuildReqTaskMng.checkPrerequisites(req.getInstanceId(),
                standalone.getComputeId(), req.getFlavorId(), req.getGlanceImageTag(), req.getNetworkId(), req.getSubnetId(),
                req.getDatastoreCode()));

        standaloneRequest.put("LoadConfigGroupStandalone",
                BuildReqTaskMng.loadConfigGroup(req.getInstanceId(), req.getGroupConfigurationId(), standalone.getComputeId()));

        standaloneRequest.put("CreateNetworkStandalone", BuildReqTaskMng.createNetwork(
                req.getInstanceId(), standalone.getComputeId(), req.getNetworkId(),
                req.getSubnetId(), null, req.getDatastoreCode(), req.getSecurityGroupIds()));

        standaloneRequest.put("CreateVolumeStandalone", BuildReqTaskMng.createVolume(
                req.getInstanceId(), standalone.getComputeId(), req.getVolumeSize(), standaloneInfo.getZone()));

        JSONObject cloudInit = BuildReqTaskMng.generateCloudInit(
                req.getDatastoreCode(), req.getDatastoreVersion(), req.getDatastoreMode(), req.getInstanceId(),
                Role.Redis.MASTER.getName(), standalone.getAgentId(), standalone.getEncryptedKey(),
                standaloneInfo.getPassword(), standalone.getComputeId(), urlBackup);
        cloudInit.put("backupMode", req.getBackupMode());
        standaloneRequest.put("GenerateCloudInitStandalone", cloudInit);

        standaloneRequest.put("CreateComputeStandalone", BuildReqTaskMng.createCompute(req.getInstanceId(),
                standalone.getComputeId(), null, req.getDatastoreCode()));

        TaskManagerRequest taskManagerReq = new TaskManagerRequest();
        taskManagerReq.setServiceId("create_redis_standalone");
        taskManagerReq.setData(standaloneRequest.toString());

        log.info("json standalone send to task-manager => {}", CommonUtils.toJson(taskManagerReq.toMap()));

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

    private AgentFirmwareEntity getAgentFirmwareLatest() {
        AgentFirmwareEntity agentFirmware = agentFirmwareService.getAgentFirmwareLatest();
        if (agentFirmware == null) {
            throw new AppException(new ErrorResponse("Not found agent firmware"));
        }

        return agentFirmware;
    }

    private InstanceDetail.ComputeInstanceInfo setResource(ComputeEntity compute, String token) {
        InstanceDetail.ComputeInstanceInfo masterDetail = new InstanceDetail.ComputeInstanceInfo();
        masterDetail.setId(compute.getId());
        masterDetail.setRole(compute.getRole());
        masterDetail.setMonitorResourceId(compute.getMonitorResourceId() != null ? compute.getMonitorResourceId() : "");

        ResourceCompute resourceMaster = this.getResourceOfCompute(compute, token);
        masterDetail.setIpAddress(resourceMaster.getIpAddress());
        masterDetail.setVCpus(resourceMaster.getVCpus());
        masterDetail.setRam(resourceMaster.getRam());
        masterDetail.setDisk(resourceMaster.getDisk());
        masterDetail.setVolumeSize(resourceMaster.getVolumeSize());
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

    private String generateUrlDownloadBackup(String backupId, String orgId) {
        log.info("generateUrlDownloadBackup: backupId[{}] - orgId[{}]", backupId, orgId);
        try {
            if (backupId == null) {
                return null;
            }
            BackupEntity backup = backupRepository.findFirstByIdAndOrgId(backupId, orgId).orElse(new BackupEntity());
            if (backup.getId() == null) {
                return null;
            }
            BackupStrategyEntity backupStrategy = backupStrategyRepository.findById(backup.getBackupStrategyId())
                    .orElseThrow(() -> new AppException(new ErrorResponse("Backup strategy not found")));
            String configJsonDecode = CryptoUtils.decrypt(backupStrategy.getConfiguration(), EnvConfig.KEY_DECRYPT_CONFIG_BACKUP);
            S3StorageConfigDto storageConfigDto = CommonUtils.convert(new JSONObject(configJsonDecode), S3StorageConfigDto.class);
            String objectName = orgId + "/" + backup.getFileName();
            AmazonS3 amazonS3 = S3Utils.buildAmazonS3(storageConfigDto);

            String urlGet = S3Utils.genPresignedUrl(amazonS3, HttpMethod.GET, objectName);

            return URLDecoder.decode(urlGet, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("generateUrlDownloadBackup ERROR => [{}]", e.getMessage());
            return null;
        }
    }

}
