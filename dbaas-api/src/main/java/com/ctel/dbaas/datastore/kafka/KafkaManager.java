package com.ctel.dbaas.datastore.kafka;

import com.ctel.dbaas.common.enums.DbAction;
import com.ctel.dbaas.datastore.DatastoreActionAbstract;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
public class KafkaManager extends DatastoreActionAbstract {

    private static final String MSG_NO_SUPPORT = "kafka not support this action";

    @Override
    public Map<String, Object> startInstance() {
        return Map.of();
    }

    @Override
    public Map<String, Object> promoteSlaveToMaster() {
        throw new AppException(new ErrorResponse(MSG_NO_SUPPORT));
    }

    @Override
    public Map<String, Object> setPassword() {
        throw new AppException(new ErrorResponse(MSG_NO_SUPPORT));
    }

    @Override
    public Map<String, Object> createBackup() {
        return Map.of();
    }

    @Override
    public Map<String, Object> getListUser() {
        throw new AppException(new ErrorResponse(MSG_NO_SUPPORT));
    }

    @Override
    public Map<String, Object> getListDatabase() {
        throw new AppException(new ErrorResponse(MSG_NO_SUPPORT));
    }

    @SneakyThrows
    @Override
    public Map<String, Object> dbAction() {
        String command = (String) this.getData().get("command");
        Map<String, Object> body = (Map<String, Object>) this.getData().get("body");
        DbAction.Kafka kafkaActionEnum = DbAction.Kafka.get(command);
        if (kafkaActionEnum == null) {
            throw new AppException(new ErrorResponse("Kafka action invalid"));
        }

        if (body == null) {
            throw new AppException(new ErrorResponse("Kafka request invalid"));
        }

        KafkaAction kafkaAction = new KafkaAction(
                (Map<String, Object>) this.getData().get("body"), this.getInstanceInfo(), computeRepository);

        Method method = kafkaAction.getClass().getMethod(kafkaActionEnum.getMethodName());
        Map<String, Object> reqMap = (Map<String, Object>) method.invoke(kafkaAction);
        if (reqMap == null) {
            reqMap = new HashMap<>();
        }

        Map<String, Object> mapRequest = new HashMap<>();
        mapRequest.put("command", kafkaActionEnum.getServiceId());
        mapRequest.put("body", reqMap);

        return mapRequest;
    }
}
