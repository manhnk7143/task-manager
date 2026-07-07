package com.ctel.dbaas.dto.agent_firmware;

import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import lombok.Data;
import lombok.SneakyThrows;

import java.lang.reflect.Field;

@Data
public class CreateAgentFirmwareReq {

    private String objectKey;

    private String agentVersion;

    private String osSupport;

    private String configuration;

    private String state;

    private String apiKey;

    @SneakyThrows
    public void validate() {
        Class<?> clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(this);
            if (value == null) {
                throw new AppException(new ErrorResponse("%s cannot be null", field.getName()));
            }
        }
    }

}
