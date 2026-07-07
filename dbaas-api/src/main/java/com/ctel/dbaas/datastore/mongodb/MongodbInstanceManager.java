package com.ctel.dbaas.datastore.mongodb;

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
import com.ctel.dbaas.datastore.DatastoreInstanceAbstract;
import com.ctel.dbaas.datastore.mongodb.model.Config;
import com.ctel.dbaas.datastore.mongodb.model.MongodbReplicaSet;
import com.ctel.dbaas.datastore.mongodb.model.MongodbSharedCluster;
import com.ctel.dbaas.datastore.mongodb.model.MongodbStandalone;
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
import com.ctel.dbaas.service.grpc_monitoring.MonitorGrpcClientService;
import com.ctel.dbaas.utils.CommonUtils;
import com.ctel.dbaas.utils.CryptoUtils;
import com.ctel.dbaas.utils.S3Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
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

@Log4j2
@Service
public class MongodbInstanceManager implements DatastoreInstanceAbstract {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AgentFirmwareService agentFirmwareService;

    @Autowired
    private ComputeRepository computeRepository;

    @Autowired
    private VolumeRepository volumeRepository;

    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    private FlavorRepository flavorRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private BackupStrategyRepository backupStrategyRepository;

    @Autowired
    private BackupRepository backupRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MonitorGrpcClientService monitorGrpcClientService;

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private CmcCloudService cmcCloudService;

    @SneakyThrows
    @Override
    public void createInstance(InstanceCreateReq request, RequestInfo requestCtx) {
        InstanceCreateReq.DtValid req = request.getDtValid();
        DatastoreMode.Mongodb mongodbMode = DatastoreMode.Mongodb.get(req.getDatastoreMode());
        switch (mongodbMode) {
            case STANDALONE -> {
                MongodbStandalone standalone = objectMapper
                        .readValue(request.getRequestMetadata(), MongodbStandalone.class);
                this.mongoStandaloneCreate(req, standalone, requestCtx);
            }
            case REPLICA_SET -> {
                MongodbReplicaSet replicaSet = objectMapper
                        .readValue(request.getRequestMetadata(), MongodbReplicaSet.class);
                this.mongoReplicaSetCreate(req, replicaSet, requestCtx);
            }
//            case SHARED_CLUSTER -> {
//                MongodbSharedCluster sharedCluster = objectMapper
//                        .readValue(request.getRequestMetadata(), MongodbSharedCluster.class);
//                this.mongoSharedClusterCreate(req, sharedCluster, requestCtx);
//            }
        }
    }

    @Override
    public InstanceDetail getInstanceDetail(InstanceInfo instanceInfo, RequestInfo requestCtx) {
        InstanceEntity instance = instanceInfo.getInstance();
        List<ComputeEntity> lstCompute = computeRepository.findAllByInstanceIdAndProjectIdAndOrgId(
                instanceInfo.getInstance().getId(), GrpcCtx.getReqCtx().getProjectId(), GrpcCtx.getReqCtx().getOrgId());
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
        instanceDetail.setStatus(instance.getStatus().toLowerCase());
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

        List<InstanceDetail.ComputeInstanceInfo> slavesDetail = new ArrayList<>();
        String statusArbiter = null;

        for (ComputeEntity slaveCompute : lstCompute) {
            slavesDetail.add(this.setResource(slaveCompute, requestCtx.getToken()));
            if (StringUtils.equalsIgnoreCase(Role.MongoDB.ARBITER.getName(), slaveCompute.getRole())) {
                statusArbiter = slaveCompute.getStatus();
            }
        }

        instanceDetail.setDataDetail(CommonUtils.toJson(new InstanceDetail.Mongodb(slavesDetail, statusArbiter)));

        return instanceDetail;
    }

    @SneakyThrows
    private void mongoStandaloneCreate(InstanceCreateReq.DtValid req, MongodbStandalone standalone, RequestInfo requestCtx) {
        String rootPwdMongo = CommonUtils.generateString(30);
        rootPwdMongo = "aadmin";
        Config mongoConfig = new Config(rootPwdMongo);

        InstanceEntity instanceEntity = instanceRepository.findById(req.getInstanceId())
                .orElseThrow(() -> new AppException(new ErrorResponse("Instance not found")));
        instanceEntity.setInstanceConfigEncrypt(CryptoUtils.encrypt(CommonUtils.toJson(mongoConfig), EnvConfig.KEY_DECRYPT_CONFIG_INSTANCE));
        instanceRepository.save(instanceEntity);

        AgentFirmwareEntity agentFirmware = this.getAgentFirmwareLatest();
        String urlBackup = this.generateUrlDownloadBackup(req.getBackupId(), requestCtx.getOrgId());

        CreateComputeInfo standaloneServer = this.createComputeDB(req.getInstanceId(),
                req.getFlavorId(), Role.MongoDB.PRIMARY.getName(), agentFirmware.getId(),
                agentFirmware.getBuildNumber(), standalone.getZone());

        JSONObject standaloneRequest = new JSONObject();
        standaloneRequest.put("CheckPrerequisitesStandalone", BuildReqTaskMng.checkPrerequisites(req.getInstanceId(),
                standaloneServer.getComputeId(), req.getFlavorId(), req.getGlanceImageTag(), req.getNetworkId(),
                req.getSubnetId(), req.getDatastoreCode()));

        standaloneRequest.put("LoadConfigGroupStandalone",
                BuildReqTaskMng.loadConfigGroup(req.getInstanceId(), req.getGroupConfigurationId(), standaloneServer.getComputeId()));

        standaloneRequest.put("CreateNetworkStandalone", BuildReqTaskMng.createNetwork(
                req.getInstanceId(), standaloneServer.getComputeId(), req.getNetworkId(),
                req.getSubnetId(), null, req.getDatastoreCode(), req.getSecurityGroupIds()));

        standaloneRequest.put("CreateVolumeStandalone", BuildReqTaskMng.createVolume(
                req.getInstanceId(), standaloneServer.getComputeId(), req.getVolumeSize(), standalone.getZone()));

        JSONObject cloudInitStandalone = BuildReqTaskMng.generateCloudInit(
                req.getDatastoreCode(), req.getDatastoreVersion(), req.getDatastoreMode(), req.getInstanceId(),
                Role.MongoDB.PRIMARY.getName(), standaloneServer.getAgentId(), standaloneServer.getEncryptedKey(),
                null, standaloneServer.getComputeId(), urlBackup);
        cloudInitStandalone.put("rootUser", Constant.ROOT_USER_MONGODB);
        cloudInitStandalone.put("rootPassword", rootPwdMongo);
        cloudInitStandalone.put("backupMode", req.getBackupMode());
        standaloneRequest.put("GenerateCloudInitStandalone", cloudInitStandalone);

        standaloneRequest.put("CreateComputeStandalone", BuildReqTaskMng.createCompute(req.getInstanceId(),
                standaloneServer.getComputeId(), standalone.getZone(), req.getDatastoreCode()));

        TaskManagerRequest taskManagerReq = new TaskManagerRequest();
        taskManagerReq.setServiceId("create_mongodb_standalone");
        taskManagerReq.setData(standaloneRequest.toString());

        log.info("json mongodb standalone send to task-manager => {}", CommonUtils.toJson(taskManagerReq.toMap()));
        rabbitTemplate.convertAndSend(RabbitMQConfig.DBAAS_TASK_MANAGER, RabbitMQConfig.ROUTING_KEY_TASK_MANAGER,
                taskManagerReq.toMap());
    }

    @SneakyThrows
    private void mongoReplicaSetCreate(InstanceCreateReq.DtValid req, MongodbReplicaSet replicaSet, RequestInfo requestCtx) {
        String rootPwdMongo = CommonUtils.generateString(30);
        rootPwdMongo = "aadmin";
        Config mongoConfig = new Config(rootPwdMongo);

        List<String> zones = new ArrayList<>(replicaSet.getZones());
        int zoneSize = zones.size();
        String zonePrimary = zones.get(0);

        InstanceEntity instanceEntity = instanceRepository.findById(req.getInstanceId())
                .orElseThrow(() -> new AppException(new ErrorResponse("Instance not found")));
        instanceEntity.setInstanceConfigEncrypt(CryptoUtils.encrypt(CommonUtils.toJson(mongoConfig), EnvConfig.KEY_DECRYPT_CONFIG_INSTANCE));
        instanceRepository.save(instanceEntity);

        AgentFirmwareEntity agentFirmware = this.getAgentFirmwareLatest();
        String urlBackup = this.generateUrlDownloadBackup(req.getBackupId(), requestCtx.getOrgId());

        CreateComputeInfo primaryServer = this.createComputeDB(req.getInstanceId(), req.getFlavorId(), Role.MongoDB.PRIMARY.getName(),
                agentFirmware.getId(), agentFirmware.getBuildNumber(), zonePrimary);

        JSONObject replicasetRequest = new JSONObject();
        replicasetRequest.put("LoadConfigGroupReplicaset",
                BuildReqTaskMng.loadConfigGroup(req.getInstanceId(), req.getGroupConfigurationId(), primaryServer.getComputeId()));

        replicasetRequest.put("CreateNetworkPrimaryReplicaset", BuildReqTaskMng.createNetwork(
                req.getInstanceId(), primaryServer.getComputeId(), req.getNetworkId(),
                req.getSubnetId(), null, req.getDatastoreCode(), req.getSecurityGroupIds()));

        replicasetRequest.put("CreateVolumePrimaryReplicaset", BuildReqTaskMng.createVolume(
                req.getInstanceId(), primaryServer.getComputeId(), req.getVolumeSize(), zonePrimary));

        JSONObject jsonCloudInit = BuildReqTaskMng.generateCloudInitMongodbReplicaset(
                req.getDatastoreCode(), req.getDatastoreVersion(), req.getDatastoreMode(), req.getInstanceId(),
                Role.MongoDB.PRIMARY.getName(), rootPwdMongo, urlBackup);
        jsonCloudInit.put("agentId", primaryServer.getAgentId());
        jsonCloudInit.put("encryptedKey", primaryServer.getEncryptedKey());
        jsonCloudInit.put("backupMode", req.getBackupMode());
        replicasetRequest.put("GenerateCloudInitPrimaryReplicaset", jsonCloudInit);

        replicasetRequest.put("CreateComputePrimaryReplicaset", BuildReqTaskMng.createCompute(req.getInstanceId(),
                primaryServer.getComputeId(), zonePrimary, req.getDatastoreCode()));

        // create arbiter node
        String flavorArbiter = EnvConfig.OS_FLAVOR_INIT_MONGODB_ARBITER;
        CreateComputeInfo computeArbiter = this.createComputeDB(req.getInstanceId(), flavorArbiter, Role.MongoDB.ARBITER.getName(),
                agentFirmware.getId(), agentFirmware.getBuildNumber(), zonePrimary);

        replicasetRequest.put("CreateNetworkArbiterReplicaset", BuildReqTaskMng.createNetwork(
                req.getInstanceId(), computeArbiter.getComputeId(), req.getNetworkId(),
                req.getSubnetId(), null, req.getDatastoreCode(), req.getSecurityGroupIds()));

        JSONObject cloudInitArbiter = BuildReqTaskMng.generateCloudInitMongodbReplicaset(
                req.getDatastoreCode(), req.getDatastoreVersion(), req.getDatastoreMode(), req.getInstanceId(),
                Role.MongoDB.ARBITER.getName(), rootPwdMongo, null);
        cloudInitArbiter.put("agentId", computeArbiter.getAgentId());
        cloudInitArbiter.put("encryptedKey", computeArbiter.getEncryptedKey());
        replicasetRequest.put("GenerateCloudInitArbiterReplicaset", cloudInitArbiter);

        replicasetRequest.put("CreateComputeArbiterReplicaset", BuildReqTaskMng.createCompute(
                req.getInstanceId(), computeArbiter.getComputeId(), zonePrimary, req.getDatastoreCode()));

        int quantityOfSecondary = replicaSet.getQuantityOfSecondary();
        JSONArray secondaryComputeIds = new JSONArray();
        for (int i = 0; i < quantityOfSecondary; i++) {
            CreateComputeInfo secondaryServer = this.createComputeDB(req.getInstanceId(),
                    req.getFlavorId(), Role.MongoDB.SECONDARY.getName(),
                    agentFirmware.getId(), agentFirmware.getBuildNumber(), zones.get(i % zoneSize));
            secondaryComputeIds.put(secondaryServer.getComputeId());
        }

        // network
        JSONObject networkSecondaryReplicaset = BuildReqTaskMng.createNetwork(
                req.getInstanceId(), null, req.getNetworkId(),
                req.getSubnetId(), null, req.getDatastoreCode(), req.getSecurityGroupIds());
        replicasetRequest.put("CreateNetworkSecondaryReplicaset", networkSecondaryReplicaset);

        //volume
        JSONObject volumeSecondaryReplicaset = BuildReqTaskMng.createVolume(
                req.getInstanceId(), null, req.getVolumeSize(), null);
        replicasetRequest.put("CreateVolumeSecondaryReplicaset", volumeSecondaryReplicaset);

        // cloud-init
        JSONObject cloudInitSecondaryReplicaset = BuildReqTaskMng.generateCloudInitMongodbReplicaset(
                req.getDatastoreCode(), req.getDatastoreVersion(), req.getDatastoreMode(), req.getInstanceId(),
                Role.MongoDB.SECONDARY.getName(), rootPwdMongo, null);
        replicasetRequest.put("GenerateCloudInitSecondaryReplicaset", cloudInitSecondaryReplicaset);

        // compute
        JSONObject computeSecondaryReplicaset = BuildReqTaskMng.createCompute(
                req.getInstanceId(), null, null, req.getDatastoreCode());
        replicasetRequest.put("CreateComputeSecondaryReplicaset", computeSecondaryReplicaset);

        JSONObject checkPrerequisites = BuildReqTaskMng.checkPrerequisites(req.getInstanceId(),
                primaryServer.getComputeId(), req.getFlavorId(), req.getGlanceImageTag(), req.getNetworkId(),
                req.getSubnetId(), req.getDatastoreCode());
        checkPrerequisites.put("secondaryComputeIds", secondaryComputeIds);
        replicasetRequest.put("CheckPrerequisitesReplicaset", checkPrerequisites);

        TaskManagerRequest taskManagerReq = new TaskManagerRequest();
        taskManagerReq.setServiceId("create_mongodb_replicaset");
        taskManagerReq.setData(replicasetRequest.toString());

        log.info("json mongodb replicaset send to task-manager => {}", CommonUtils.toJson(taskManagerReq.toMap()));
        rabbitTemplate.convertAndSend(RabbitMQConfig.DBAAS_TASK_MANAGER, RabbitMQConfig.ROUTING_KEY_TASK_MANAGER,
                taskManagerReq.toMap());
    }

    private void mongoSharedClusterCreate(InstanceCreateReq.DtValid req, MongodbSharedCluster sharedCluster, RequestInfo requestCtx) {

    }

    private CreateComputeInfo createComputeDB(String instanceId, String flavorId, String role, String agentFirmwareId,
                                              Integer buildNumber, String zone) {
        ComputeEntity compute = new ComputeEntity();
        compute.setFlavorId(flavorId);
        compute.setRole(role);
        compute.setStatus(ComputeStatus.BUILDING.getName());
        compute.setInstanceId(instanceId);
        compute.setProjectId(GrpcCtx.getReqCtx().getProjectId());
        compute.setOrgId(GrpcCtx.getReqCtx().getOrgId());
        compute.setRegionId(GrpcCtx.getReqCtx().getRegionId());
        compute.setZoneName(zone);
        computeRepository.save(compute);

        AgentEntity agent = new AgentEntity();
        agent.setName("agent-compute-" + compute.getId());
        agent.setEncryptedKey(CommonUtils.generateString(32));
        agent.setAgentFirmwareId(agentFirmwareId);
        agent.setAgentVersion(String.valueOf(buildNumber == null ? 0 : buildNumber));
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
            return new AgentFirmwareEntity();
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

//        FlavorEntity flavorEntity = flavorRepository.findFirstByOsFlavorId(compute.getFlavorId()).orElse(new FlavorEntity());
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
