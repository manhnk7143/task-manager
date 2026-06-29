package com.dev.dbaas.common;

import lombok.Getter;

@Getter
public enum Region {

    HN1("hn-1"), HCM1("hcm-1");
    private final String name;
    Region(String name) {
        this.name = name;
    }
}
