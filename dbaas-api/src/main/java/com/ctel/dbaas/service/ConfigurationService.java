package com.ctel.dbaas.service;

import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.common.enums.DataTypeConfig;
import com.ctel.dbaas.common.enums.InstanceAction;
import com.ctel.dbaas.common.enums.Status;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.configuration.*;
import com.ctel.dbaas.entity.dbaas.*;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.*;
import com.ctel.dbaas.repository.dbaas.projection.DatastoreModeInfo;
import com.ctel.dbaas.repository.dbaas.projection.QueryConfiguration;
import com.ctel.dbaas.repository.dbaas.projection.group_config.GroupConfigDropdown;
import com.ctel.dbaas.repository.dbaas.projection.instance.InstanceInfoProjection;
import com.ctel.dbaas.utils.CommonUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Log4j2
public class ConfigurationService {

    @Autowired
    private DatastoreModeRepository datastoreModeRepository;

    @Autowired
    private DatastoreConfigurationRepository datastoreConfigurationRepository;

    @Autowired
    private GroupConfigurationRepository groupConfigurationRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private ActionService actionService;
    @Autowired
    private ComputeRepository computeRepository;


    @Transactional(rollbackFor = {Exception.class})
    public String createGroupConfiguration(CreateGroupConfigReq req) {
        try {
            DatastoreModeEntity datastoreMode = datastoreModeRepository.findById(req.getDatastoreModeId())
                    .orElseThrow(() -> new AppException(new ErrorResponse("Datastore mode not found")));

            List<DatastoreConfigurationEntity> lstConfigDefault = datastoreConfigurationRepository
                    .findAllByDatastoreModeIdAndStatus(datastoreMode.getId(), Status.ACTIVE.getStatus());

            GroupConfigurationEntity groupConfiguration = new GroupConfigurationEntity();
            groupConfiguration.setName(req.getName());
            groupConfiguration.setDescription(req.getDescription());
            groupConfiguration.setDatastoreModeId(datastoreMode.getId());
            groupConfiguration.setOrgId(GrpcCtx.getReqCtx().getOrgId());
            groupConfigurationRepository.save(groupConfiguration);

            List<ConfigurationEntity> lstConfigCustom = new ArrayList<>();
            for (DatastoreConfigurationEntity cfDefault : lstConfigDefault) {
                String newValue = req.getOverridesConfig().get(cfDefault.getParamName());
                String dataType = cfDefault.getTypeValue();
                if (StringUtils.isBlank(newValue)) {
                    if (DataTypeConfig.String.name().equals(dataType)) {
                        newValue = StringUtils.EMPTY;
                    } else {
                        throw new AppException(new ErrorResponse("Missing param %s in request", cfDefault.getParamName()));
                    }
                }
                this.validateParamConfig(cfDefault, newValue);

                ConfigurationEntity configCustom = new ConfigurationEntity();
                configCustom.setParamName(cfDefault.getParamName());
                configCustom.setParamValue(newValue);
                configCustom.setGroupConfigurationId(groupConfiguration.getId());
                configCustom.setDatastoreConfigurationId(cfDefault.getId());
                configCustom.setOrgId(GrpcCtx.getReqCtx().getOrgId());
                lstConfigCustom.add(configCustom);
            }
            configurationRepository.saveAll(lstConfigCustom);

            return groupConfiguration.getId();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    public Page<GroupConfigurationRes> getListGroupConfiguration(String configGroupName, String datastoreModeId, String datastoreCode,
                                                                 boolean groupDefault, Pageable pageable) {
        Page<GroupConfigurationEntity> lstGroupConfig;
        String orgId = groupDefault ? "system" : GrpcCtx.getReqCtx().getOrgId();

        if (StringUtils.isBlank(configGroupName)) {
            lstGroupConfig = StringUtils.isBlank(datastoreModeId) ?
                    groupConfigurationRepository.queryAllByOrgIdAndDatastoreCode(orgId, datastoreCode, pageable) : // màn list group config
                    groupConfigurationRepository.findAllByOrgIdAndDatastoreModeId(orgId, datastoreModeId, pageable);
        } else {
            lstGroupConfig = StringUtils.isBlank(datastoreModeId) ?
                    groupConfigurationRepository.queryAllByOrgIdAndNameLike(orgId, datastoreCode, configGroupName, pageable) : // filter màn list group config
                    groupConfigurationRepository.findAllByOrgIdAndDatastoreModeIdAndNameContainingIgnoreCase(
                            orgId, datastoreModeId, configGroupName, pageable);
        }

        return lstGroupConfig.map(this::convertToRes);
    }

    public List<GroupConfigDropdown> getListGroupConfigDropdown(String datastoreModeId) {
        return groupConfigurationRepository.getGroupConfigDropdown(datastoreModeId, List.of(GrpcCtx.getReqCtx().getOrgId()));
    }

    public GroupConfigRes getGroupConfigDetail(String groupConfigId, RequestInfo requestInfo) {
        GroupConfigurationEntity groupConfiguration = groupConfigurationRepository.findByIdAndOrgId(groupConfigId, requestInfo.getOrgId())
                .orElseThrow(() -> new AppException(new ErrorResponse("Group config not found")));
        List<ConfigurationRes> configurations;
        if (!groupConfiguration.isDefault()) {
            List<QueryConfiguration> lstConfig = configurationRepository
                    .getConfigurationInfo(groupConfigId, GrpcCtx.getReqCtx().getOrgId());
            configurations = lstConfig.stream().map(this::convertToRes).toList();
        } else {
            // query table tb_datastore_configuration
            List<DatastoreConfigurationEntity> lstConfig = datastoreConfigurationRepository
                    .findAllByDatastoreModeIdAndStatus(groupConfiguration.getDatastoreModeId(), Status.ACTIVE.getStatus());
            configurations = lstConfig.stream().map(this::convertToRes).toList();
        }

        GroupConfigRes response = new GroupConfigRes();
        response.setGroupConfigId(groupConfiguration.getId());
        response.setDatastoreModeId(groupConfiguration.getDatastoreModeId());
        response.setName(groupConfiguration.getName());
        response.setDescription(groupConfiguration.getDescription());
        response.setConfigurations(configurations);

        return response;
    }

    public GroupConfigRes getGroupConfigDefaultDetail(String datastoreModeId) {
        GroupConfigurationEntity groupConfiguration = groupConfigurationRepository.findFirstByDatastoreModeId(datastoreModeId)
                .orElseThrow(() -> new AppException(new ErrorResponse("Group config not found")));
        List<DatastoreConfigurationEntity> configDefaults = datastoreConfigurationRepository
                .findAllByDatastoreModeIdAndStatus(datastoreModeId, Status.ACTIVE.getStatus());
        List<ConfigurationRes> listConfig = configDefaults.stream().map(this::convertToRes).toList();

        GroupConfigRes response = new GroupConfigRes();
        response.setGroupConfigId(groupConfiguration.getId());
        response.setDatastoreModeId(groupConfiguration.getDatastoreModeId());
        response.setName(groupConfiguration.getName());
        response.setDescription(groupConfiguration.getDescription());
        response.setConfigurations(listConfig);

        return response;
    }

    public List<ConfigurationRes> getConfigurationOfInstance(String instanceId, RequestInfo requestInfo) {
        InstanceEntity instance = instanceRepository
                .findByIdAndOrgIdAndProjectId(instanceId, requestInfo.getOrgId(), requestInfo.getProjectId())
                .orElseThrow(() -> new AppException(new ErrorResponse("instance not found")));
        GroupConfigRes res = this.getGroupConfigDetail(instance.getGroupConfigurationId(), requestInfo);

        return res.getConfigurations();
    }

    public void updateConfiguration(UpdateConfigReq req, RequestInfo requestCtx) {
        GroupConfigurationEntity groupConfiguration = groupConfigurationRepository
                .findFirstByIdAndOrgId(req.getGroupConfigId(), requestCtx.getOrgId())
                .orElseThrow(() -> new AppException(new ErrorResponse("Group configuration not found")));

        if (!req.getConfigurations().isEmpty()) {
            List<QueryConfiguration> lstConfig = configurationRepository.getConfigurationInfo(req.getGroupConfigId(), requestCtx.getOrgId());

            List<InstanceEntity> lstInstance = instanceRepository.findAllByOrgIdAndProjectIdAndDeletedAtNullAndGroupConfigurationId(
                    requestCtx.getOrgId(), requestCtx.getProjectId(), groupConfiguration.getId());

            Map<String, String> mapInstanceNotRunning = new HashMap<>();
            for (InstanceEntity instance : lstInstance) {
                List<ComputeEntity> listCompute = computeRepository.findAllByInstanceIdAndProjectIdAndOrgId(instance.getId(),
                        requestCtx.getProjectId(), requestCtx.getOrgId());
                for (ComputeEntity compute : listCompute) {
                    if (!"RUNNING".equalsIgnoreCase(compute.getStatus())) {
                        mapInstanceNotRunning.put(instance.getId(), instance.getName());
                        break;
                    }
                }
            }

            if (!mapInstanceNotRunning.isEmpty()) {
                String msgError = "some compute in the instance is in a non-RUNNING state : " + CommonUtils.toJson(mapInstanceNotRunning);
                throw new AppException(new ErrorResponse(msgError));
            }

            List<ConfigurationEntity> lstConfigCustom = new ArrayList<>();
            Map<String, String> configurations = req.getConfigurations();
            for (QueryConfiguration config : lstConfig) {
                if (!configurations.containsKey(config.getParamName())) {
                    continue;
                }

                String newValue = configurations.get(config.getParamName());
//                if (StringUtils.isBlank(newValue)) {
//                    throw new AppException(new ErrorResponse("Missing param %s in request", config.getParamName()));
//                }
                this.validateParamConfig(newValue, config.getValueType(), config.getParamName(), config.getValueRange());
                ConfigurationEntity editConfig = new ConfigurationEntity();
                editConfig.setId(config.getId());
                editConfig.setParamName(config.getParamName());
                editConfig.setParamValue(newValue);
                editConfig.setGroupConfigurationId(groupConfiguration.getId());
                editConfig.setDatastoreConfigurationId(config.getDatastoreConfigId());
                editConfig.setOrgId(GrpcCtx.getReqCtx().getOrgId());
                editConfig.setCreatedAt(config.getCreatedAt());
                editConfig.setUpdatedAt(LocalDateTime.now());
                lstConfigCustom.add(editConfig);
            }
            configurationRepository.saveAll(lstConfigCustom);

            Map<String, Object> mapData = new HashMap<>();
            mapData.put("groupConfigId", groupConfiguration.getId());

            for (InstanceEntity instance : lstInstance) {
                actionService.executeAction(instance.getId(), InstanceAction.CHANGE_GROUP_CONFIG, mapData, requestCtx);
            }

//            Map<String, String> mapInstanceNotRunning = new HashMap<>();
//            for (InstanceEntity instance : lstInstance) {
//                List<ComputeEntity> listCompute = computeRepository.findAllByInstanceIdAndProjectIdAndOrgId(instance.getId(),
//                        requestCtx.getProjectId(), requestCtx.getOrgId());
//                for (ComputeEntity compute : listCompute) {
//                    if (!"RUNNING".equals(compute.getStatus())) {
//                        mapInstanceNotRunning.put(instance.getId(), instance.getName());
//                        break;
//                    }
//                }
//
//                if (instance.getStatus().equals(InstanceStatus.RUNNING.getStatus())) {
//                    actionService.executeAction(instance.getId(), InstanceAction.CHANGE_GROUP_CONFIG, mapData, requestCtx);
//                }
//            }
        }
    }

    public void updateGroupConfiguration(UpdateGroupConfigReq req, RequestInfo requestInfo) {
        GroupConfigurationEntity groupConfiguration = groupConfigurationRepository.findFirstByIdAndOrgId(
                req.getGroupConfigId(), requestInfo.getOrgId()).orElseThrow(() -> new AppException(new ErrorResponse("Group configuration not found")));

        groupConfiguration.setName(req.getName());
        groupConfiguration.setDescription(req.getDescription());
        groupConfigurationRepository.save(groupConfiguration);
    }

    @Transactional
    public void deleteGroupConfiguration(List<String> groupConfigIds, RequestInfo requestInfo) {
        List<String> groupIds = groupConfigurationRepository.getIdsByOrgId(requestInfo.getOrgId(), groupConfigIds);
        if (!groupIds.isEmpty()) {
            List<InstanceEntity> lstInstanceApplyGroupConfig = instanceRepository.findAllByGroupConfigurationIdInAndOrgIdAndProjectIdAndDeletedAtIsNull(
                    groupIds, requestInfo.getOrgId(), requestInfo.getProjectId());
            if (!lstInstanceApplyGroupConfig.isEmpty()) {
                InstanceEntity instance = lstInstanceApplyGroupConfig.get(0);
                throw new AppException(new ErrorResponse("Group config[%s] cannot be deleted because there is a instance[%s] applied", instance.getGroupConfigurationId(), instance.getId()));
            }

            configurationRepository.deleteAllByGroupConfigurationIdInAndOrgId(groupIds, requestInfo.getOrgId());
            groupConfigurationRepository.deleteAllById(groupIds);
        }
    }

    private void validateParamConfig(DatastoreConfigurationEntity configParam, String value) {
        this.validateParamConfig(value, configParam.getTypeValue(), configParam.getParamName(), configParam.getRangeValue());
    }

    private void validateParamConfig(String value, String valueType, String paramName, String valueRange) {
        if (DataTypeConfig.Integer.name().equals(valueType)) {
            if (!CommonUtils.isNumeric(value)) {
                throw new AppException(new ErrorResponse("The value of %s must be number", paramName));
            }
            boolean fromIsNegativeNumber = false;
            if (valueRange.startsWith("-")) {
                valueRange = valueRange.substring(1);
                fromIsNegativeNumber = true;
            }
            int val = Integer.parseInt(value);
            int from = fromIsNegativeNumber ? -Integer.parseInt(valueRange.split("-")[0]) : Integer.parseInt(valueRange.split("-")[0]);
            int to = Integer.parseInt(valueRange.split("-")[1]);
            if (!(from <= val && val <= to)) {
                throw new AppException(new ErrorResponse("Value of param %s must be between %s and %s", paramName, from, to));
            }
        } else if (DataTypeConfig.Enum.name().equals(valueType)) {
            List<String> validValue = List.of(valueRange.split(","));
            if (!validValue.contains(value)) {
                throw new AppException(new ErrorResponse("Value of param %s must exist in this list: %s", paramName, valueRange));
            }
        } else if (DataTypeConfig.Regular.name().equals(valueType)) {
            Pattern pattern = Pattern.compile(valueRange);
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) {
                throw new AppException(new ErrorResponse("Value of param %s must match regex %s", paramName, valueRange));
            }
        } else if (DataTypeConfig.Double.name().equals(valueType)) {
            if (!CommonUtils.isNumeric(value)) {
                throw new AppException(new ErrorResponse("The value of %s must be number", paramName));
            }
            boolean fromIsNegativeNumber = false;
            if (valueRange.startsWith("-")) {
                valueRange = valueRange.substring(1);
                fromIsNegativeNumber = true;
            }
            double val = Double.parseDouble(value);
            double from = fromIsNegativeNumber ? -Double.parseDouble(valueRange.split("-")[0]) : Double.parseDouble(valueRange.split("-")[0]);
            double to = Double.parseDouble(valueRange.split("-")[1]);
            if (!(from <= val && val <= to)) {
                throw new AppException(new ErrorResponse("Value of param %s must be between %s and %s", paramName, from, to));
            }
        }
    }

    private GroupConfigurationRes convertToRes(GroupConfigurationEntity entity) {
        GroupConfigurationRes dto = new GroupConfigurationRes();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setDatastoreModeId(entity.getDatastoreModeId());
        DatastoreModeInfo datastoreModeInfo = datastoreModeRepository.getByDatastoreModeId(entity.getDatastoreModeId());
        dto.setDatastoreName(datastoreModeInfo.getDatastoreName());
        dto.setDatastoreCode(datastoreModeInfo.getDatastoreCode());
        dto.setDatastoreVersion(datastoreModeInfo.getDatastoreVersion());
        dto.setDatastoreVersionId(datastoreModeInfo.getDatastoreVersionId());
        dto.setDatastoreMode(datastoreModeInfo.getDatastoreMode());
        dto.setGroupDefault(entity.isDefault());

        List<InstanceInfoProjection> lstInstance = instanceRepository.getInstanceNameByGroupConfigId(entity.getId());
        dto.setListInstanceInfo(lstInstance.stream()
                .map(in -> new GroupConfigurationRes.InstancesUseGroupConfig(in.getId(), in.getName())).toList());

        dto.setCreatedAt(entity.getCreatedAt().toString());
        dto.setUpdatedAt(entity.getUpdatedAt().toString());

        return dto;
    }

    private ConfigurationRes convertToRes(QueryConfiguration configInfo) {
        ConfigurationRes dto = new ConfigurationRes();
        dto.setId(configInfo.getId());
        dto.setParamName(configInfo.getParamName());
        dto.setParamValue(configInfo.getParamValue());
        dto.setDefaultValue(configInfo.getDefaultValue());
        dto.setValueRange(configInfo.getValueRange());
        dto.setValueType(configInfo.getValueType());
        dto.setDescription(configInfo.getDescription());
        dto.setCreatedAt(configInfo.getCreatedAt() == null ? "" : configInfo.getCreatedAt().toString());
        dto.setUpdatedAt(configInfo.getUpdatedAt() == null ? "" : configInfo.getUpdatedAt().toString());

        return dto;
    }

    private ConfigurationRes convertToRes(DatastoreConfigurationEntity entity) {
        ConfigurationRes dto = new ConfigurationRes();
        dto.setId(entity.getId());
        dto.setParamName(entity.getParamName());
        dto.setParamValue(entity.getDefaultValue());
        dto.setDefaultValue(entity.getDefaultValue());
        dto.setValueRange(entity.getRangeValue());
        dto.setValueType(entity.getTypeValue());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt() == null ? "" : entity.getCreatedAt().toString());
        dto.setUpdatedAt(entity.getUpdatedAt() == null ? "" : entity.getUpdatedAt().toString());

        return dto;
    }

}
