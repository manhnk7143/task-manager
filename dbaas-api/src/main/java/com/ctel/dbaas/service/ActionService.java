package com.ctel.dbaas.service;

import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.common.enums.DatastoreSupport;
import com.ctel.dbaas.common.enums.InstanceAction;
import com.ctel.dbaas.common.enums.InstanceStatus;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.config.RabbitMQConfig;
import com.ctel.dbaas.datastore.DatastoreActionAbstract;
import com.ctel.dbaas.datastore.kafka.KafkaManager;
import com.ctel.dbaas.datastore.mongodb.MongodbManager;
import com.ctel.dbaas.datastore.redis.RedisManager;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.common.TaskManagerRequest;
import com.ctel.dbaas.dto.instance.InstanceInfo;
import com.ctel.dbaas.entity.dbaas.*;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.DatastoreModeRepository;
import com.ctel.dbaas.repository.dbaas.DatastoreRepository;
import com.ctel.dbaas.repository.dbaas.DatastoreVersionRepository;
import com.ctel.dbaas.repository.dbaas.InstanceRepository;
import com.ctel.dbaas.utils.CommonUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class ActionService {

    private static final Map<String, DatastoreActionAbstract> MAP_DATASTORE_MANAGER = new HashMap<>();

    @Autowired
    private InstanceRepository instanceRepository;

    @Autowired
    private DatastoreModeRepository datastoreModeRepository;

    @Autowired
    private DatastoreRepository datastoreRepository;

    @Autowired
    private DatastoreVersionRepository datastoreVersionRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public ActionService(RedisManager redisManager, MongodbManager mongodbManager, KafkaManager kafkaManager) {
        MAP_DATASTORE_MANAGER.put(DatastoreSupport.REDIS.getCode(), redisManager);
        MAP_DATASTORE_MANAGER.put(DatastoreSupport.MONGODB.getCode(), mongodbManager);
        MAP_DATASTORE_MANAGER.put(DatastoreSupport.KAFKA.getCode(), kafkaManager);
    }

    @SneakyThrows
    public Map<String, String> executeAction(String instanceId, InstanceAction action, Map<String, Object> data, RequestInfo reqCtx) {
        InstanceInfo instanceInfo = this.getInstanceInfo(instanceId, reqCtx);

        if (reqCtx.getClientSocketId() == null) {
            this.validateActionByStatus(instanceInfo, action);
        }

        Map<String, Object> reqMap;

        // new code
        DatastoreActionAbstract datastoreActionAbstract = MAP_DATASTORE_MANAGER.get(instanceInfo.getDatastoreCode());
        datastoreActionAbstract.setInstanceInfo(instanceInfo);
        datastoreActionAbstract.setData(data);
        datastoreActionAbstract.setReqCtx(reqCtx);

        try {
            Method method = datastoreActionAbstract.getClass().getMethod(action.getMethodName());
            reqMap = (Map<String, Object>) method.invoke(datastoreActionAbstract);
            if (reqMap == null) {
                reqMap = new HashMap<>();
            }
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof InvocationTargetException) {
                cause = cause.getCause();
            }

            if (cause instanceof AppException ex) {
                throw ex;
            } else if (cause instanceof ClassCastException) {
                throw new AppException(new ErrorResponse("data request invalid"));
            }
            throw e;
        }

        InstanceEntity instance = instanceInfo.getInstance();

        // for test
        String regionId = EnvConfig.TEST_REGION_DEV;
        if (!"dev".equals(regionId)) {
            regionId = instance.getRegionId();
        }
        // end

        if (action.isReturnValue()) {
            log.info("response raw => {}", reqMap);
            return Collections.singletonMap("response", new JSONObject(reqMap).toString());
        }

        reqMap.put("instanceId", instanceId);
        reqMap.put("datastoreModeCode", instanceInfo.getDatastoreModeCode());
        reqMap.put("datastoreName", instanceInfo.getDatastoreName());
        reqMap.put("datastore", instanceInfo.getDatastoreCode());
        reqMap.put("orgId", instance.getOrgId());
        reqMap.put("regionId", regionId);
        reqMap.put("projectId", instance.getProjectId());
        reqMap.put("userRequest", reqCtx.getUserId());
        if (reqCtx.getClientSocketId() != null) {
            reqMap.put("clientInfo", Collections.singletonMap("clientSocketId", reqCtx.getClientSocketId()));
        }

        TaskManagerRequest taskManagerRequest = new TaskManagerRequest();
        taskManagerRequest.setServiceId(action.getServiceId());
        taskManagerRequest.setData(CommonUtils.toJson(reqMap));

        log.info("send executeAction to task-manager => {}", CommonUtils.toJson(taskManagerRequest.toMap()));
        rabbitTemplate.convertAndSend(RabbitMQConfig.DBAAS_TASK_MANAGER, RabbitMQConfig.ROUTING_KEY_TASK_MANAGER,
                taskManagerRequest.toMap());

        return new HashMap<>();
    }

    private void validateActionByStatus(InstanceInfo instanceInfo, InstanceAction action) {
        InstanceStatus currentStatusInstance = InstanceStatus.get(instanceInfo.getInstance().getStatus());
        if (!InstanceStatus.RUNNING.equals(currentStatusInstance)) {
            switch (currentStatusInstance) {
                case UNKNOWN, WAITING, ERROR, DELETING -> {
                    if (action.equals(InstanceAction.DELETE_INSTANCE)) {
                        return;
                    }
                }
                case SHUTDOWN -> {
                    if (action.equals(InstanceAction.DELETE_INSTANCE) ||
                            action.equals(InstanceAction.RESTART_INSTANCE) ||
                            action.equals(InstanceAction.START_INSTANCE)) {
                        return;
                    }
                }
                case WARNING -> {
                    if (action.equals(InstanceAction.DELETE_INSTANCE) ||
                            action.equals(InstanceAction.RESTART_INSTANCE)) {
                        return;
                    }
                }
            }

            log.warn("Not execute action, the current state of instance[{}] is {}",
                    instanceInfo.getInstance().getId(), instanceInfo.getInstance().getStatus());
            throw new AppException(new ErrorResponse("Action invalid with current status of instance"));
        }
    }

    private InstanceInfo getInstanceInfo(String instanceId, RequestInfo requestInfo) {
        InstanceEntity instance = instanceRepository.findByIdAndOrgIdAndProjectIdAndDeletedAtIsNull(
                        instanceId, requestInfo.getOrgId(), requestInfo.getProjectId())
                .orElseThrow(() -> new AppException(new ErrorResponse("instance not found")));

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
                .datastoreModeName(datastoreMode.getName())
                .datastoreModeCode(datastoreMode.getCode())
                .datastoreModeId(datastoreMode.getId())
                .build();
    }

}
