package com.ctel.dbaas.common;

import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import lombok.*;

import java.lang.reflect.Field;
import java.util.Locale;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestInfo {

    private String orgId;

    private String regionId;

    private String projectId;

    private String userId;

    private String token;

    private Locale locale;

    private String clientSocketId;

    @SneakyThrows
    public void validate(boolean skipSocketId) throws IllegalAccessException {
        Class<?> clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(this);
            if (value == null) {
                if (skipSocketId && field.getName().equals("clientSocketId")) {
                    continue;
                }
                throw new AppException(new ErrorResponse("%s cannot be null", field.getName()));
            }
        }
    }

    @SneakyThrows
    public void validate() {
        this.validate(true);
    }

}
