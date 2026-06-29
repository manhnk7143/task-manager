package com.dev.dbaas.config;

import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Arrays;

public enum Datastore {

    REDIS("redis"),
    MONGO_DB("mongodb");

    @Getter
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
