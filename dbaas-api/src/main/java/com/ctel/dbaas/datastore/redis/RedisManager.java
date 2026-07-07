package com.ctel.dbaas.datastore.redis;

import com.amazonaws.HttpMethod;
import com.ctel.dbaas.common.enums.DatastoreMode;
import com.ctel.dbaas.common.enums.InstanceStatus;
import com.ctel.dbaas.common.enums.Role;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.datastore.DatastoreActionAbstract;
import com.ctel.dbaas.dto.backup.S3StorageConfigDto;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.instance.InstanceInfo;
import com.ctel.dbaas.entity.dbaas.*;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.ComputeRepository;
import com.ctel.dbaas.repository.dbaas.GroupConfigurationRepository;
import com.ctel.dbaas.repository.dbaas.NetworkRepository;
import com.ctel.dbaas.repository.dbaas.VolumeRepository;
import com.ctel.dbaas.service.CmcCloudService;
import com.ctel.dbaas.utils.CommonUtils;
import com.ctel.dbaas.utils.CryptoUtils;
import com.ctel.dbaas.utils.DateUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Log4j2
@Component
public class RedisManager extends DatastoreActionAbstract {

    @Autowired
    private GroupConfigurationRepository groupConfigurationRepository;

    @Autowired
    private ComputeRepository computeRepository;

    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    private CmcCloudService cmcCloudService;

    @Autowired
    private VolumeRepository volumeRepository;

    private static final String MSG_NO_SUPPORT = "redis not support this action";

    @Override
    public Map<String, Object> startInstance() {
        InstanceInfo req = this.getInstanceInfo();
        Map<String, Object> res = new HashMap<>();
        GroupConfigurationEntity groupConfiguration = groupConfigurationRepository
                .findById(req.getInstance().getGroupConfigurationId())
                .orElseThrow(() -> new AppException(new ErrorResponse("Group config not found")));

        if (!groupConfiguration.isDefault() && !Objects.equals(groupConfiguration.getOrgId(), this.getReqCtx().getOrgId())) {
            throw new AppException(new ErrorResponse("Not permission to access this group config"));
        }

        Map<String, Object> configs = this.getConfigFromGroup(req.getInstance().getGroupConfigurationId());
        res.put("overridesConfig", configs);

        return res;
    }

    @Override
    public Map<String, Object> promoteSlaveToMaster() {
        //validate
        String masterId = (String) this.getData().remove("masterId");
        String slaveId = (String) this.getData().remove("slaveId");
        if (StringUtils.isBlank(masterId)) {
            throw new AppException(new ErrorResponse("masterId cannot be empty"));
        }

        if (StringUtils.isBlank(slaveId)) {
            throw new AppException(new ErrorResponse("slaveId cannot be empty"));
        }
        //

        Map<String, Object> res = new HashMap<>();
        InstanceEntity instance = this.getInstanceInfo().getInstance();
        if (!DatastoreMode.Redis.MASTER_SLAVE.getCode().equals(this.getInstanceInfo().getDatastoreModeCode())) {
            throw new AppException(new ErrorResponse("Mode of instance invalid to promote"));
        }

        if (!InstanceStatus.RUNNING.getStatus().equalsIgnoreCase(instance.getStatus())) {
            throw new AppException(new ErrorResponse("instance not ready to promote"));
        }

        ComputeEntity currentMaster = computeRepository.findFirstByInstanceIdAndRole(instance.getId(), Role.Redis.MASTER.getName());
        if (!currentMaster.getId().equals(masterId)) {
            throw new AppException(new ErrorResponse("Not found master with id[%s]", masterId));
        }

        List<ComputeEntity> lstSlave = computeRepository.findAllByInstanceIdAndRole(instance.getId(), Role.Redis.SLAVE.getName());
        List<String> slaveIds = new ArrayList<>(lstSlave.stream().map(ComputeEntity::getId).toList());
        if (!slaveIds.contains(slaveId)) {
            throw new AppException(new ErrorResponse("Not found slave with id[%s]", slaveId));
        }
        slaveIds.removeIf(slaveComputeId -> slaveComputeId.equals(slaveId));

        NetworkEntity networkSlaveToPromote = networkRepository.findFirstByComputeIdAndMode(slaveId, "system");
        if (networkSlaveToPromote == null) {
            throw new AppException(new ErrorResponse("Network of slaveId[%s] not found", slaveId));
        }
        String newMasterIp = networkSlaveToPromote.getIpAddress();
        if (newMasterIp == null || newMasterIp.isEmpty()) {
            throw new AppException(new ErrorResponse("Network of new master [%s] not found", slaveId));
        }

        slaveIds.add(masterId);
        res.put("masterComputeId", slaveId);
        res.put("slaveComputeIds", slaveIds);
        res.put("newMasterIp", newMasterIp);

        return res;
    }

    @SneakyThrows
    @Override
    public Map<String, Object> setPassword() {
        Map<String, Object> res = new HashMap<>();
        String password = (String) this.getData().get("password");
        if (password == null) {
            throw new AppException(new ErrorResponse("password cannot be empty"));
        }
        if (StringUtils.isBlank(password)) {
            throw new AppException(new ErrorResponse("password invalid"));
        }
        CommonUtils.validatePwd(password);
        res.put("password", password);

        return res;
    }

    @Override
    @SneakyThrows
    public Map<String, Object> createBackup() {
        Map<String, Object> res = new HashMap<>();
        String backupStrategyType = this.getData().get("backupStrategyType").toString();
        String name = this.getData().get("name").toString();
        String backupScheduleId = this.getData().get("backupScheduleId") != null ? this.getData().get("backupScheduleId").toString() : null;

        // validate request
        if (StringUtils.isBlank(backupStrategyType)) {
            throw new AppException(new ErrorResponse("backupStrategyType cannot be empty"));
        }
        if (StringUtils.isBlank(name)) {
            throw new AppException(new ErrorResponse("name cannot be empty"));
        }
        //

        BackupStrategyEntity backupStrategy = backupStrategyRepository
                .findFirstByTypeAndActiveIsTrue(backupStrategyType);

        if (backupStrategy == null) {
            throw new AppException(new ErrorResponse("No backup strategy is available"));
        }

        if ("S3".equals(backupStrategy.getType())) {
            String configJsonDecode = CryptoUtils.decrypt(
                    backupStrategy.getConfiguration(), EnvConfig.KEY_DECRYPT_CONFIG_BACKUP);
            S3StorageConfigDto storageConfigDto = CommonUtils.convert(
                    new JSONObject(configJsonDecode), S3StorageConfigDto.class);

            BackupEntity backup = new BackupEntity();
            backup.setInstanceId(this.getInstanceInfo().getInstance().getId());
            backup.setName(name);
            backup.setStatus(InstanceStatus.WAITING.getStatus());
            backup.setDatastoreVersionId(this.getInstanceInfo().getDatastoreVersionId());
            backup.setDatastoreVersion(this.getInstanceInfo().getDatastoreVersion());
            backup.setDatastoreMode(this.getInstanceInfo().getDatastoreModeCode());
            backup.setBackupStrategyId(backupStrategy.getId());
            backup.setBackupScheduleId(backupScheduleId);
            backup.setOrgId(this.getReqCtx().getOrgId());
            backup.setBackupMode(this.getInstanceInfo().getDatastoreModeCode());

            String fileName = this.getInstanceInfo().getDatastoreName() + "_" + backup.getName() + "_" +
                    DateUtils.toString(LocalDateTime.now(), DateUtils.yyyyMMddHHmmss) + "_" + this.getInstanceInfo().getDatastoreModeCode() + ".tar.gz";
            backup.setFileName(fileName);

            String objectName = this.getReqCtx().getOrgId() + "/" + fileName;
            String urlPut = this.genUrls3(storageConfigDto, objectName, HttpMethod.PUT);
            backup.setDatastoreCode(this.getInstanceInfo().getDatastoreCode());
            backupRepository.save(backup);

            res.put("backupId", backup.getId());
            res.put("url", urlPut);
            res.put("name", name);
        } else {
            throw new AppException(new ErrorResponse("This backup strategy type not supported yet"));
        }

        return res;
    }

//    @Override
//    public Map<String, Object> resizeInstance() {
//        Map<String, Object> req = new HashMap<>();
//        String newFlavorId = this.getData().get("newFlavorId").toString();
//        if (StringUtils.isBlank(newFlavorId)) {
//            throw new AppException(new ErrorResponse("flavor cannot be empty"));
//        }
//
//        FlavorInfo flavor = cmcCloudService.getFlavor(newFlavorId, this.getReqCtx().getToken(), this.getReqCtx().getRegionId());
//        if (flavor.getFlavorId() == null) {
//            throw new AppException(new ErrorResponse("flavor not found - flavorId[%s]", newFlavorId));
//        }
//
//        List<String> computeIds = new ArrayList<>();
//        List<ComputeEntity> listComputes = computeRepository.findAllByInstanceId(this.getInstanceInfo().getInstance().getId());
//        for (ComputeEntity compute : listComputes) {
//            if (!compute.getFlavorId().equals(newFlavorId)) {
//                computeIds.add(compute.getId());
//            }
//        }
//
//        req.put("newFlavorId", newFlavorId);
//        req.put("computeIds", computeIds);
//
//        return req;
//    }
//
//    @Override
//    public Map<String, Object> resizeVolume() {
//        Map<String, Object> req = new HashMap<>();
//        String volumeSizeStr = this.getData().get("newVolumeSize").toString();
//
//        if (!NumberUtils.isDigits(volumeSizeStr)) {
//            throw new AppException(new ErrorResponse("newVolumeSize must be a number"));
//        }
//
//        Integer newVolumeSize = Integer.parseInt(volumeSizeStr);
//        if (newVolumeSize < Constant.MIN_VOLUME_SIZE || newVolumeSize > Constant.MAX_VOLUME_SIZE) {
//            throw new AppException(new ErrorResponse("newVolumeSize must be greater than equal 20 and less than equal 32000"));
//        }
//
//        List<String> computeIds = new ArrayList<>();
//        List<ComputeEntity> listComputes = computeRepository.findAllByInstanceId(this.getInstanceInfo().getInstance().getId());
//        for (ComputeEntity compute : listComputes) {
//            VolumeEntity volume = volumeRepository.findFirstByComputeId(compute.getId());
//            if (volume == null) {
//                throw new AppException(new ErrorResponse("not found volume of compute - computeId[%s]", compute.getId()));
//            }
//            if (newVolumeSize <= volume.getSize()) {
//                throw new AppException(new ErrorResponse("newVolumeSize[%s] must be greater than oldSize[%s]", newVolumeSize, volume.getSize()));
//            }
//            computeIds.add(compute.getId());
//        }
//
//        req.put("newVolumeSize", newVolumeSize);
//        req.put("computeIds", computeIds);
//
//        return req;
//    }

    @Override
    public Map<String, Object> getListUser() {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getListDatabase() {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> dbAction() {
        throw new AppException(new ErrorResponse(MSG_NO_SUPPORT));
    }
}
