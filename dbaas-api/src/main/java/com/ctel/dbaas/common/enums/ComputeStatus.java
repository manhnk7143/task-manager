package com.ctel.dbaas.common.enums;

import lombok.Getter;

@Getter
public enum ComputeStatus {

    BUILDING("building"),
    STARTING("starting"),
    ERROR("error"),
    AVAILABLE("available"),
    UNAVAILABLE("unavailable"),
    SHUTTING_DOWN("shutting down"),
    DELETED("deleted");

    private final String name;

    ComputeStatus(String name) {
        this.name = name;
    }

}
