package com.ctel.dbaas.common.enums;

import lombok.Getter;

public enum Status {

    ACTIVE("active"), INACTIVE("inactive");

    @Getter
    private final String status;

    Status(String status) {
        this.status = status;
    }

}
