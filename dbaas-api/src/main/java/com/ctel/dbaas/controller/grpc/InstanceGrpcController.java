package com.ctel.dbaas.controller.grpc;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.common.enums.DatastoreSupport;
import com.ctel.dbaas.dto.instance.InstanceCreateReq;
import com.ctel.dbaas.dto.instance.InstanceDetail;
import com.ctel.dbaas.dto.instance.InstanceInfo;
import com.ctel.dbaas.dto.instance.InstanceRes;
import com.ctel.dbaas.repository.dbaas.projection.instance.InstanceDropdownRes;
import com.ctel.dbaas.service.InstanceService;
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
public class InstanceGrpcController extends InstanceServiceGrpc.InstanceServiceImplBase {

    @Autowired
    private InstanceService instanceService;

    @Override
    public void createInstance(CreateInstanceRequest request, StreamObserver<CreateInstanceResponse> responseObserver) {
        try {
            log.info("createInstance params => [{}]", request);
            InstanceCreateReq req = new InstanceCreateReq();
            req.setName(request.getName());
            req.setVolumeSize(request.getVolumeSize());
            req.setFlavorId(request.getFlavorId());
            req.setGroupConfigurationId(request.getGroupConfigurationId());
            req.setNetworkId(request.getNetworkId());
            req.setSubnetId(request.getSubnetId());
            req.setSecurityGroupIds(request.getSecurityGroupIds());
            req.setBackupId(request.getBackupId());

            InstanceCreateReq.Datastore datastore = new InstanceCreateReq.Datastore();
            datastore.setDatastoreName(DatastoreSupport.getOrThrow(request.getDatastore().getDatastoreCode()).getName());
            datastore.setDatastoreCode(request.getDatastore().getDatastoreCode());
            datastore.setDatastoreVersionId(request.getDatastore().getDatastoreVersionId());
            datastore.setDatastoreModeId(request.getDatastore().getDatastoreModeId());

            req.setDatastore(datastore);
            req.setRequestMetadata(request.getRequestMetadata());

            req.validate();
            String instanceId = instanceService.createInstance(req, GrpcCtx.getReqCtx());
            responseObserver.onNext(CreateInstanceResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(grpc.model.dbaas_service.InstanceRes.newBuilder()
                            .setInstanceId(instanceId)
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(CreateInstanceResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getInstanceDetail(GetInstanceDetailRequest request, StreamObserver<GetInstanceDetailResponse> responseObserver) {
        try {
            log.info("getInstanceDetail params => [{}]", request);
            InstanceDetail instanceDetail = instanceService.getInstanceDetail(request.getInstanceId(), GrpcCtx.getReqCtx());

            responseObserver.onNext(GetInstanceDetailResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(InstanceDetailRes.newBuilder()
                            .setId(instanceDetail.getId())
                            .setName(instanceDetail.getInstanceName())
                            .setDatastoreName(instanceDetail.getDatastoreName())
                            .setDatastoreVersion(instanceDetail.getDatastoreVersion())
                            .setDatastoreMode(instanceDetail.getDatastoreMode())
                            .setGroupConfigId(instanceDetail.getGroupConfigId())
                            .setSecurityClientIds(instanceDetail.getSecurityClientIds() != null ? instanceDetail.getSecurityClientIds() : "")
                            .setVpcId(instanceDetail.getVpcId() != null ? instanceDetail.getVpcId() : "")
                            .setSubnetId(instanceDetail.getSubnetId() != null ? instanceDetail.getSubnetId() : "")
                            .setStatus(instanceDetail.getStatus().toUpperCase())
                            .setDataDetail(instanceDetail.getDataDetail())
                            .setFlavorId(instanceDetail.getFlavorId())
                            .setVpcName(instanceDetail.getVpcName())
                            .setSubnetName(instanceDetail.getSubnetName())
                            .setFlavorName(instanceDetail.getFlavorName())
                            .setVolumeSize(instanceDetail.getVolumeSize())
                            .setCreated(instanceDetail.getCreated())
                            .setUpdated(instanceDetail.getUpdated())
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetInstanceDetailResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .setData(InstanceDetailRes.newBuilder().build())
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getInstanceDetailV2(GetInstanceDetailRequest request, StreamObserver<GetInstanceDetailResponseV2> responseObserver) {
        try {
            log.info("getInstanceDetailV2 params => [{}]", request);
            InstanceDetail instanceDetail = instanceService.getInstanceDetail(request.getInstanceId(), GrpcCtx.getReqCtx());

            responseObserver.onNext(GetInstanceDetailResponseV2.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(InstanceDetailResV2.newBuilder()
                            .setId(instanceDetail.getId())
                            .setName(instanceDetail.getInstanceName())
                            .setDatastoreName(instanceDetail.getDatastoreName())
                            .setDatastoreVersion(instanceDetail.getDatastoreVersion())
                            .setDatastoreMode(instanceDetail.getDatastoreMode())
                            .setGroupConfigId(instanceDetail.getGroupConfigId())
                            .setSecurityClientIds(instanceDetail.getSecurityClientIds() != null ? instanceDetail.getSecurityClientIds() : "")
                            .setVpcId(instanceDetail.getVpcId() != null ? instanceDetail.getVpcId() : "")
                            .setSubnetId(instanceDetail.getSubnetId() != null ? instanceDetail.getSubnetId() : "")
                            .setStatus(instanceDetail.getStatus().toUpperCase())
                            .setDataDetail(instanceDetail.getDataDetail())
                            .setVpcName(instanceDetail.getVpcName())
                            .setSubnetName(instanceDetail.getSubnetName())
                            .setVolumeSize(instanceDetail.getVolumeSize())
                            .setCreated(instanceDetail.getCreated())
                            .setUpdated(instanceDetail.getUpdated())
                            .setFlavorInfo(InstanceDetailResV2.FlavorInfo.newBuilder()
                                    .setId(instanceDetail.getFlavorId())
                                    .setName(instanceDetail.getFlavorName())
                                    .setVCpus(instanceDetail.getVCpu())
                                    .setRam(instanceDetail.getRam())
                                    .setDisk(instanceDetail.getDisk())
                                    .build())
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetInstanceDetailResponseV2.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .setData(InstanceDetailResV2.newBuilder().build())
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getListInstance(GetListInstanceRequest request, StreamObserver<GetListInstanceResponse> responseObserver) {
        try {
            log.info("getListInstance params => [{}]", request);
            Pageable pageable = CommonUtils.convertToPageable(request.getPage(), request.getSize(), "DESC", "createdAt");
            List<String> datastoreCodes = DatastoreSupport.validateDatastoreCodes(request.getDatastoreCode());
            Page<InstanceRes> instanceRes = instanceService.listInstance(request.getQuery(), datastoreCodes, pageable, GrpcCtx.getReqCtx());

            List<Instance> result = instanceRes.stream()
                    .map(i -> Instance.newBuilder()
                            .setId(i.getId())
                            .setName(i.getName())
                            .setVCpus(i.getVCpus())
                            .setRam(i.getRam())
                            .setDisk(i.getDisk())
                            .setVolumeSize(i.getVolumeSize())
                            .setDatastoreName(i.getDatastoreName())
                            .setDatastoreVersion(i.getDatastoreVersion())
                            .setDatastoreVersionId(i.getDatastoreVersionId())
                            .setDatastoreMode(i.getDatastoreMode())
                            .setDatastoreModeId(i.getDatastoreModeId())
                            .setStatus(i.getStatus().toUpperCase())
                            .setCreatedAt(i.getCreatedAt())
                            .setUpdatedAt(i.getUpdatedAt())
                            .setGroupConfigId(i.getGroupConfigId())
                            .build())
                    .toList();

            responseObserver.onNext(GetListInstanceResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(ListInstance.newBuilder()
                            .addAllDocs(result)
                            .setPage(instanceRes.getPageable().getPageNumber() + 1)
                            .setSize(instanceRes.getPageable().getPageSize())
                            .setTotal((int) instanceRes.getTotalElements())
                            .setTotalPage(instanceRes.getTotalPages())
                            .build())
                    .build());
            log.info("getListInstance DONE => [{}]", request);
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetListInstanceResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .setData(ListInstance.newBuilder()
                            .addAllDocs(new ArrayList<>()).build())
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getListInstanceDropdown(GetListInstanceDropdownRequest request, StreamObserver<GetListInstanceDropdownResponse> responseObserver) {
        try {
            log.info("getListInstanceDropdown params => [{}]", request);
            List<String> datastoreCodes = DatastoreSupport.validateDatastoreCodes(request.getDatastoreCode());
            List<InstanceDropdownRes> instanceRes = instanceService.listInstanceDropdown(request.getInstanceName(), datastoreCodes, request.getStatus(), GrpcCtx.getReqCtx());
            List<InstanceDropdown> result = instanceRes.stream()
                    .map(i -> InstanceDropdown.newBuilder()
                            .setId(i.getId())
                            .setName(i.getName())
                            .setDatastoreName(i.getDatastoreName())
                            .build())
                    .toList();

            responseObserver.onNext(GetListInstanceDropdownResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(ListInstanceDropdown.newBuilder()
                            .addAllDocs(result)
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetListInstanceDropdownResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .setData(ListInstanceDropdown.newBuilder()
                            .addAllDocs(new ArrayList<>()).build())
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteInstance(DeleteInstanceRequest request, StreamObserver<CommonResponse> responseObserver) {
        try {
            log.info("deleteInstance params => [{}]", request);
            List<String> instanceIds = request.getInstanceIdsList().stream().map(InstanceIds::getInstanceId).toList();
            instanceService.deleteInstance(instanceIds, GrpcCtx.getReqCtx());
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
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getInstanceInfo(GetInstanceInfoRequest request, StreamObserver<GetInstanceInfoResponse> responseObserver) {
        try {
            log.info("getInstanceInfo params => [{}], requestInfo[{}]", request, GrpcCtx.getReqCtx());
            InstanceInfo instanceInfo = instanceService.getInstanceInfo(request.getInstanceId(), true);
            responseObserver.onNext(GetInstanceInfoResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(grpc.model.dbaas_service.InstanceInfo.newBuilder()
                            .setInstanceName(instanceInfo.getInstance().getName())
                            .setOrgId(instanceInfo.getInstance().getOrgId())
                            .setRegionId(instanceInfo.getInstance().getRegionId())
                            .setProjectId(instanceInfo.getInstance().getProjectId())
                            .setType(DatastoreSupport.getOrThrow(instanceInfo.getDatastoreCode()).getMonitorResourceTypeName())
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetInstanceInfoResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}
