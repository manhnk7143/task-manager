package com.ctel.dbaas.utils;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.util.*;

@Log4j2
public class CommonUtils {

    public static Map<String, Object> toMap(Object obj) {
        try {
            if (obj == null) {
                return new HashMap<>();
            }

            if (obj instanceof JSONObject) {
                return toMap((JSONObject) obj);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            return objectMapper.convertValue(obj, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public static Map<String, Object> toMap(JSONObject json) throws JSONException {
        Map<String, Object> map = new HashMap<>();

        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.get(key);

            if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }

            map.put(key, value);
        }

        return map;
    }

    public static <T> T toObject(Map<String, Object> value, Class<T> zClass) { // OK
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.convertValue(value, zClass);
        } catch (Exception e) {
            log.error("toObject ERROR convert[{}] to object[{}] - message[{}]", value, zClass.getName(), e.getMessage());
            return null;
        }
    }

    public static <T> T toObject(String jsonStr, Class<T> zClass) { // OK
        try {
            if (isValidJson(jsonStr)) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(jsonStr, zClass);
            }
        } catch (Exception e) {
            log.info("toObject ERROR => jsonStr[{}] - msg[{}]", jsonStr, e.getMessage());
        }
        return null;
    }

    public static Map<String, Object> toMap(String jsonStr) {
        if (!isValidJson(jsonStr)) {
            return new HashMap<>();
        }

        try {
            return toMap(new JSONObject(jsonStr));
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    @SneakyThrows
    public static String toJson(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper.writeValueAsString(obj);
    }

    public static String toJson(MessageOrBuilder messageOrBuilder) {
        try {
            return JsonFormat.printer().print(messageOrBuilder);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T convert(JSONObject jsonObject, Class<T> clazz) throws Exception {
        T obj = clazz.getDeclaredConstructor().newInstance();

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if (jsonObject.has(fieldName)) {
                Object value = jsonObject.get(fieldName);
                field.set(obj, value);
            }
        }

        return obj;
    }

    public static boolean isValidJson(String jsonString) {
        try {
            new ObjectMapper().readTree(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Pageable convertToPageable(int page, int size) {
        if (page < 0) {
            page = 0;
        }

        if (page > 0) {
            page = page - 1;
        }

        if (size <= 0) {
            size = 10;
        }

        return PageRequest.of(page, size, Sort.by("updatedAt").descending());
    }

    public static Pageable convertToPageable(int page, int size, String sortType, String... fieldToSort) {
        if (page < 0) {
            page = 0;
        }

        if (page > 0) {
            page = page - 1;
        }

        if (size <= 0) {
            size = 10;
        }

        if ("DESC".equals(sortType)) {
            return PageRequest.of(page, size, Sort.by(fieldToSort).descending());
        }

        return PageRequest.of(page, size, Sort.by(fieldToSort).ascending());
    }

    public static String generateString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    public static String calculateIntervalType(Integer hour, Integer minute, Integer second) {
        if (hour != null) {
            if (minute != null && second != null) {
                return "DAY";
            }
        } else if (minute != null) {
            if (second != null) {
                return "HOUR";
            }
        } else if (second != null) {
            return "MINUTE";
        }

        throw new AppException(new ErrorResponse("Time invalid"));
    }

    public static HttpStatus getHttpStatus(int statusCode) {
        try {
            return HttpStatus.valueOf(statusCode);
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    public static boolean isValidUUID(String uuidStr) {
        try {
            UUID.fromString(uuidStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void validatePwd(String pwd) {
        if (!pwd.matches(Constant.REGEX_PASSWORD)) {
            throw new AppException(new ErrorResponse("password must not contain special characters, length from 8 to 32 characters including upper and lower case letters and numbers"));
        }
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDigits(Object obj) {
        if (obj == null) {
            return false;
        }
        String str = obj.toString().trim();

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }
}
