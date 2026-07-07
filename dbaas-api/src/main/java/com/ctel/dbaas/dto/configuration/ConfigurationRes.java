package com.ctel.dbaas.dto.configuration;

import lombok.Data;

@Data
public class ConfigurationRes {

    private String id;

    private String paramName;

    private String paramValue;

    private String defaultValue;

    private String valueRange;

    private String valueType;

    private String description;

    private String createdAt;

    private String updatedAt;

}
