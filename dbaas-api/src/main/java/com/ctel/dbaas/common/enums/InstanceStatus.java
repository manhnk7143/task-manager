package com.ctel.dbaas.common.enums;

import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Arrays;

@Getter
public enum InstanceStatus {
    BUILDING("building"),
    WAITING("waiting"),
    WARNING("warning"),
    STARTING("starting"),
    DELETING("deleting"),
    ERROR("error"),

    // status from agent
    RUNNING("running"),
    SHUTDOWN("shutdown"),
    PAUSED("paused"),
    UNKNOWN("unknown"),
    HEALTHY("healthy"),
    UPGRADING("upgrading"),
    RESTARTING("restarting");

    private final String status;

    InstanceStatus(String status) {
        this.status = status;
    }

    @SneakyThrows
    public static InstanceStatus get(String status) {
        return Arrays.stream(InstanceStatus.values())
                .filter(value -> status.equalsIgnoreCase(value.getStatus()))
                .findFirst()
                .orElse(null);
    }

}
