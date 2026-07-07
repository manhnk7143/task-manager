package com.ctel.dbaas.datastore.api_gateway.model;

import lombok.Getter;

@Getter
public enum ApiGatewayRole {

    DATABASE("database"),
    API_GATEWAY("api_gateway");

    private final String name;

    ApiGatewayRole(String name) {
        this.name = name;
    }

}
