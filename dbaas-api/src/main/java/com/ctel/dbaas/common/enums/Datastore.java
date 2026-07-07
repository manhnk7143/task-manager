package com.ctel.dbaas.common.enums;

import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Arrays;

@Getter
public enum Datastore {

    REDIS("redis"),
    MONGODB("mongodb"),
    POSTGRESQL("postgresql"),
    API_GATEWAY("api_gateway");

    private final String datastoreName;

    Datastore(String datastoreName) {
        this.datastoreName = datastoreName;
    }

    @SneakyThrows
    public static Datastore get(String datastoreName) {
        return Arrays.stream(Datastore.values())
                .filter(value -> datastoreName.equals(value.getDatastoreName()))
                .findFirst()
                .orElseThrow(() -> new Exception("Datastore " + datastoreName + " not support"));
    }


}
