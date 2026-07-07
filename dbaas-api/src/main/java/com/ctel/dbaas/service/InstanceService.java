package com.ctel.dbaas.service;

import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.common.enums.*;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.datastore.DatastoreInstanceAbstract;
import com.ctel.dbaas.datastore.api_gateway.ApiGatewayInstanceManager;
import com.ctel.dbaas.datastore.kafka.KafkaInstanceManager;
import com.ctel.dbaas.datastore.kafka.model.KafkaCluster;
import com.ctel.dbaas.datastore.kafka.model.KafkaSingleNode;
import com.ctel.dbaas.datastore.mongodb.MongodbInstanceManager;
import com.ctel.dbaas.datastore.mongodb.model.MongodbReplicaSet;
import com.ctel.dbaas.datastore.mongodb.model.MongodbStandalone;
import com.ctel.dbaas.datastore.postgres.PostgresInstanceManager;
import com.ctel.dbaas.datastore.redis.RedisInstanceManager;
import com.ctel.dbaas.datastore.redis.model.RedisCluster;
import com.ctel.dbaas.datastore.redis.model.RedisMasterSlave;
import com.ctel.dbaas.datastore.redis.model.RedisStandalone;
import com.ctel.dbaas.dto.cmc_cloud.SubnetInfo;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.compute.FlavorInfo;
import com.ctel.dbaas.dto.configuration.CreateGroupConfigReq;
import com.ctel.dbaas.dto.instance.*;
import com.ctel.dbaas.dto.resource.ResourceUsedReq;
import com.ctel.dbaas.entity.dbaas.*;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.*;
import com.ctel.dbaas.repository.dbaas.projection.DatastoreModeInfo;
import com.ctel.dbaas.repository.dbaas.projection.FlavorIdCompute;
import com.ctel.dbaas.repository.dbaas.projection.instance.InstanceDropdownRes;
import com.ctel.dbaas.service.grpc_monitoring.MonitorGrpcClientService;
import com.ctel.dbaas.utils.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class InstanceService {

    private final Map<String, DatastoreInstanceAbstract> MAP_DATASTORE_MANAGER = new HashMap<>();
    @Autowired
    private InstanceRepository instanceRepository;
    @Autowired
    private DatastoreVersionRepository datastoreVersionRepository;
    @Autowired
    private DatastoreRepository datastoreRepository;
    @Autowired
    private DatastoreModeRepository datastoreModeRepository;
    @Autowired
    private GroupConfigurationRepository groupConfigurationRepository;
    @Autowired
    private ComputeRepository computeRepository;
    @Autowired
    private VolumeRepository volumeRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FlavorRepository flavorRepository;
    @Autowired
    private ActionService actionService;
    @Autowired
    private MonitorGrpcClientService monitorGrpcClientService;
    @Autowired
    private CmcCloudService cmcCloudService;
    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    private DatastoreConfigurationRepository datastoreConfigurationRepository;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private BackupRepository backupRepository;
    @Autowired
    private BackupScheduleRepository backupScheduleRepository;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private ResourceInstanceRepository resourceInstanceRepository;

    public InstanceService(
            RedisInstanceManager redisManager,
            MongodbInstanceManager mongodbManager,
            KafkaInstanceManager kafkaInstanceManager,
            ApiGatewayInstanceManager apiGatewayInstanceManager,
            PostgresInstanceManager postgresInstanceManager) {
        MAP_DATASTORE_MANAGER.put(DatastoreSupport.REDIS.getCode(), redisManager);
        MAP_DATASTORE_MANAGER.put(DatastoreSupport.MONGODB.getCode(), mongodbManager);
        MAP_DATASTORE_MANAGER.put(DatastoreSupport.KAFKA.getCode(), kafkaInstanceManager);
        MAP_DATASTORE_MANAGER.put(DatastoreSupport.API_GATEWAY.getCode(), apiGatewayInstanceManager);
        MAP_DATASTORE_MANAGER.put(DatastoreSupport.POSTGRESQL.getCode(), postgresInstanceManager);
    }

    @SneakyThrows
    public String createInstance(InstanceCreateReq req, RequestInfo requestCtx) {

        // validate datastore
        DatastoreEntity datastore = datastoreRepository
                .findFirstByCodeAndStatus(req.getDatastore().getDatastoreCode(), Status.ACTIVE.getStatus())
                .orElseThrow(() -> new AppException(new ErrorResponse("Not support datastore [%s]",
                        req.getDatastore().getDatastoreName())));

        DatastoreVersionEntity datastoreVersion = datastoreVersionRepository
                .findFirstByDatastoreIdAndIdAndStatus(datastore.getId(), req.getDatastore().getDatastoreVersionId(),
                        Status.ACTIVE.getStatus())
                .orElseThrow(() -> new AppException(new ErrorResponse("Version not found [%s]",
                        req.getDatastore().getDatastoreVersionId())));

        DatastoreModeEntity datastoreMode = datastoreModeRepository
                .findFirstByIdAndDatastoreVersionId(req.getDatastore().getDatastoreModeId(), datastoreVersion.getId())
                .orElseThrow(() -> new AppException(new ErrorResponse(
                        "Mode not found with datastore %s - version %s",
                        datastore.getName(), datastoreVersion.getVersion())));

        String backupMode = "";
        if (StringUtils.isNotBlank(req.getBackupId())) {
            BackupEntity backup = backupRepository.findFirstByIdAndOrgId(req.getBackupId(), requestCtx.getOrgId())
                    .orElseThrow(() -> new AppException(new ErrorResponse("Backup not found")));
            if (!StringUtils.equals(backup.getDatastoreCode(), datastore.getCode())) {
                throw new AppException(new ErrorResponse("Datastore of instance not match with backup"));
            }
            backupMode = backup.getBackupMode();
        }

        boolean needGroupConfig = !datastore.getCode().equals(DatastoreSupport.KAFKA.getCode());

        // configuration for instance
        String groupConfigId = null;
        if (needGroupConfig) {
            if (req.getGroupConfigurationId().isEmpty()) {
                List<DatastoreConfigurationEntity> lstConfigDefault = datastoreConfigurationRepository
                        .findAllByDatastoreModeIdAndStatus(datastoreMode.getId(), Status.ACTIVE.getStatus());
                Map<String, String> overridesConfig = lstConfigDefault.stream()
                        .collect(Collectors.toMap(DatastoreConfigurationEntity::getParamName, DatastoreConfigurationEntity::getDefaultValue));

                CreateGroupConfigReq createGroupConfigReq = CreateGroupConfigReq.builder()
                        .datastoreModeId(req.getDatastore().getDatastoreModeId())
                        .name(req.getName() + "-config-" + CommonUtils.generateString(5))
                        .description("clone from group config default " + req.getDatastore().getDatastoreCode()
                                + " " + req.getDatastore().getDatastoreVersionId())
                        .overridesConfig(overridesConfig)
                        .build();
                createGroupConfigReq.validate();
                groupConfigId = configurationService.createGroupConfiguration(createGroupConfigReq);
            } else {
                GroupConfigurationEntity groupConfig = groupConfigurationRepository.findFirstByIdAndDatastoreModeIdAndOrgId(
                        req.getGroupConfigurationId(), req.getDatastore().getDatastoreModeId(), requestCtx.getOrgId());
                if (groupConfig == null) {
                    throw new AppException(new ErrorResponse("Group configuration not found"));
                }
                groupConfigId = groupConfig.getId();
            }
        }

        DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(datastore.getCode());

        FlavorInfo flavor = cmcCloudService.getFlavor(req.getFlavorId(), requestCtx.getToken(), requestCtx.getRegionId());
        if (flavor.getFlavorId() == null) {
            throw new AppException(new ErrorResponse("flavor not found - flavorId[%s]", req.getFlavorId()));
        }

        // validate metadata by mode of datastore
        Set<String> zone = new HashSet<>();
        ResourceUsedReq resourceUsedForInstance = new ResourceUsedReq();
        int quantityOfServer;
        switch (datastoreSupport) {
            case REDIS -> {
                DatastoreMode.Redis redisMode = DatastoreMode.Redis.get(datastoreMode.getCode());
                switch (redisMode) {
                    case STANDALONE -> {
                        RedisStandalone standalone = objectMapper
                                .readValue(req.getRequestMetadata(), RedisStandalone.class);
                        standalone.validate();

                        quantityOfServer = 1;
                        resourceUsedForInstance.setCpu(flavor.getVCpus() * quantityOfServer);
                        resourceUsedForInstance.setRam(flavor.getRam() * quantityOfServer);
                        resourceUsedForInstance.setSystemDisk(flavor.getDisk() * quantityOfServer);
                        resourceUsedForInstance.setVolumeGb(req.getVolumeSize() * quantityOfServer);

                        zone = new HashSet<>(Collections.singletonList(standalone.getZone()));
                    }
                    case MASTER_SLAVE -> {
                        RedisMasterSlave redisMasterSlave = objectMapper
                                .readValue(req.getRequestMetadata(), RedisMasterSlave.class);
                        redisMasterSlave.setNumOfSlaves(2);
                        redisMasterSlave.validate();

                        quantityOfServer = redisMasterSlave.getNumOfSlaves() + 1;
                        resourceUsedForInstance.setCpu(flavor.getVCpus() * quantityOfServer);
                        resourceUsedForInstance.setRam(flavor.getRam() * quantityOfServer);
                        resourceUsedForInstance.setSystemDisk(flavor.getDisk() * quantityOfServer);
                        resourceUsedForInstance.setVolumeGb(req.getVolumeSize() * quantityOfServer);

                        zone = new HashSet<>(redisMasterSlave.getZones());
                    }
                    case CLUSTER -> {
                        RedisCluster redisCluster = objectMapper
                                .readValue(req.getRequestMetadata(), RedisCluster.class);
                        redisCluster.setNumOfMasterServer(3);
                        redisCluster.validate();

                        quantityOfServer = redisCluster.getNumOfMasterServer() * (redisCluster.getReplicas() + 1);
                        resourceUsedForInstance.setCpu(flavor.getVCpus() * quantityOfServer);
                        resourceUsedForInstance.setRam(flavor.getRam() * quantityOfServer);
                        resourceUsedForInstance.setSystemDisk(flavor.getDisk() * quantityOfServer);
                        resourceUsedForInstance.setVolumeGb(req.getVolumeSize() * quantityOfServer);

                        zone = new HashSet<>(redisCluster.getZones());
                    }
                }
            }
            case MONGODB -> {
                DatastoreMode.Mongodb mongodbMode = DatastoreMode.Mongodb.get(datastoreMode.getCode());
                switch (mongodbMode) {
                    case STANDALONE -> {
                        MongodbStandalone standalone = objectMapper
                                .readValue(req.getRequestMetadata(), MongodbStandalone.class);
                        standalone.validate();

                        quantityOfServer = 1;
                        resourceUsedForInstance.setCpu(flavor.getVCpus() * quantityOfServer);
                        resourceUsedForInstance.setRam(flavor.getRam() * quantityOfServer);
                        resourceUsedForInstance.setSystemDisk(flavor.getDisk() * quantityOfServer);
                        resourceUsedForInstance.setVolumeGb(req.getVolumeSize() * quantityOfServer);
                        zone = new HashSet<>(Collections.singletonList(standalone.getZone()));
                    }
                    case REPLICA_SET -> {
                        MongodbReplicaSet replicaSet = objectMapper
                                .readValue(req.getRequestMetadata(), MongodbReplicaSet.class);
                        replicaSet.validate();

                        quantityOfServer = replicaSet.getQuantityOfSecondary() + 1;
                        FlavorInfo flavorArbiter = cmcCloudService.getFlavor(
                                EnvConfig.OS_FLAVOR_INIT_MONGODB_ARBITER, requestCtx.getToken(), requestCtx.getRegionId());
                        if (flavorArbiter.getFlavorId() == null) {
                            throw new AppException(new ErrorResponse("Not found flavorId[%s] for arbiter", EnvConfig.OS_FLAVOR_INIT_MONGODB_ARBITER));
                        }
                        int cpuArbiter = flavorArbiter.getVCpus();
                        int ramArbiter = flavorArbiter.getRam();
                        int diskArbiter = flavorArbiter.getDisk();

                        resourceUsedForInstance.setCpu(flavor.getVCpus() * quantityOfServer + cpuArbiter);
                        resourceUsedForInstance.setRam(flavor.getRam() * quantityOfServer + ramArbiter);
                        resourceUsedForInstance.setSystemDisk(flavor.getDisk() * quantityOfServer + diskArbiter);
                        resourceUsedForInstance.setVolumeGb(req.getVolumeSize() * quantityOfServer);

                        zone = new HashSet<>(replicaSet.getZones());
                    }
                }
            }
            case KAFKA -> {
                DatastoreMode.Kafka kafkaMode = DatastoreMode.Kafka.get(datastoreMode.getCode());
                switch (kafkaMode) {
                    case SINGLE_NODE -> {
                        KafkaSingleNode stand = objectMapper.readValue(req.getRequestMetadata(), KafkaSingleNode.class);
                        stand.validate();

                        quantityOfServer = 1;
                        resourceUsedForInstance.setCpu(flavor.getVCpus() * quantityOfServer);
                        resourceUsedForInstance.setRam(flavor.getRam() * quantityOfServer);
                        resourceUsedForInstance.setSystemDisk(flavor.getDisk() * quantityOfServer);
                        resourceUsedForInstance.setVolumeGb(req.getVolumeSize() * quantityOfServer);

                        zone = new HashSet<>(Collections.singletonList(stand.getZone()));
                    }
                    case CLUSTER -> {
                        KafkaCluster cluster = objectMapper.readValue(req.getRequestMetadata(), KafkaCluster.class);
                        cluster.validate();

                        quantityOfServer = cluster.getZones().size() * cluster.getBrokersPerZone();
                        resourceUsedForInstance.setCpu(flavor.getVCpus() * quantityOfServer);
                        resourceUsedForInstance.setRam(flavor.getRam() * quantityOfServer);
                        resourceUsedForInstance.setSystemDisk(flavor.getDisk() * quantityOfServer);
                        resourceUsedForInstance.setVolumeGb(req.getVolumeSize() * quantityOfServer);

                        zone = new HashSet<>(cluster.getZones());
                    }
                }
            }
        }

        SubnetInfo subnetInfo = null;
        if (!"dev".equals(EnvConfig.TEST_REGION_DEV)) {
            cmcCloudService.validateZone(requestCtx.getToken(), requestCtx.getRegionId(), zone);
            subnetInfo = cmcCloudService.getSubnet(req.getSubnetId(), requestCtx);
            if (subnetInfo == null) {
                throw new AppException(new ErrorResponse("Not found subnet[%s]", req.getSubnetId()));
            }

            if (!StringUtils.equals(req.getNetworkId(), subnetInfo.getNetworkId())) {
                throw new AppException(new ErrorResponse("not found network with id[%s]", req.getNetworkId()));
            }

            cmcCloudService.validateNetworkPortalV2(requestCtx, req.getSecurityGroupIds());
        }

        ResourceEntity currentResourceUsed = resourceService.checkAndSetResourceUsed(resourceUsedForInstance, requestCtx);
        InstanceEntity instance = new InstanceEntity();
        instance.setName(req.getName());
        instance.setFlavorId(req.getFlavorId());
        instance.setVolumeSize(req.getVolumeSize());
        instance.setDatastoreId(datastore.getId());
        instance.setDatastoreVersionId(datastoreVersion.getId());
        instance.setDatastoreModeId(datastoreMode.getId());
        instance.setGroupConfigurationId(groupConfigId);
        instance.setNeutronSecurityGroupClientIds("[]");
        instance.setResourcePackage("{}");
        instance.setStatus(InstanceStatus.WAITING.getStatus());
        instance.setMessage("");
        instance.setProjectId(requestCtx.getProjectId());
        instance.setOrgId(requestCtx.getOrgId());
        instance.setRegionId(requestCtx.getRegionId());
        instance.setFlavorId(req.getFlavorId());
        instance.setSubnetId(req.getSubnetId());

        if (subnetInfo != null) {
            instance.setNetworkId(subnetInfo.getNetworkId());
            instance.setVpcId(subnetInfo.getVpcId());
        }

        instanceRepository.save(instance);

        ResourceInstanceEntity resourceExecute = new ResourceInstanceEntity();
        resourceExecute.setInstanceId(instance.getId());
        resourceExecute.setResourceId(currentResourceUsed.getId());
        resourceExecute.setResourceUsed(CommonUtils.toJson(resourceUsedForInstance));
        resourceInstanceRepository.save(resourceExecute);

        InstanceCreateReq.DtValid dtValid = InstanceCreateReq.DtValid.builder()
                .datastoreId(datastore.getId())
                .datastoreName(datastore.getName())
                .datastoreCode(datastore.getCode())
                .datastoreVersionId(datastoreVersion.getId())
                .datastoreVersion(datastoreVersion.getVersion())
                .datastoreModeId(datastoreMode.getId())
                .datastoreMode(datastoreMode.getCode())
                .vpcId(instance.getVpcId())
                .networkId(instance.getNetworkId())
                .subnetId(req.getSubnetId())
                .glanceImageTag(datastoreVersion.getGlanceImageTags())
                .flavorId(req.getFlavorId())
                .volumeSize(req.getVolumeSize())
                .groupConfigurationId(instance.getGroupConfigurationId())
                .requestMetadata(req.getRequestMetadata())
                .instanceId(instance.getId())
                .instanceName(req.getName())
                .regionId(instance.getRegionId())
                .securityGroupIds(req.getSecurityGroupIds())
                .backupId(req.getBackupId())
                .backupMode(backupMode)
                .build();
        req.setDtValid(dtValid);

        DatastoreInstanceAbstract datastoreInstanceAbstract = MAP_DATASTORE_MANAGER.get(datastoreSupport.getCode());
        datastoreInstanceAbstract.createInstance(req, requestCtx);

        return instance.getId();
    }

    public Page<InstanceRes> listInstance(String nameSearch, List<String> datastoreCodes, Pageable pageable, RequestInfo ctxRequest) {
        Page<InstanceEntity> instances;
        List<String> datastoreIds = datastoreRepository.findByCodeInAndStatus(datastoreCodes, Status.ACTIVE.getStatus())
                .stream().map(DatastoreEntity::getId).toList();
        if (StringUtils.isBlank(nameSearch)) {
            instances = instanceRepository.findByOrgIdAndDatastoreIdInAndProjectIdAndDeletedAtNull(
                    ctxRequest.getOrgId(), datastoreIds, ctxRequest.getProjectId(), pageable);
        } else {
            instances = instanceRepository.findByOrgIdAndDatastoreIdInAndProjectIdAndDeletedAtNullAndNameContainingIgnoreCase(
                    ctxRequest.getOrgId(), datastoreIds, ctxRequest.getProjectId(), nameSearch, pageable);
        }
        return instances.map(this::convertToRes);
    }

    public List<InstanceDropdownRes> listInstanceDropdown(String nameSearch, List<String> datastoreCodes, String status, RequestInfo reqCtx) {
        return instanceRepository.queryInstanceDropdown(nameSearch, datastoreCodes, status, reqCtx);
    }

    public InstanceDetail getInstanceDetail(String instanceId, RequestInfo requestCtx) {
        InstanceInfo instanceInfo = this.getInstanceInfo(instanceId, false);
        DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(instanceInfo.getDatastoreCode());
        DatastoreInstanceAbstract datastoreInstanceAbstract = MAP_DATASTORE_MANAGER.get(datastoreSupport.getCode());
        return datastoreInstanceAbstract.getInstanceDetail(instanceInfo, requestCtx);
    }

    public InstanceInfo getInstanceInfo(String instanceId, boolean isSystemCall) {
        InstanceEntity instance;
        if (isSystemCall) {
            instance = instanceRepository.findByIdAndDeletedAtIsNull(instanceId)
                    .orElseThrow(() -> new AppException(new ErrorResponse("instance not found")));
        } else {
            instance = instanceRepository.findByIdAndOrgIdAndProjectIdAndDeletedAtIsNull(
                            instanceId, GrpcCtx.getReqCtx().getOrgId(), GrpcCtx.getReqCtx().getProjectId())
                    .orElseThrow(() -> new AppException(new ErrorResponse("instance not found")));
        }

        DatastoreEntity datastore = datastoreRepository
                .findById(instance.getDatastoreId())
                .orElseThrow(() -> new AppException(new ErrorResponse("datastore not found")));

        DatastoreVersionEntity datastoreVersion = datastoreVersionRepository
                .findById(instance.getDatastoreVersionId())
                .orElseThrow(() -> new AppException(new ErrorResponse("datastore version not found")));

        DatastoreModeEntity datastoreMode = datastoreModeRepository
                .findById(instance.getDatastoreModeId())
                .orElseThrow(() -> new AppException(new ErrorResponse("datastore mode not found")));

        return InstanceInfo.builder()
                .instance(instance)
                .datastoreId(datastore.getId())
                .datastoreName(datastore.getName())
                .datastoreCode(datastore.getCode())
                .datastoreVersionId(datastoreVersion.getId())
                .datastoreVersion(datastoreVersion.getVersion())
                .datastoreModeId(datastoreMode.getId())
                .datastoreModeCode(datastoreMode.getCode())
                .datastoreModeName(datastoreMode.getName())
                .build();
    }

    public void deleteInstance(List<String> instanceIds, RequestInfo requestInfo) {
        List<InstanceEntity> lstInstance = instanceRepository.findAllByIdInAndOrgIdAndProjectId(
                instanceIds, requestInfo.getOrgId(), requestInfo.getProjectId());
        for (InstanceEntity instance : lstInstance) {
            monitorGrpcClientService.deleteAgentMonitor(instance.getId(), instance.getRegionId(), instance.getProjectId(), instance.getOrgId());
            backupScheduleRepository.deleteByInstanceId(instance.getId());
            actionService.executeAction(instance.getId(), InstanceAction.DELETE_INSTANCE, new HashMap<>(), requestInfo);
            resourceService.deleteResourceInstance(instance.getId(), requestInfo);
        }
    }

    // system trigger
    @SneakyThrows
    public void updateStatusApplyConfig(String agentId, String groupConfigId) {
        AgentEntity agent = agentRepository.findById(agentId).orElse(new AgentEntity());
        if (agent.getId() == null) {
            log.warn("Not found agent with id[{}]", agentId);
            return;
        }

        GroupConfigurationEntity groupConfiguration = groupConfigurationRepository.findById(groupConfigId)
                .orElse(new GroupConfigurationEntity());
        if (groupConfiguration.getId() == null) {
            log.warn("Not found group configuration with id[{}]", groupConfigId);
            return;
        }

        InstanceEntity instanceEntity = instanceRepository.findById(agent.getInstanceId()).orElse(new InstanceEntity());
        if (instanceEntity.getId() != null) {
            if (!Objects.equals(groupConfiguration.getDatastoreModeId(), instanceEntity.getDatastoreModeId())) {
                log.warn("DatastoreMode not match - group for mode[{}] - instance mode[{}]",
                        groupConfiguration.getDatastoreModeId(), instanceEntity.getDatastoreModeId());
                return;
            }
            instanceEntity.setGroupConfigurationId(groupConfigId);
            instanceRepository.save(instanceEntity);
        }
    }

    private InstanceRes convertToRes(InstanceEntity entity) {
        InstanceRes res = new InstanceRes();
        res.setId(entity.getId());
        res.setName(entity.getName());

        ResourceInstance resource = this.getResourceOfInstance(entity.getId());
        res.setVCpus(resource.getVCpus());
        res.setRam(resource.getRam());
        res.setDisk(resource.getDisk());
        res.setVolumeSize(resource.getVolumeSize());
        res.setGroupConfigId(entity.getGroupConfigurationId() == null ? "" : entity.getGroupConfigurationId());

        DatastoreModeInfo datastore = datastoreModeRepository.getByDatastoreModeId(entity.getDatastoreModeId());

        res.setDatastoreName(datastore.getDatastoreName());
        res.setDatastoreCode(datastore.getDatastoreCode());
        res.setDatastoreVersion(datastore.getDatastoreVersion());
        res.setDatastoreVersionId(entity.getDatastoreVersionId());
        res.setDatastoreMode(datastore.getDatastoreMode());
        res.setDatastoreModeId(entity.getDatastoreModeId());

        res.setStatus(entity.getStatus().toUpperCase());
        res.setCreatedAt(entity.getCreatedAt() == null ? "" : entity.getCreatedAt().toString());
        res.setUpdatedAt(entity.getUpdatedAt() == null ? "" : entity.getUpdatedAt().toString());

        return res;
    }

    private ResourceInstance getResourceOfInstance(String instanceId) {
        List<FlavorIdCompute> computeResource = computeRepository.getFlavorIds(instanceId);

        if (!computeResource.isEmpty()) {
            ResourceInstance res = new ResourceInstance();
            for (FlavorIdCompute compute : computeResource) {
                FlavorEntity flavorEntity = flavorRepository.findFirstByOsFlavorId(compute.getFlavorId()).orElse(new FlavorEntity());
                res.setDisk(res.getDisk() + flavorEntity.getDisk());
                res.setVCpus(res.getVCpus() + flavorEntity.getVCpus());
                res.setRam(res.getRam() + flavorEntity.getRam());

                VolumeEntity volumeEntity = volumeRepository.findFirstByComputeId(compute.getComputeId());
                if (volumeEntity != null) {
                    res.setVolumeSize(res.getVolumeSize() + volumeEntity.getSize());
                }
            }

            return res;
        }

        return new ResourceInstance();
    }

}
