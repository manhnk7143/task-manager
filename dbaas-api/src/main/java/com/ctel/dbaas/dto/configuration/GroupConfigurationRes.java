package com.ctel.dbaas.dto.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class GroupConfigurationRes {

    private String id;

    private String name;

    private String description;

    private String datastoreMode;

    private String datastoreModeId;

    private String datastoreName;

    private String datastoreCode;

    private String datastoreVersion;

    private String datastoreVersionId;

    private String createdAt;

    private String updatedAt;

    private boolean groupDefault;

    private List<InstancesUseGroupConfig> listInstanceInfo;

    @Data
    @AllArgsConstructor
    public static class InstancesUseGroupConfig {
        private String id;
        private String name;
    }

}
