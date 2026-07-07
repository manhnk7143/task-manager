package com.ctel.dbaas.datastore;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.common.enums.DatastoreMode;
import com.ctel.dbaas.common.enums.DatastoreSupport;
import com.ctel.dbaas.common.enums.Role;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.dto.backup.S3StorageConfigDto;
import com.ctel.dbaas.dto.cmc_cloud.SecurityGroupInfo;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.compute.FlavorInfo;
import com.ctel.dbaas.dto.instance.InstanceInfo;
import com.ctel.dbaas.dto.resource.ResourceUsedReq;
import com.ctel.dbaas.dto.resource.ResourceUsedRes;
import com.ctel.dbaas.entity.dbaas.*;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.*;
import com.ctel.dbaas.service.CmcCloudService;
import com.ctel.dbaas.service.ResourceService;
import com.ctel.dbaas.service.grpc_monitoring.MonitorGrpcClientService;
import com.ctel.dbaas.utils.CommonUtils;
import com.ctel.dbaas.utils.CryptoUtils;
import com.ctel.dbaas.utils.S3Utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;


@Getter
@Setter
@Component
@Log4j2
public abstract class DatastoreActionAbstract {

    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    protected BackupRepository backupRepository;

    @Autowired
    protected BackupStrategyRepository backupStrategyRepository;

    @Autowired
    protected GroupConfigurationRepository groupConfigurationRepository;

    @Autowired
    protected ConfigurationRepository configurationRepository;

    @Autowired
    protected InstanceRepository instanceRepository;

    @Autowired
    protected ComputeRepository computeRepository;

    @Autowired
    protected AgentRepository agentRepository;

    @Autowired
    protected MonitorGrpcClientService monitorGrpcClientService;

    @Autowired
    private VolumeRepository volumeRepository;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ResourceInstanceRepository resourceInstanceRepository;

    @Autowired
    private CmcCloudService cmcCloudService;


    private InstanceInfo instanceInfo;

    private Map<String, Object> data;

    private RequestInfo reqCtx;


    public abstract Map<String, Object> startInstance();

    public abstract Map<String, Object> promoteSlaveToMaster();

    public abstract Map<String, Object> setPassword();

    public abstract Map<String, Object> createBackup();

    public abstract Map<String, Object> getListUser();

    public abstract Map<String, Object> getListDatabase();

    public abstract Map<String, Object> dbAction();

    public Map<String, Object> stopInstance() {
        return new HashMap<>();
    }

    public Map<String, Object> restartInstance() {
        return new HashMap<>();
    }

    public Map<String, Object> deleteInstance() {
        return new HashMap<>();
    }

    @SneakyThrows
    public Map<String, Object> restoreBackup() {
        Map<String, Object> res = new HashMap<>();

        // validate
        String backupId = this.getData().get("backupId").toString();
        if (StringUtils.isBlank(backupId)) {
            throw new AppException(new ErrorResponse("backupId cannot be empty"));
        }
        //

        BackupEntity backup = backupRepository.findFirstByIdAndOrgId(backupId, this.getReqCtx().getOrgId())
                .orElseThrow(() -> new AppException(new ErrorResponse("Backup not found")));
        if (!StringUtils.equals(backup.getDatastoreCode(), this.getInstanceInfo().getDatastoreCode())) {
            throw new AppException(new ErrorResponse("Datastore of instance not match with backup"));
        }

//        DatastoreSupport datastoreInstance = DatastoreSupport.get(this.getInstanceInfo().getDatastoreCode());
//        if (datastoreInstance.equals(DatastoreSupport.REDIS) &&
//                this.getInstanceInfo().getDatastoreModeCode().equalsIgnoreCase(DatastoreMode.Redis.STANDALONE.getCode()) &&
//                backup.getDatastoreMode().equalsIgnoreCase(DatastoreMode.Redis.CLUSTER.getCode())) {
//            throw new AppException(new ErrorResponse("feature restore backup from cluster to standalone coming soon..."));
//        }

        BackupStrategyEntity backupStrategy = backupStrategyRepository.findById(backup.getBackupStrategyId())
                .orElseThrow(() -> new AppException(new ErrorResponse("Backup strategy not found")));

        String configJsonDecode = CryptoUtils.decrypt(backupStrategy.getConfiguration(), EnvConfig.KEY_DECRYPT_CONFIG_BACKUP);
        S3StorageConfigDto storageConfigDto = CommonUtils.convert(new JSONObject(configJsonDecode), S3StorageConfigDto.class);

        String objectName = this.getReqCtx().getOrgId() + "/" + backup.getFileName();
        String urlGet = this.genUrls3(storageConfigDto, objectName, HttpMethod.GET);
        res.put("url", urlGet);
        res.put("backupMode", backup.getBackupMode());

        return res;
    }

    public Map<String, Object> changeGroupConfig() {
        Map<String, Object> res = new HashMap<>();
        String groupConfigId = this.getData().get("groupConfigId").toString();

        //validate
        if (StringUtils.isBlank(groupConfigId)) {
            throw new AppException(new ErrorResponse("groupConfigId cannot be empty"));
        }
        //

        GroupConfigurationEntity groupConfig = groupConfigurationRepository.findById(groupConfigId).orElse(null);
        if (groupConfig == null) {
            throw new AppException(new ErrorResponse("Group config not found"));
        }

        if (!groupConfig.isDefault() && !Objects.equals(groupConfig.getOrgId(), this.getReqCtx().getOrgId())) {
            throw new AppException(new ErrorResponse("Not permission to access this group config"));
        }

        if (!Objects.equals(groupConfig.getDatastoreModeId(), this.getInstanceInfo().getDatastoreModeId())) {
            throw new AppException(new ErrorResponse("This group config does not match the version of the instance"));
        }
        Map<String, Object> lstConfig = this.getConfigFromGroup(groupConfigId);
        res.put("overridesConfig", lstConfig);
        res.put("groupConfigId", groupConfigId);

        InstanceEntity instance = this.instanceInfo.getInstance();
        instance.setGroupConfigurationId(groupConfigId);
        instanceRepository.save(instance);

        return res;
    }

    @SneakyThrows
    public Map<String, Object> attachSecurityGroup() {
        Map<String, Object> mapReq = new HashMap<>();
        String sgIdsReq = this.getData().get("securityGroupIds").toString();
        if (StringUtils.isBlank(sgIdsReq)) {
            throw new AppException(new ErrorResponse("securityGroupIds cannot be empty"));
        }

        LinkedHashSet<String> idsToAttach = new LinkedHashSet<>(new HashSet<>(Arrays.asList(sgIdsReq.split(","))));
        for (String sgId : idsToAttach) {
            if (!CommonUtils.isValidUUID(sgId)) {
                throw new AppException(new ErrorResponse("securityGroupId invalid"));
            }
            if (!"dev".equals(EnvConfig.TEST_REGION_DEV)) {
                SecurityGroupInfo securityGroupInfo = cmcCloudService.getSecurityGroup(sgId, this.getReqCtx());
                if (securityGroupInfo == null) {
                    throw new AppException(new ErrorResponse("Security group[%s] not found", sgId));
                }
            }
        }

        InstanceEntity instanceEntity = this.getInstanceInfo().getInstance();
        if (instanceEntity.getNeutronSecurityGroupClientIds() != null) {
            JSONArray arrIds = new JSONArray(instanceEntity.getNeutronSecurityGroupClientIds());
            for (int i = 0; i < arrIds.length(); i++) {
                idsToAttach.add(arrIds.getString(i));
            }
        }

        if (idsToAttach.size() > 10) {
            throw new AppException(new ErrorResponse("The number of security group cannot exceed 10"));
        }
        mapReq.put("securityGroupIds", idsToAttach);

        return mapReq;
    }

    @SneakyThrows
    public Map<String, Object> detachSecurityGroup() {
        Map<String, Object> mapReq = new HashMap<>();

        String sgIdsReq = this.getData().get("securityGroupIds").toString();
        if (StringUtils.isBlank(sgIdsReq)) {
            throw new AppException(new ErrorResponse("securityGroupIds cannot be empty"));
        }

        LinkedHashSet<String> lstIdsToDetach = new LinkedHashSet<>(new HashSet<>(Arrays.asList(sgIdsReq.split(","))));
        for (String sgId : lstIdsToDetach) {
            if (!CommonUtils.isValidUUID(sgId)) {
                throw new AppException(new ErrorResponse("securityGroupId invalid"));
            }
        }

        InstanceEntity instanceEntity = this.getInstanceInfo().getInstance();
        if (instanceEntity.getNeutronSecurityGroupClientIds() != null) {
            HashSet<String> currentSgIds = new ObjectMapper().readValue(instanceEntity.getNeutronSecurityGroupClientIds(), new TypeReference<>() {
            });
            if (!currentSgIds.isEmpty() && !currentSgIds.containsAll(lstIdsToDetach)) {
                throw new AppException(new ErrorResponse("some security group not permission to access"));
            }
        }
        mapReq.put("securityGroupIds", lstIdsToDetach);

        return mapReq;
    }

    public Map<String, Object> resizeInstance() {
        Map<String, Object> req = new HashMap<>();
        String newFlavorId = this.getData().get("newFlavorId").toString();
        String currentFlavorId = this.getInstanceInfo().getInstance().getFlavorId();
        String instanceId = this.getInstanceInfo().getInstance().getId();
        if (StringUtils.isBlank(newFlavorId)) {
            throw new AppException(new ErrorResponse("flavor cannot be empty"));
        }

        if (newFlavorId.equals(currentFlavorId)) {
            throw new AppException(new ErrorResponse("Cannot change the flavor to match the current flavor"));
        }

        ResourceInstanceEntity resourceInstance = resourceInstanceRepository.findFirstByInstanceIdAndDeletedAtIsNull(instanceId);
        if (resourceInstance == null) {
            throw new AppException(new ErrorResponse("Error during get resource instance[%s]", instanceId));
        }

        FlavorInfo newFlavor = cmcCloudService.getFlavor(newFlavorId, this.getReqCtx().getToken(), this.getReqCtx().getRegionId());
        if (newFlavor.getFlavorId() == null) {
            throw new AppException(new ErrorResponse("flavor not found - flavorId[%s]", newFlavorId));
        }

        FlavorInfo currentFlavor = cmcCloudService.getFlavor(currentFlavorId, this.getReqCtx().getToken(), this.getReqCtx().getRegionId());
        if (currentFlavor.getFlavorId() == null) {
            throw new AppException(new ErrorResponse("current flavor not found - flavorId[%s]", currentFlavor));
        }

        List<String> computeIds = new ArrayList<>();
        List<ComputeEntity> listComputes = computeRepository.findAllByInstanceId(this.getInstanceInfo().getInstance().getId());
        DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(this.getInstanceInfo().getDatastoreCode());
        for (ComputeEntity compute : listComputes) {
            switch (datastoreSupport) {
                case REDIS -> computeIds.add(compute.getId());
                case MONGODB -> {
                    // not chane flavor of arbiter server
                    if (!compute.getRole().equalsIgnoreCase(Role.MongoDB.ARBITER.getName())) {
                        computeIds.add(compute.getId());
                    }
                }
            }
        }

        ResourceUsedReq resourceRequest = this.calculatorResourceRequest(currentFlavor, newFlavor);
        resourceService.checkAndSetResourceUsed(resourceRequest, this.getReqCtx());

        ResourceUsedRes resourceUsed = CommonUtils.toObject(resourceInstance.getResourceUsed(), ResourceUsedRes.class);
        if (resourceUsed == null) {
            throw new AppException(new ErrorResponse("Error during convert resource instance[%s]", instanceId));
        }

        int quantityOfServer = computeIds.size();
        if (datastoreSupport.equals(DatastoreSupport.MONGODB)
                && this.getInstanceInfo().getDatastoreModeCode().equals(DatastoreMode.Mongodb.REPLICA_SET.getCode())) {
            FlavorInfo flavorArbiter = cmcCloudService.getFlavor(
                    EnvConfig.OS_FLAVOR_INIT_MONGODB_ARBITER, this.getReqCtx().getToken(), this.getReqCtx().getRegionId());
            if (flavorArbiter.getFlavorId() == null) {
                throw new AppException(new ErrorResponse("Not found flavorId[%s] for arbiter", EnvConfig.OS_FLAVOR_INIT_MONGODB_ARBITER));
            }
            int cpuArbiter = flavorArbiter.getVCpus();
            int ramArbiter = flavorArbiter.getRam();
            int diskArbiter = flavorArbiter.getDisk();

            resourceUsed.setCpu(newFlavor.getVCpus() * quantityOfServer + cpuArbiter);
            resourceUsed.setRam(newFlavor.getRam() * quantityOfServer + ramArbiter);
            resourceUsed.setSystemDisk(newFlavor.getDisk() * quantityOfServer + diskArbiter);
        } else {
            resourceUsed.setCpu(newFlavor.getVCpus() * quantityOfServer);
            resourceUsed.setRam(newFlavor.getRam() * quantityOfServer);
            resourceUsed.setSystemDisk(newFlavor.getDisk() * quantityOfServer);
        }

        resourceInstance.setResourceUsed(CommonUtils.toJson(resourceUsed));
        resourceInstanceRepository.save(resourceInstance);

        req.put("newFlavorId", newFlavorId);
        req.put("computeIds", computeIds);

        return req;
    }

    public Map<String, Object> resizeVolume() {
        Map<String, Object> req = new HashMap<>();
        String volumeSizeStr = this.getData().get("newVolumeSize").toString();
        Integer currentVolumeSize = this.getInstanceInfo().getInstance().getVolumeSize();
        String instanceId = this.getInstanceInfo().getInstance().getId();

        ResourceInstanceEntity resourceInstance = resourceInstanceRepository.findFirstByInstanceIdAndDeletedAtIsNull(instanceId);
        if (resourceInstance == null) {
            throw new AppException(new ErrorResponse("Error during get resource instance[%s]", instanceId));
        }

        if (!NumberUtils.isDigits(volumeSizeStr)) {
            throw new AppException(new ErrorResponse("newVolumeSize must be a number"));
        }

        int newVolumeSize = Integer.parseInt(volumeSizeStr);
        if (newVolumeSize < Constant.MIN_VOLUME_SIZE || newVolumeSize > Constant.MAX_VOLUME_SIZE) {
            throw new AppException(new ErrorResponse("newVolumeSize must be greater than equal 20 and less than equal 32000"));
        }

        List<String> computeIds = new ArrayList<>();
        List<ComputeEntity> listComputes = computeRepository.findAllByInstanceId(this.getInstanceInfo().getInstance().getId());
        DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(this.getInstanceInfo().getDatastoreCode());

        if (newVolumeSize <= currentVolumeSize) {
            throw new AppException(new ErrorResponse("newVolumeSize[%s] must be greater than oldSize[%s]", newVolumeSize, currentVolumeSize));
        }

        for (ComputeEntity compute : listComputes) {
            switch (datastoreSupport) {
                case REDIS -> computeIds.add(compute.getId());
                case MONGODB -> {
                    if (!compute.getRole().equalsIgnoreCase(Role.MongoDB.ARBITER.getName())) {
                        computeIds.add(compute.getId());
                    }
                }
            }
        }

        // currentVolumeSize always less than newVolumeSize
        Integer resourceVolumeInstance = currentVolumeSize * computeIds.size() - newVolumeSize * computeIds.size();
        ResourceUsedReq resourceUsage = new ResourceUsedReq(0, 0, 0, resourceVolumeInstance);
        resourceService.checkAndSetResourceUsed(resourceUsage, this.getReqCtx());

        ResourceUsedRes resourceUsed = CommonUtils.toObject(resourceInstance.getResourceUsed(), ResourceUsedRes.class);
        if (resourceUsed == null) {
            throw new AppException(new ErrorResponse("Error during convert resource instance[%s]", instanceId));
        }
        resourceUsed.setVolumeGb(newVolumeSize * computeIds.size());
        resourceInstance.setResourceUsed(CommonUtils.toJson(resourceUsed));
        resourceInstanceRepository.save(resourceInstance);

        req.put("newVolumeSize", newVolumeSize);
        req.put("computeIds", computeIds);

        return req;
    }

    protected String genUrls3(S3StorageConfigDto storageConfigDto, String objectName, HttpMethod method) {
        AmazonS3 amazonS3 = S3Utils.buildAmazonS3(storageConfigDto);
        return S3Utils.genPresignedUrl(amazonS3, method, objectName);
    }

    protected Map<String, Object> getConfigFromGroup(String groupConfigId) {
        List<ConfigurationEntity> lstConfig = configurationRepository.findAllByGroupConfigurationId(groupConfigId);
        Map<String, Object> overridesConfig = new HashMap<>();
        for (ConfigurationEntity configParam : lstConfig) {
            overridesConfig.put(configParam.getParamName(), configParam.getParamValue());
        }

        return overridesConfig;
    }

    private ResourceUsedReq calculatorResourceRequest(FlavorInfo oldFlavor, FlavorInfo newFlavor) {
        int cpu = newFlavor.getVCpus() - oldFlavor.getVCpus();
        int ram = newFlavor.getRam() - oldFlavor.getRam();
        int disk = newFlavor.getDisk() - oldFlavor.getDisk();
        return new ResourceUsedReq(cpu, ram, disk, 0);
    }

}
