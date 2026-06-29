package com.dev.dbaas.common;

import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Arrays;

@Getter
public enum DatastoreSupport {
    REDIS("redis", "redisdb"),
    MONGODB("mongodb", "mongo"),
    POSTGRESQL("postgresql", "postgresql"),
    KAFKA("kafka", "kafka_consumer"),
    MYSQL("mysql", "mysql"),
    API_GATEWAY("api_gateway", "api_gateway");

    private final String code;
    private final String monitorResourceTypeName;

    DatastoreSupport(String code, String monitorResourceTypeName) {
        this.code = code;
        this.monitorResourceTypeName = monitorResourceTypeName;
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
}
