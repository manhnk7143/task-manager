package com.dev.dbaas.config;

import lombok.Getter;

public enum ComputeStatus {

    BUILDING("building"),
    STARTING("starting"),
    RUNNING("running"),
    ERROR("error"),
    AVAILABLE("available"),
    UNAVAILABLE("unavailable"),
    SHUTTING_DOWN("shutting down"),
    DELETED("deleted"),
    DISCONNECTED("disconnected");

    @Getter
    private final String name;

    ComputeStatus(String name) {
        this.name = name;
    }

}
