package com.ctel.dbaas.datastore.mongodb;

import com.amazonaws.HttpMethod;
import com.ctel.dbaas.common.enums.DbAction;
import com.ctel.dbaas.common.enums.InstanceStatus;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.datastore.DatastoreActionAbstract;
import com.ctel.dbaas.dto.backup.S3StorageConfigDto;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.entity.dbaas.*;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.DataReceiveAgentRepository;
import com.ctel.dbaas.repository.dbaas.VolumeRepository;
import com.ctel.dbaas.service.CmcCloudService;
import com.ctel.dbaas.utils.CommonUtils;
import com.ctel.dbaas.utils.CryptoUtils;
import com.ctel.dbaas.utils.DateUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
@Component
public class MongodbManager extends DatastoreActionAbstract {
    private final DataReceiveAgentRepository dataReceiveAgentRepository;

    @Autowired
    private CmcCloudService cmcCloudService;

    @Autowired
    private VolumeRepository volumeRepository;

    public MongodbManager(DataReceiveAgentRepository dataReceiveAgentRepository) {
        this.dataReceiveAgentRepository = dataReceiveAgentRepository;
    }

    @Override
    public Map<String, Object> startInstance() {
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> promoteSlaveToMaster() {
        throw new AppException(new ErrorResponse("mongodb not support this action"));
    }

    @Override
    public Map<String, Object> setPassword() {
        throw new AppException(new ErrorResponse("mongodb not support this action"));
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
        // end validate

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

            String fileName = this.getInstanceInfo().getDatastoreCode() + "_" + backup.getName() + "_" +
                    DateUtils.toString(LocalDateTime.now(), DateUtils.yyyyMMddHHmmss) + "_" + this.getInstanceInfo().getDatastoreCode() + ".tar.gz";
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
//        List<String> computeIds = new ArrayList<>();
//        List<ComputeEntity> listComputes = computeRepository.findAllByInstanceId(this.getInstanceInfo().getInstance().getId());
//        for (ComputeEntity compute : listComputes) {
//            // not chane flavor of arbiter server
//            if (!compute.getFlavorId().equals(newFlavorId) && !compute.getRole().equalsIgnoreCase(Role.MongoDB.ARBITER.getName())) {
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
//
//            if (newVolumeSize <= volume.getSize()) {
//                throw new AppException(new ErrorResponse("newVolumeSize[%s] must be greater than oldSize[%s]", newVolumeSize, volume.getSize()));
//            }
//
//            if (!compute.getRole().equalsIgnoreCase(Role.MongoDB.ARBITER.getName())) {
//                computeIds.add(compute.getId());
//            }
//
//        }
//
//        req.put("newVolumeSize", newVolumeSize);
//        req.put("computeIds", computeIds);
//
//        return req;
//    }

    @SneakyThrows
    @Override
    public Map<String, Object> getListUser() {
        JSONArray response = new JSONArray();
        try {
            DataReceiveAgentEntity dataReceiveAgent = dataReceiveAgentRepository
                    .findFirstByInstanceId(this.getInstanceInfo().getInstance().getId());

            log.info("dataReceiveAgent => [{}]", dataReceiveAgent);
            if (dataReceiveAgent != null) {
                response = new JSONArray(dataReceiveAgent.getUsersInfo());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return Collections.singletonMap("users", response);
    }

    @SneakyThrows
    @Override
    public Map<String, Object> getListDatabase() {
        JSONArray response = new JSONArray();
        try {
            DataReceiveAgentEntity dataReceiveAgent = dataReceiveAgentRepository
                    .findFirstByInstanceId(this.getInstanceInfo().getInstance().getId());

            log.info("dataReceiveAgent => [{}]", dataReceiveAgent);
            if (dataReceiveAgent != null) {
                response = new JSONArray(dataReceiveAgent.getDatabasesInfo());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return Collections.singletonMap("databases", response);
    }

    @SneakyThrows
    @Override
    public Map<String, Object> dbAction() {
        String command = (String) this.getData().get("command");
        Map<String, Object> body = (Map<String, Object>) this.getData().get("body");
        DbAction.Mongodb mongoAction = DbAction.Mongodb.get(command);
        if (mongoAction == null) {
            throw new AppException(new ErrorResponse("MongoDB action invalid"));
        }

        if (body == null) {
            throw new AppException(new ErrorResponse("MongoDB request invalid"));
        }

        MongoDBAction mongoDBAction = new MongoDBAction((Map<String, Object>) this.getData().get("body"));
        Method method = mongoDBAction.getClass().getMethod(mongoAction.getMethodName());
        Map<String, Object> reqMap = (Map<String, Object>) method.invoke(mongoDBAction);
        if (reqMap == null) {
            reqMap = new HashMap<>();
        }

        Map<String, Object> mapRequest = new HashMap<>();
        mapRequest.put("command", mongoAction.getServiceId());
        mapRequest.put("body", reqMap);

        return mapRequest;
    }
}
