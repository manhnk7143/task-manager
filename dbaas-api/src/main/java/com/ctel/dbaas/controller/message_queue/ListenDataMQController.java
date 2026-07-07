package com.ctel.dbaas.controller.message_queue;

import com.ctel.dbaas.common.TaskMngServiceType;
import com.ctel.dbaas.config.RabbitMQConfig;
import com.ctel.dbaas.controller.message_queue.handler.*;
import com.ctel.dbaas.dto.backup.TriggerBackupRequest;
import com.ctel.dbaas.dto.common.TaskManagerRequest;
import com.ctel.dbaas.service.BackupRestoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
public class ListenDataMQController {

    @Autowired
    private BackupRestoreService backupRestoreService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Map<String, HandlerBase<TaskManagerRequest>> MAP_SERVICE_TYPE = new HashMap<>();

    public ListenDataMQController(ResponseDbActionHandler responseDbActionHandler,
                                  UpdateStatusBackupHandler updateStatusBackupHandler,
                                  UpdateStatusChangeConfigHandler updateStatusChangeConfigHandler,
                                  UpdateStatusComputeHandler updateStatusComputeHandler,
                                  UpdateStatusInstanceHandler updateStatusInstanceHandler) {

        MAP_SERVICE_TYPE.put(TaskMngServiceType.RESPONSE_DB_ACTION, responseDbActionHandler);
        MAP_SERVICE_TYPE.put(TaskMngServiceType.UPDATE_STATUS_BACKUP, updateStatusBackupHandler);
        MAP_SERVICE_TYPE.put(TaskMngServiceType.UPDATE_STATUS_CHANGE_CONFIG, updateStatusChangeConfigHandler);
        MAP_SERVICE_TYPE.put(TaskMngServiceType.UPDATE_STATUS_COMPUTE, updateStatusComputeHandler);
        MAP_SERVICE_TYPE.put(TaskMngServiceType.UPDATE_STATUS_INSTANCE, updateStatusInstanceHandler);
    }

    @RabbitListener(queues = {RabbitMQConfig.TRIGGER_BACKUP})
    public void handleTrigger(@Payload Map<String, Object> reqMap) {
        log.info("Start handle request from queue trigger_auto_backup: [{}]", reqMap);
        try {
            TriggerBackupRequest req = objectMapper.convertValue(reqMap, TriggerBackupRequest.class);
            backupRestoreService.executeAutoBackup(req);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("handleTrigger error : [{}]", e.getMessage());
        }

    }

    @RabbitListener(queues = {RabbitMQConfig.DBAAS_API})
    public void onDataDBaaSAPI(@Payload Map<String, Object> reqMap,
                               @Headers MessageHeaders headers,
                               Message message) {
        try {
            TaskManagerRequest req = objectMapper.convertValue(reqMap, TaskManagerRequest.class);
            req.decodePacket();

            HandlerBase<TaskManagerRequest> handlerBase = MAP_SERVICE_TYPE.get(req.getServiceId());
            if (handlerBase != null) {
                handlerBase.handle(req);
            }
        } catch (Exception e) {
            log.error("ERROR handle from queue dbaas.api : error[{}] - data[{}]", e.getMessage(), reqMap);
        }
    }

}
