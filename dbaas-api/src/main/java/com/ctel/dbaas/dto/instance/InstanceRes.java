package com.ctel.dbaas.dto.instance;

import lombok.Data;

@Data
public class InstanceRes {

    private String id;

    private String name;

    private Integer vCpus = 0;

    private Integer ram = 0;

    private Integer disk = 0;

    private Integer volumeSize = 0;

    private String groupConfigId;

    private String datastoreName;

    private String datastoreCode;

    private String datastoreVersion;

    private String datastoreVersionId;

    private String datastoreMode;

    private String datastoreModeId;

    private String status;

    private String createdAt;

    private String updatedAt;

}
