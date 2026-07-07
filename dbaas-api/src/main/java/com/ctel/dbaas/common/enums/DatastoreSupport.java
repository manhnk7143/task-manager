package com.ctel.dbaas.common.enums;

import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

@Getter
public enum DatastoreSupport {

    POSTGRESQL("postgresql", "Postgresql", List.of("16.2", "14.9"), "postgresql", "POSTGRESQL"),
    REDIS("redis", "Redis", List.of("6.0", "7.0"), "redisdb", "REDIS"),
    MONGODB("mongodb", "MongoDB", List.of("6.0", "7.0"), "mongo", "MONGO"),
    KAFKA("kafka", "Kafka", List.of("3.7"), "kafka_consumer", "KAFKA"),
    API_GATEWAY("api_gateway", "API Gateway", List.of("1.0"), "apigateway", "APIGATEWAY");

    private final String code;
    private final String name;
    private final List<String> versionSupport;
    private final String monitorResourceTypeName;
    private final String serviceType;

    DatastoreSupport(String code, String name, List<String> versionSupport, String monitorResourceTypeName, String serviceType) {
        this.code = code;
        this.name = name;
        this.versionSupport = versionSupport;
        this.monitorResourceTypeName = monitorResourceTypeName;
        this.serviceType = serviceType;
    }

    @SneakyThrows
    public static DatastoreSupport getOrThrow(String code, String version) {
        return Arrays.stream(DatastoreSupport.values())
                .filter(value -> code.equals(value.getCode()) && value.getVersionSupport().contains(version))
                .findFirst()
                .orElseThrow(() -> new Exception("datastore " + code + ":" + version + " not support"));
    }

    @SneakyThrows
    public static DatastoreSupport getOrThrow(String code) {
        return Arrays.stream(DatastoreSupport.values())
                .filter(value -> code.equals(value.getCode()))
                .findFirst()
                .orElseThrow(() -> new Exception("datastore " + code + " not support"));
    }

    public static DatastoreSupport get(String code) {
        return Arrays.stream(DatastoreSupport.values())
                .filter(value -> code.equals(value.getCode()))
                .findFirst()
                .orElse(null);
    }

    public static List<String> validateDatastoreCodes(String datastoreCodes) {
        if (StringUtils.isBlank(datastoreCodes)) {
            throw new AppException(new ErrorResponse("datastoreCodes cannot be empty"));
        }
        List<String> lstDatastoreCode = List.of(datastoreCodes.split(","));
        if (lstDatastoreCode.isEmpty()) {
            throw new AppException(new ErrorResponse("datastoreCodes cannot be empty"));
        }
        for (String code : lstDatastoreCode) {
            DatastoreSupport.getOrThrow(code);
        }

        return lstDatastoreCode;
    }

//    public static List<String> getListDatastoreCodeByName(String datastoreNames) {
//        if (StringUtils.isBlank(datastoreNames)) {
//            throw new AppException(new ErrorResponse("DatastoreCodes cannot be empty"));
//        }
//        List<String> lstDatastoreName = List.of(datastoreNames.split(","));
//        if (lstDatastoreName.isEmpty()) {
//            throw new AppException(new ErrorResponse("DatastoreCodes cannot be empty"));
//        }
//        List<String> lstDatastoreCode = new ArrayList<>();
//        for (String name : lstDatastoreName) {
//            DatastoreSupport datastoreSupport = DatastoreSupport.getByName(name);
//            lstDatastoreCode.add(datastoreSupport.getCode());
//        }
//        return lstDatastoreCode;
//    }
}
