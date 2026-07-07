package com.ctel.dbaas.controller.grpc;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.common.enums.DatastoreSupport;
import com.ctel.dbaas.dto.configuration.GroupConfigurationRes;
import com.ctel.dbaas.dto.configuration.*;
import com.ctel.dbaas.repository.dbaas.projection.group_config.GroupConfigDropdown;
import com.ctel.dbaas.service.ConfigurationService;
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
public class ConfigurationGrpcController extends ConfigurationServiceGrpc.ConfigurationServiceImplBase {

    @Autowired
    private ConfigurationService configurationService;

    @Override
    public void createGroupConfiguration(CreateGroupConfigurationRequest request, StreamObserver<CreateGroupConfigurationResponse> responseObserver) {
        try {
            log.info("createGroupConfiguration params => [{}]", request);
            CreateGroupConfigReq req = CreateGroupConfigReq.builder()
                    .datastoreModeId(request.getDatastoreModeId())
                    .name(request.getName())
                    .description(request.getDescription())
                    .overridesConfig(request.getOverridesConfigMap())
                    .build();
            req.validate();

            String groupConfigId = configurationService.createGroupConfiguration(req);

            responseObserver.onNext(CreateGroupConfigurationResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(grpc.model.dbaas_service.GroupConfigurationRes.newBuilder()
                            .setGroupConfigId(groupConfigId)
                            .setDatastoreModeId(request.getDatastoreModeId())
                            .setName(req.getName())
                            .setDescription(request.getDescription())
                            .putAllOverridesConfig(request.getOverridesConfigMap())
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(CreateGroupConfigurationResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getListGroupConfiguration(GetListGroupConfigurationRequest request, StreamObserver<GetListGroupConfigurationResponse> responseObserver) {
        try {
            log.info("getListGroupConfiguration params => [{}]", request);
            Pageable pageable = CommonUtils.convertToPageable(request.getPage(), request.getSize());
            DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(request.getDatastoreCode());

            Page<GroupConfigurationRes> groupConfigRes = configurationService
                    .getListGroupConfiguration(request.getQuery(), request.getDatastoreModeId(),
                            datastoreSupport.getCode(), request.getGetDefault(), pageable);

            List<GroupConfigurationView> result = groupConfigRes.getContent().stream()
                    .map(gr -> GroupConfigurationView.newBuilder()
                            .setId(gr.getId())
                            .setName(gr.getName())
                            .setDescription(gr.getDescription())
                            .setDatastoreName(gr.getDatastoreName())
                            .setDatastoreVersion(gr.getDatastoreVersion())
                            .setDatastoreVersionId(gr.getDatastoreVersionId())
                            .setDatastoreMode(gr.getDatastoreMode())
                            .setDatastoreModeId(gr.getDatastoreModeId())
                            .setCreatedAt(gr.getCreatedAt())
                            .setUpdatedAt(gr.getUpdatedAt())
                            .setIsGroupDefault(gr.isGroupDefault())
                            .addAllListInstance(gr.getListInstanceInfo().stream()
                                    .map(in -> InstancesUseGroupConfig.newBuilder()
                                            .setId(in.getId())
                                            .setName(in.getName())
                                            .build())
                                    .toList())
                            .build()
                    )
                    .toList();

            responseObserver.onNext(GetListGroupConfigurationResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(ListGroupConfigurationView.newBuilder()
                            .addAllDocs(result)
                            .setPage(groupConfigRes.getPageable().getPageNumber() + 1)
                            .setSize(groupConfigRes.getPageable().getPageSize())
                            .setTotal((int) groupConfigRes.getTotalElements())
                            .setTotalPage(groupConfigRes.getTotalPages())
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetListGroupConfigurationResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .setData(ListGroupConfigurationView.newBuilder()
                            .addAllDocs(new ArrayList<>())
                            .build()
                    )
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getListGroupConfigDropdown(GetListGroupConfigDropdownRequest request, StreamObserver<GetListGroupConfigDropdownResponse> responseObserver) {
        try {
            log.info("getListGroupConfigDropdown params => [{}]", request);
            List<GroupConfigDropdown> groupConfigRes = configurationService.getListGroupConfigDropdown(request.getDatastoreModeId());

            List<grpc.model.dbaas_service.GroupConfigDropdown> result = groupConfigRes.stream()
                    .map(gr -> grpc.model.dbaas_service.GroupConfigDropdown.newBuilder()
                            .setId(gr.getId())
                            .setName(gr.getName())
                            .setGroupDefault(gr.getDefault())
                            .build()
                    )
                    .toList();

            responseObserver.onNext(GetListGroupConfigDropdownResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(ListGroupConfigDropdown.newBuilder()
                            .addAllDocs(result)
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetListGroupConfigDropdownResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .setData(ListGroupConfigDropdown.newBuilder()
                            .addAllDocs(new ArrayList<>())
                            .build()
                    )
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getGroupConfigDetail(GetGroupConfigDetailRequest request, StreamObserver<GetGroupConfigDetailResponse> responseObserver) {
        try {
            log.info("getGroupConfigDetail params => [{}]", request);

            GroupConfigRes groupConfigRes = configurationService.getGroupConfigDetail(request.getGroupConfigId(), GrpcCtx.getReqCtx());

            List<ConfigurationView> listConfig = groupConfigRes.getConfigurations()
                    .stream()
                    .map(config ->
                            ConfigurationView.newBuilder()
                                    .setId(config.getId())
                                    .setParamName(config.getParamName())
                                    .setParamValue(config.getParamValue())
                                    .setDefaultValue(config.getDefaultValue())
                                    .setValueRange(config.getValueRange())
                                    .setValueType(config.getValueType())
                                    .setDescription(config.getDescription())
                                    .setCreatedAt(config.getCreatedAt())
                                    .setUpdatedAt(config.getUpdatedAt())
                                    .build()
                    )
                    .toList();

            responseObserver.onNext(GetGroupConfigDetailResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(GroupConfigDetailRes.newBuilder()
                            .setGroupConfigId(groupConfigRes.getGroupConfigId())
                            .setDatastoreModeId(groupConfigRes.getDatastoreModeId())
                            .setName(groupConfigRes.getName())
                            .setDescription(groupConfigRes.getDescription())
                            .addAllConfigurations(listConfig)
                            .build())
                    .build()
            );
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetGroupConfigDetailResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getListConfigurationDefault(GetListConfigurationDefaultRequest request, StreamObserver<GetListConfigurationDefaultResponse> responseObserver) {
        try {
            log.info("getListConfigurationDefault params => [{}]", request);
            GroupConfigRes groupConfigRes = configurationService.getGroupConfigDefaultDetail(request.getDatastoreModeId());
            List<ConfigurationView> listConfig = groupConfigRes.getConfigurations()
                    .stream()
                    .map(config ->
                            ConfigurationView.newBuilder()
                                    .setId(config.getId())
                                    .setParamName(config.getParamName())
                                    .setParamValue(config.getParamValue())
                                    .setDefaultValue(config.getDefaultValue())
                                    .setValueRange(config.getValueRange())
                                    .setValueType(config.getValueType())
                                    .setDescription(config.getDescription())
                                    .setCreatedAt(config.getCreatedAt())
                                    .setUpdatedAt(config.getUpdatedAt())
                                    .build()
                    )
                    .toList();

            responseObserver.onNext(GetListConfigurationDefaultResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(GroupConfigDetailRes.newBuilder()
                            .setGroupConfigId(groupConfigRes.getGroupConfigId())
                            .setDatastoreModeId(groupConfigRes.getDatastoreModeId())
                            .setName(groupConfigRes.getName())
                            .setDescription(groupConfigRes.getDescription())
                            .addAllConfigurations(listConfig)
                            .build())
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetListConfigurationDefaultResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getConfigurationInstance(GetConfigurationInstanceRequest request, StreamObserver<GetConfigurationInstanceResponse> responseObserver) {
        try {
            log.info("getConfigurationInstance params => [{}]", request);

            List<ConfigurationView> responseList = configurationService.getConfigurationOfInstance(request.getInstanceId(), GrpcCtx.getReqCtx())
                    .stream()
                    .map(config ->
                            ConfigurationView.newBuilder()
                                    .setId(config.getId())
                                    .setParamName(config.getParamName())
                                    .setParamValue(config.getParamValue())
                                    .setDefaultValue(config.getDefaultValue())
                                    .setValueRange(config.getValueRange())
                                    .setValueType(config.getValueType())
                                    .setDescription(config.getDescription())
                                    .setCreatedAt(config.getCreatedAt())
                                    .setUpdatedAt(config.getUpdatedAt())
                                    .build()
                    )
                    .toList();

            responseObserver.onNext(GetConfigurationInstanceResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.SUCCESS)
                    .setMsg(Constant.Message.SUCCESS)
                    .setData(ListConfiguration.newBuilder()
                            .addAllDocs(responseList)
                            .build()
                    )
                    .build());
        } catch (Exception e) {
            log.log(Level.ERROR, e.getMessage(), e);
            responseObserver.onNext(GetConfigurationInstanceResponse.newBuilder()
                    .setCurrentTime(LocalDateTime.now().toString())
                    .setStatus(Constant.STATUS.ERROR)
                    .setMsg(e.getMessage())
                    .setData(ListConfiguration.newBuilder()
                            .addAllDocs(new ArrayList<>())
                            .build())
                    .build());
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateConfigurations(UpdateConfigurationsRequest request, StreamObserver<CommonResponse> responseObserver) {
        try {
            log.info("updateConfigurations params => [{}]", request);
            UpdateConfigReq req = UpdateConfigReq.builder()
                    .groupConfigId(request.getGroupConfigId())
                    .configurations(request.getConfigurationsMap())
                    .build();
            configurationService.updateConfiguration(req, GrpcCtx.getReqCtx());
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
    public void updateGroupConfiguration(UpdateGroupConfigurationRequest request, StreamObserver<CommonResponse> responseObserver) {
        try {
            log.info("updateGroupConfiguration params => [{}]", request);

            UpdateGroupConfigReq req = UpdateGroupConfigReq.builder()
                    .groupConfigId(request.getGroupConfigId())
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();
            req.validate();
            configurationService.updateGroupConfiguration(req, GrpcCtx.getReqCtx());

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
    public void deleteGroupConfiguration(DeleteGroupConfigurationRequest request, StreamObserver<CommonResponse> responseObserver) {
        try {
            log.info("deleteGroupConfiguration params => [{}]", request);
            List<String> groupIds = request.getGroupConfigIdsList().stream().map(GroupConfigIds::getGroupConfigId).toList();
            configurationService.deleteGroupConfiguration(groupIds, GrpcCtx.getReqCtx());
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

}
