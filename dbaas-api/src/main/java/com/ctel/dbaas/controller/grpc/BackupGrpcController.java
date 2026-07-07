package com.ctel.dbaas.controller.grpc;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.dto.backup.BackupRes;
import com.ctel.dbaas.dto.backup.BackupScheduleRes;
import com.ctel.dbaas.dto.backup.CreateBackupScheduleReq;
import com.ctel.dbaas.dto.backup.UpdateBackupScheduleReq;
import com.ctel.dbaas.common.enums.DatastoreSupport;
import com.ctel.dbaas.repository.dbaas.projection.backup.BackupDropdown;
import com.ctel.dbaas.service.BackupRestoreService;
import com.ctel.dbaas.utils.CommonUtils;
import grpc.model.dbaas_service.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@GrpcService
@Log4j2
public class BackupGrpcController extends BackupServiceGrpc.BackupServiceImplBase {

    @Autowired
    private BackupRestoreService backupRestoreService;

    @Override
    public void getListBackup(GetListBackupRequest request, StreamObserver<GetListBackupResponse> responseObserver) {
        try {
            log.info("=== getListBackup params => [{}]", request);
            Pageable pageable = CommonUtils.convertToPageable(request.getPage(), request.getSize());
            List<String> datastoreCodes = DatastoreSupport.validateDatastoreCodes(request.getDatastoreCode());
            Page<BackupRes> backupRes = backupRestoreService
                    .getListBackup(datastoreCodes, request.getInstanceId(), request.getQuery(), pageable, GrpcCtx.getReqCtx());

            List<BackupInfo> result = backupRes.getContent().stream().map(backup ->
                    BackupInfo.newBuilder()
                            .setBackupId(backup.getId())
                            .setBackupName(backup.getBackupName())
                            .setDatastoreName(backup.getDatastoreName())
                            .setDatastoreCode(backup.getDatastoreCode())
                            .setDatastoreVersion(backup.getDatastoreVersion())
                            .setInstanceId(backup.getInstanceId())
                            .setInstanceName(backup.getInstanceName())
                            .setSize(Math.toIntExact(backup.getSize()))
                            .setBackupStrategyType(backup.getBackupStrategyType())
                            .setStatus(backup.getStatus().toUpperCase())
                            .setBackupStartAt(backup.getBackupStartAt())
                            .setBackupEndAt(backup.getBackupEndAt())
                            .setCreated(backup.getCreated())
                            .build()
            ).toList();

            responseObserver.onNext(GetListBackupResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(ListBackup.newBuilder()
                            .addAllDocs(result)
                            .setPage(backupRes.getPageable().getPageNumber() + 1)
                            .setSize(backupRes.getPageable().getPageSize())
                            .setTotal((int) backupRes.getTotalElements())
                            .setTotalPage(backupRes.getTotalPages())
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetListBackupResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .setData(ListBackup.newBuilder()
                            .addAllDocs(new ArrayList<>())
                            .build()
                    )
                    .build());
        } finally {
//            RequestCtx.clear();
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getListBackupDropdown(GetListBackupDropdownRequest request, StreamObserver<GetListBackupDropdownResponse> responseObserver) {
        try {
            log.info("getListBackupDropdown params => [{}]", request);
            List<String> datastoreCodes = DatastoreSupport.validateDatastoreCodes(request.getDatastoreCode());

            List<BackupDropdown> lstBackupRes = backupRestoreService
                    .getListBackupDropdown(datastoreCodes, request.getBackupName(), GrpcCtx.getReqCtx().getOrgId());

            List<grpc.model.dbaas_service.BackupDropdown> result = lstBackupRes.stream().map(backup ->
                    grpc.model.dbaas_service.BackupDropdown.newBuilder()
                            .setId(backup.getId())
                            .setName(backup.getName())
                            .setDatastoreCode(backup.getDatastoreCode())
                            .build()
            ).toList();

            responseObserver.onNext(GetListBackupDropdownResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(ListBackupDropdown.newBuilder()
                            .addAllDocs(result)
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetListBackupDropdownResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .setData(ListBackupDropdown.newBuilder()
                            .addAllDocs(new ArrayList<>())
                            .build()
                    )
                    .build());
        } finally {
//            RequestCtx.clear();
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteBackup(DeleteBackupRequest request, StreamObserver<CommonResponse> responseObserver) {
        try {
            log.info("deleteBackup params => [{}]", request);
            List<String> backupIds = request.getBackupIdsList().stream().map(BackupIds::getBackupId).toList();

            backupRestoreService.deleteBackups(backupIds, GrpcCtx.getReqCtx().getOrgId());
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .build());
        } finally {
//            RequestCtx.clear();
            responseObserver.onCompleted();
        }
    }

    @Override
    public void createBackupSchedule(CreateBackupScheduleRequest request, StreamObserver<CommonResponse> responseObserver) {
        try {
            log.info("createBackupSchedule params => [{}]", request);
            CreateBackupScheduleReq req = new CreateBackupScheduleReq();
            req.setInstanceId(request.getInstanceId());
            req.setHour(request.getHour());
            req.setMinute(request.getMinute());
            req.setSecond(0);
            req.setInterval(request.getInterval());
            req.setKeepRecordBackup(request.getKeepRecordBackup());
            req.setTimeZone(request.getTimeZone());
            req.validate();

            backupRestoreService.createBackupSchedule(req, GrpcCtx.getReqCtx());
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .build());
        } finally {
//            RequestCtx.clear();
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getBackupSchedule(GetBackupScheduleRequest request, StreamObserver<GetBackupScheduleResponse> responseObserver) {
        try {
            log.info("getBackupSchedule params => [{}]", request);
            BackupScheduleRes bk = backupRestoreService.getBackupSchedule(request.getId());
            BackupSchedule backupSchedule = this.toResGrpc(bk);

            responseObserver.onNext(GetBackupScheduleResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(backupSchedule)
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetBackupScheduleResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .build());
        } finally {
//            RequestCtx.clear();
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getListBackupSchedule(GetListBackupScheduleRequest request, StreamObserver<GetListBackupScheduleResponse> responseObserver) {
        try {
            log.info("getListBackupSchedule params => [{}]", request);
            Pageable pageable = CommonUtils.convertToPageable(request.getPage(), request.getSize());
            DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(request.getDatastoreCode());
            Page<BackupScheduleRes> backupScheduleRes = backupRestoreService.getListBackupSchedule(datastoreSupport.getCode(), pageable);

            List<BackupSchedule> result = backupScheduleRes.getContent().stream().map(this::toResGrpc).toList();
            responseObserver.onNext(GetListBackupScheduleResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(ListBackupSchedule.newBuilder()
                            .addAllDocs(result)
                            .setPage(backupScheduleRes.getPageable().getPageNumber() + 1)
                            .setSize(backupScheduleRes.getPageable().getPageSize())
                            .setTotal((int) backupScheduleRes.getTotalElements())
                            .setTotalPage(backupScheduleRes.getTotalPages())
                            .build()
                    )
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetListBackupScheduleResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .setData(ListBackupSchedule.newBuilder()
                            .addAllDocs(new ArrayList<>())
                            .build()
                    )
                    .build());
        } finally {
//            RequestCtx.clear();
            responseObserver.onCompleted();
        }
    }

    private BackupSchedule toResGrpc(BackupScheduleRes bk) {
        BackupSchedule.Builder backupSchedule = BackupSchedule.newBuilder();
        backupSchedule.setId(bk.getId());
        backupSchedule.setInstanceId(bk.getInstanceId());
        backupSchedule.setInstanceName(bk.getInstanceName());
        if (bk.getHour() != null) {
            backupSchedule.setHour(bk.getHour());
        }

        if (bk.getMinute() != null) {
            backupSchedule.setMinute(bk.getMinute());
        }

        if (bk.getSecond() != null) {
            backupSchedule.setSecond(bk.getSecond());
        }
        backupSchedule.setTimeZone(bk.getTimeZone());
        backupSchedule.setIntervalNum(bk.getIntervalNum());
        backupSchedule.setIntervalType(bk.getIntervalType());
        backupSchedule.setLastBackupTime(bk.getLastBackupTime());
        backupSchedule.setNextBackupTime(bk.getNextBackupTime());
        backupSchedule.setKeepRecordBackup(bk.getKeepRecordBackup());
        backupSchedule.setCreated(bk.getCreatedAt());
        backupSchedule.setUpdated(bk.getUpdatedAt());

        return backupSchedule.build();
    }

    @Override
    public void updateBackupSchedule(UpdateBackupScheduleRequest request, StreamObserver<CommonResponse> responseObserver) {
        try {
            log.info("updateBackupSchedule params => [{}]", request);
            UpdateBackupScheduleReq req = new UpdateBackupScheduleReq();
            req.setId(request.getId());
            req.setHour(request.getHour());
            req.setMinute(request.getMinute());
            req.setSecond(0);
            req.setActive(request.getActive());
            req.setIntervalNum(request.getIntervalNum());
            req.setKeepRecordBackup(request.getKeepRecordBackup());
            req.setTimeZone(request.getTimeZone());

            req.validate();

            backupRestoreService.updateBackupSchedule(req);
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .build());
        } finally {
//            RequestCtx.clear();
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteBackupSchedule(DeleteBackupScheduleRequest request, StreamObserver<CommonResponse> responseObserver) {
        try {
            log.info("deleteBackupSchedule params => [{}]", request);

            List<String> backupScheduleIds = request.getBackupScheduleIdsList().stream().map(BackupScheduleIds::getBackupScheduleId).toList();
            backupRestoreService.deleteBackupSchedule(backupScheduleIds);
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(CommonResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .build());
        } finally {
//            RequestCtx.clear();
            responseObserver.onCompleted();
        }
    }
}
