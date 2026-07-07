package com.ctel.dbaas.service;

import com.amazonaws.services.s3.AmazonS3;
import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.repository.dbaas.*;
import com.ctel.dbaas.utils.S3Utils;
import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.common.enums.InstanceAction;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.utils.CryptoUtils;
import com.ctel.dbaas.dto.backup.*;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.entity.dbaas.*;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.projection.DatastoreVersionInfo;
import com.ctel.dbaas.repository.dbaas.projection.backup.BackupDropdown;
import com.ctel.dbaas.repository.dbaas.projection.instance.InstanceInfoProjection;
import com.ctel.dbaas.utils.CommonUtils;
import com.ctel.dbaas.utils.DateUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class BackupRestoreService {

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private DatastoreVersionRepository datastoreVersionRepository;

    @Autowired
    private BackupRepository backupRepository;

    @Autowired
    private BackupStrategyRepository backupStrategyRepository;

    @Autowired
    private BackupScheduleRepository backupScheduleRepository;

    @Autowired
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private ActionService actionService;

    public Page<BackupRes> getListBackup(List<String> datastoreCodes, String instanceId, String name, Pageable pageable, RequestInfo reqCtx) {
        Page<BackupEntity> backupEntities;
        if (StringUtils.isNotBlank(instanceId)) {
            backupEntities = backupRepository.findAllByInstanceIdAndOrgIdAndDatastoreCodeIn(
                    instanceId, reqCtx.getOrgId(), datastoreCodes, pageable);
        } else {
            backupEntities = StringUtils.isBlank(name) ?
                    backupRepository.findAllByOrgIdAndDatastoreCodeIn(reqCtx.getOrgId(), datastoreCodes, pageable) :
                    backupRepository.findAllByDatastoreCodeInAndNameAndOrgId(datastoreCodes, name, reqCtx.getOrgId(), pageable);
        }

        return backupEntities.map(this::toRes);
    }

    public List<BackupDropdown> getListBackupDropdown(List<String> datastoreCodes, String backupName, String orgId) {
        if (StringUtils.isNotBlank(backupName)) {
            return backupRepository.findAllByOrgIdAndDatastoreCodeInAndName(orgId, datastoreCodes, backupName);
        }
        return backupRepository.findAllByOrgIdAndDatastoreCodeIn(orgId, datastoreCodes);
    }

    private BackupRes toRes(BackupEntity entity) {
        BackupRes dto = new BackupRes();
        dto.setId(entity.getId());
        dto.setBackupName(entity.getName());

        InstanceInfoProjection instance = instanceRepository.getInstanceNameById(entity.getInstanceId());
        if (instance != null) {
            dto.setInstanceId(instance.getId());
            dto.setInstanceName(instance.getName());
        }

        dto.setSize(entity.getSize());
        dto.setStatus(entity.getStatus().toUpperCase());
        dto.setBackupStartAt(entity.getBackupStartAt() != null ? entity.getBackupStartAt().toString() : "");
        dto.setBackupEndAt(entity.getBackupEndAt() != null ? entity.getBackupEndAt().toString() : "");
        dto.setCreated(entity.getCreatedAt().toString());

        backupStrategyRepository.findById(entity.getBackupStrategyId()).ifPresent(backupStrategy -> dto.setBackupStrategyType(backupStrategy.getType()));
        DatastoreVersionInfo datastoreVersionInfo = datastoreVersionRepository.getByDatastoreVersionId(entity.getDatastoreVersionId());
        if (datastoreVersionInfo != null) {
            dto.setDatastoreName(datastoreVersionInfo.getDatastoreName());
            dto.setDatastoreCode(datastoreVersionInfo.getDatastoreCode());
            dto.setDatastoreVersion(datastoreVersionInfo.getDatastoreVersion());
        }

        return dto;
    }

    @SneakyThrows
    public void deleteBackups(List<String> backupIds, String orgId) {
        for (String backupId : backupIds) {
            boolean existBackup = backupRepository.existsByIdAndOrgId(backupId, orgId);
            if (!existBackup) {
                log.warn("not exist backup with backupId[{}] - orgId[{}]", backupId, orgId);
                throw new AppException(new ErrorResponse("BackupId[%s] not exist", backupId));
            }
        }

        for (String backupId : backupIds) {
            BackupEntity backup = backupRepository.findFirstByIdAndOrgId(backupId, orgId)
                    .orElseThrow(() -> new AppException(new ErrorResponse("Backup not found")));
            BackupStrategyEntity backupStrategy = backupStrategyRepository
                    .findById(backup.getBackupStrategyId())
                    .orElseThrow(() -> new AppException(new ErrorResponse("Backup strategy not found")));

            if (StringUtils.isNotBlank(backup.getFileName())) {
                String jsonConfig = CryptoUtils.decrypt(backupStrategy.getConfiguration(),
                        EnvConfig.KEY_DECRYPT_CONFIG_BACKUP);
                if (backupStrategy.getType().equals("S3")) {
                    S3StorageConfigDto s3Config = CommonUtils.convert(new JSONObject(jsonConfig), S3StorageConfigDto.class);
                    AmazonS3 amazonS3 = S3Utils.buildAmazonS3(s3Config);
                    String bucketName = EnvConfig.S3_BUCKET_NAME;
                    String objectKey = orgId + "/" + backup.getFileName();
                    amazonS3.deleteObject(bucketName, objectKey);
                }
            }

            backupRepository.delete(backup);
        }

    }

    public void createBackupSchedule(CreateBackupScheduleReq req, RequestInfo reqCtx) {
        LocalDateTime currentTime = LocalDateTime.now();
        InstanceEntity instance = instanceRepository.findByIdAndOrgIdAndProjectIdAndDeletedAtIsNull(
                        req.getInstanceId(), reqCtx.getOrgId(), reqCtx.getProjectId())
                .orElseThrow(() -> new AppException(new ErrorResponse("instance not found")));

        DatastoreVersionInfo datastoreVersionInfo = datastoreVersionRepository.getByDatastoreVersionId(instance.getDatastoreVersionId());
        BackupScheduleEntity backupSchedule = backupScheduleRepository.findFirstByInstanceIdAndProjectIdAndOrgId(
                instance.getId(), reqCtx.getProjectId(), reqCtx.getOrgId());
        if (backupSchedule != null) {
            throw new AppException(new ErrorResponse("This instance has ready exist backup schedule"));
        }

        LocalDateTime nextBackupTime = this.calculateNextBackupTime(req.getHour(), req.getMinute(), req.getSecond(), req.getInterval(), req.getTimeZone());

        backupSchedule = new BackupScheduleEntity();
        backupSchedule.setInstanceId(instance.getId());
        backupSchedule.setHour(req.getHour());
        backupSchedule.setMinute(req.getMinute());
        backupSchedule.setSecond(req.getSecond());
        backupSchedule.setTimeZone(req.getTimeZone());
        backupSchedule.setIntervalNum(req.getInterval());
        backupSchedule.setIntervalType(req.getIntervalType());
        backupSchedule.setNextBackupTime(nextBackupTime);
        backupSchedule.setActive(true);
        backupSchedule.setStatus("WAIT");
        backupSchedule.setKeepRecordBackup(req.getKeepRecordBackup());
        backupSchedule.setProjectId(reqCtx.getProjectId());
        backupSchedule.setOrgId(reqCtx.getOrgId());
        backupSchedule.setUserId(reqCtx.getUserId());
        backupSchedule.setDatastoreCode(datastoreVersionInfo.getDatastoreCode());
        backupScheduleRepository.save(backupSchedule);

        boolean backupNow = this.checkToInitBackupNow(currentTime, nextBackupTime);
        if (backupNow) {
            this.executeAutoBackup(new TriggerBackupRequest(backupSchedule.getId(),
                    DateUtils.toString(nextBackupTime, DateUtils.yyyy_MM_dd_HH_mm_ss)));
        }
    }

    public void updateBackupSchedule(UpdateBackupScheduleReq req) {
        LocalDateTime currentTime = LocalDateTime.now();
        BackupScheduleEntity backupSchedule = backupScheduleRepository.findFirstByIdAndProjectIdAndOrgId(
                        req.getId(), GrpcCtx.getReqCtx().getProjectId(), GrpcCtx.getReqCtx().getOrgId())
                .orElseThrow(() -> new AppException(new ErrorResponse("Backup schedule not found")));

        if (!Objects.equals(req.getHour(), backupSchedule.getHour()) ||
                !Objects.equals(req.getMinute(), backupSchedule.getMinute()) ||
                !Objects.equals(req.getSecond(), backupSchedule.getSecond())) {
            backupSchedule.setStatus("WAIT");
        }

        LocalDateTime nextBackupTime = this.calculateNextBackupTime(req.getHour(), req.getMinute(), req.getSecond(), req.getIntervalNum(), backupSchedule.getTimeZone());

        backupSchedule.setHour(req.getHour());
        backupSchedule.setMinute(req.getMinute());
        backupSchedule.setSecond(req.getSecond());
        backupSchedule.setTimeZone(req.getTimeZone());
        backupSchedule.setIntervalNum(req.getIntervalNum());
        backupSchedule.setIntervalType(req.getIntervalType());
        backupSchedule.setKeepRecordBackup(req.getKeepRecordBackup());
        backupSchedule.setNextBackupTime(nextBackupTime);
        backupSchedule.setUserId(GrpcCtx.getReqCtx().getUserId());
        backupScheduleRepository.save(backupSchedule);

        boolean backupNow = this.checkToInitBackupNow(currentTime, nextBackupTime);
        if (backupNow) {
            this.executeAutoBackup(new TriggerBackupRequest(backupSchedule.getId(),
                    DateUtils.toString(nextBackupTime, DateUtils.yyyy_MM_dd_HH_mm_ss)));
        }
    }

    public void deleteBackupSchedule(List<String> backupScheduleIds) {
        List<BackupScheduleEntity> backupScheduleToDel = new ArrayList<>();
        for (String backupScheduleId : backupScheduleIds) {
            BackupScheduleEntity backupSchedule = backupScheduleRepository.findFirstByIdAndProjectIdAndOrgId(
                            backupScheduleId, GrpcCtx.getReqCtx().getProjectId(), GrpcCtx.getReqCtx().getOrgId())
                    .orElseThrow(() -> new AppException(new ErrorResponse("Backup schedule not found")));
            backupScheduleToDel.add(backupSchedule);
        }
        backupScheduleRepository.deleteAll(backupScheduleToDel);
    }

    public BackupScheduleRes getBackupSchedule(String backupScheduleId) {
        BackupScheduleEntity backupSchedule = backupScheduleRepository.findFirstByIdAndProjectIdAndOrgId(
                        backupScheduleId, GrpcCtx.getReqCtx().getProjectId(), GrpcCtx.getReqCtx().getOrgId())
                .orElseThrow(() -> new AppException(new ErrorResponse("Backup schedule not found")));

        return this.toRes(backupSchedule);
    }

    public Page<BackupScheduleRes> getListBackupSchedule(String datastoreCode, Pageable pageable) {
        Page<BackupScheduleEntity> backupSchedules = backupScheduleRepository.findAllByOrgIdAndProjectIdAndDatastoreCode(
                GrpcCtx.getReqCtx().getOrgId(), GrpcCtx.getReqCtx().getProjectId(), datastoreCode, pageable);
        return backupSchedules.map(this::toRes);
    }

    public void executeAutoBackup(TriggerBackupRequest req) {
        log.info("Start auto backup - request[{}]", req);
        BackupScheduleEntity backupSchedule = backupScheduleRepository.findById(req.getScheduleBackupId()).orElse(null);
        if (backupSchedule != null) {
            LocalDateTime currentNextBackupTime = backupSchedule.getNextBackupTime().withNano(0);
            LocalDateTime timeExec = DateUtils.convertToLocalDateTime(req.getTimeExec(), DateUtils.yyyy_MM_dd_HH_mm_ss).withNano(0);

            String intervalType = CommonUtils.calculateIntervalType(backupSchedule.getHour(), backupSchedule.getMinute(), backupSchedule.getSecond());
            log.info("intervalType[{}] - currentNextBackupTime[{}]", intervalType, currentNextBackupTime);
            if ("DAY".equals(intervalType)) {
                LocalDateTime nextBackupTime = backupSchedule.getNextBackupTime().plusDays(backupSchedule.getIntervalNum());
                backupSchedule.setNextBackupTime(nextBackupTime);
                log.info("intervalType is DAY => newNextBackupTime[{}]", nextBackupTime);
                backupScheduleRepository.save(backupSchedule);
                log.info("after save => backupSchedule[{}]", backupSchedule);
            }

            long seconds = Duration.between(currentNextBackupTime, timeExec).getSeconds();
            if (seconds == 0) {
                backupSchedule.setStatus("QUEUED");
                backupScheduleRepository.save(backupSchedule);

                long remainSecondsExec = Duration.between(LocalDateTime.now(), currentNextBackupTime).getSeconds();
                String orgId = backupSchedule.getOrgId();
                String projectId = backupSchedule.getProjectId();
                scheduledExecutorService.schedule(() -> this.startAutoBackup(backupSchedule.getId(),
                        backupSchedule.getInstanceId(), orgId, projectId), remainSecondsExec, TimeUnit.SECONDS);
            }
        }
    }

    public void startAutoBackup(String backupScheduleId, String instanceId, String orgId, String projectId) {
        BackupScheduleEntity backupSchedule = backupScheduleRepository.findById(backupScheduleId).orElse(null);
        if (backupSchedule == null || !"QUEUED".equals(backupSchedule.getStatus())) {
            return;
        }

        backupSchedule.setStatus("WAIT");
        backupScheduleRepository.save(backupSchedule);

        InstanceEntity instance = instanceRepository.findById(instanceId).orElse(null);
        if (instance != null) {
            try {
                RequestInfo requestInfo = new RequestInfo();
                requestInfo.setOrgId(orgId);
                requestInfo.setProjectId(projectId);

                Map<String, Object> reqData = new HashMap<>();
                reqData.put("backupStrategyType", "S3");
                reqData.put("name", instance.getName() + "-auto-backup-" + CommonUtils.generateString(4) + "-" +
                        DateUtils.toString(LocalDateTime.now(), DateUtils.yyyyMMdd));
                reqData.put("backupScheduleId", backupScheduleId);
                actionService.executeAction(instance.getId(), InstanceAction.CREATE_BACKUP, reqData, requestInfo);

                backupSchedule.setLastBackupTime(LocalDateTime.now());
                backupScheduleRepository.save(backupSchedule);

            } catch (Exception e) {
                log.error("startAutoBackup error => [{}]", e.getMessage());
            }
        }
    }

    public void deleteRotateBackupRecord(String backupId) {
        BackupEntity backup = backupRepository.findById(backupId).orElse(null);
        if (backup != null && backup.getBackupScheduleId() != null && Constant.COMPLETED.equalsIgnoreCase(backup.getStatus())) {
            BackupScheduleEntity backupSchedule = backupScheduleRepository.findById(backup.getBackupScheduleId()).orElse(null);
            if (backupSchedule != null) {
                Integer keepRecord = backupSchedule.getKeepRecordBackup();
                List<BackupDropdown> idsToKeep = backupRepository.getBackupIdsToKeep(backupSchedule.getId(),
                        backupSchedule.getOrgId(), Constant.COMPLETED, keepRecord);
                List<String> lstBackupIdsToKeep = idsToKeep.parallelStream().map(BackupDropdown::getId).toList();
                log.info("deleteRotateBackup[{}]:: list backupIds to keep => {}", backupId, lstBackupIdsToKeep);

                List<String> lstIdsToDelete = backupRepository.findAllByInstanceIdAndIdNotInAndBackupScheduleIdIsNotNull(backupSchedule.getInstanceId(), lstBackupIdsToKeep, BackupDropdown.class)
                        .stream().map(BackupDropdown::getId).toList();
                if (!lstIdsToDelete.isEmpty()) {
                    log.info("deleteRotateBackup[{}]:: list backupIds to delete => {}", backupId, lstIdsToDelete);
                    this.deleteBackups(lstIdsToDelete, backup.getOrgId());
                }
            }
        }
    }

    private BackupScheduleRes toRes(BackupScheduleEntity entity) {
        BackupScheduleRes dto = new BackupScheduleRes();
        dto.setId(entity.getId());
        dto.setInstanceId(entity.getInstanceId());

        InstanceInfoProjection instanceDropdownRes = instanceRepository.getInstanceNameById(entity.getInstanceId());
        dto.setInstanceName(instanceDropdownRes != null ? instanceDropdownRes.getName() : "");

        dto.setHour(entity.getHour());
        dto.setMinute(entity.getMinute());
        dto.setSecond(entity.getSecond());
        dto.setTimeZone(entity.getTimeZone());
        dto.setIntervalNum(entity.getIntervalNum());
        dto.setIntervalType(entity.getIntervalType());
        dto.setKeepRecordBackup(entity.getKeepRecordBackup());
        dto.setLastBackupTime(entity.getLastBackupTime() != null ? entity.getLastBackupTime().toString() : "");
        dto.setNextBackupTime(entity.getNextBackupTime() != null ? entity.getNextBackupTime().toString() : "");

        dto.setCreatedAt(entity.getCreatedAt().toString());
        dto.setUpdatedAt(entity.getUpdatedAt().toString());

        return dto;
    }

    private LocalDateTime calculateNextBackupTime(Integer hour, Integer minute, Integer second, Integer intervalNum, String timeZone) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime timeTriggerBackup = DateUtils.convertToUTC(
                LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, currentDateTime.getMinute(), currentDateTime.getSecond())), timeZone);

        LocalDateTime nextBackupTime;
        LocalDateTime backupTime;

        if (intervalNum == null) {
            intervalNum = 1;
        }

        String intervalType = CommonUtils.calculateIntervalType(timeTriggerBackup.getHour(), timeTriggerBackup.getMinute(), timeTriggerBackup.getSecond());
        if (intervalType.equals("DAY")) {
            backupTime = currentDateTime.withHour(timeTriggerBackup.getHour()).withMinute(timeTriggerBackup.getMinute()).withSecond(timeTriggerBackup.getSecond()).withNano(0);
            LocalTime timeBackup = LocalTime.of(timeTriggerBackup.getHour(), timeTriggerBackup.getMinute(), 0);
            LocalTime timeCurrent = LocalTime.of(currentDateTime.getHour(), currentDateTime.getMinute(), 0);

            if (timeCurrent.isBefore(timeBackup) || timeBackup.equals(timeCurrent)) {
                System.out.println("Backup in day");
                nextBackupTime = currentDateTime.withHour(timeTriggerBackup.getHour())
                        .withMinute(minute)
                        .withSecond(second)
                        .withNano(Constant.MAX_NANO_SECOND);
            } else {
                System.out.println("Backup in next " + intervalNum + " day");
                nextBackupTime = backupTime.plusDays(intervalNum);
            }
//            if (timeTriggerBackup.getHour() >= currentDateTime.getHour()) {
//                if (timeTriggerBackup.getMinute() >= currentDateTime.getMinute()) {
//                    // thời gian backup trong ngày
//                    nextBackupTime = currentDateTime.withHour(timeTriggerBackup.getHour()).withMinute(minute).withSecond(second).withNano(Constant.MAX_NANO_SECOND);
//                } else {
//                    // + n ngày tiếp theo
//                    nextBackupTime = backupTime.plusDays(intervalNum);
//                }
//            } else {
//                // + n ngày tiếp theo
//                nextBackupTime = backupTime.plusDays(intervalNum);
//            }

//            case "HOUR" -> {
//                nextBackupTime = currentTime.withMinute(minute).withSecond(second).withNano(0);
//                backupTime = currentTime.withMinute(minute).withSecond(second).withNano(0);
//                LocalDateTime backupTimeMaxNanoSecond = currentTime.withSecond(second).withNano(Constant.MAX_NANO_SECOND);
//                if (backupTime.isBefore(backupTimeMaxNanoSecond)) {
//                    nextBackupTime = backupTime.plusHours(intervalNum);
//                }
//            }
//            case "MINUTE" -> {
//                nextBackupTime = currentTime.withSecond(second).withNano(0);
//                backupTime = currentTime.withSecond(second).withNano(0);
//                LocalDateTime currentTimeWithoutNanoSecond = currentTime.withSecond(second).withNano(Constant.MAX_NANO_SECOND);
//                if (backupTime.isBefore(currentTimeWithoutNanoSecond)) {
//                    nextBackupTime = backupTime.plusMinutes(intervalNum);
//                }
//            }
        } else {
            throw new AppException(new ErrorResponse("intervalType invalid"));
        }

        return nextBackupTime;
    }

    private boolean checkToInitBackupNow(LocalDateTime currentTime, LocalDateTime nextBackupTime) {
        LocalDateTime currentTimeWithoutSecond = currentTime.withSecond(0).withNano(0);
        LocalDateTime nextBackupTimeWithoutSecond = nextBackupTime.withSecond(0).withNano(0);
        return currentTimeWithoutSecond.isEqual(nextBackupTimeWithoutSecond);
    }

}
