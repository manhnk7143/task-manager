package com.ctel.dbaas.dto.configuration;

import lombok.Data;

import java.util.List;

@Data
public class GroupConfigRes {

    private String groupConfigId;

    private String datastoreModeId;

    private String name;

    private String description;

    private List<ConfigurationRes> configurations;

}
