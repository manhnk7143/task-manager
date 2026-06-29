package com.dev.dbaas.database.enties;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TbAgent implements java.io.Serializable {

    @Getter @Setter
    private String id;
    @Getter @Setter
    private LocalDateTime createdAt;
    @Getter @Setter
    private LocalDateTime updatedAt;
    @Getter @Setter
    private String agentVersion;
    @Getter @Setter
    private Integer currentVersion;
    @Getter @Setter
    private String computeId;
    @Getter @Setter
    private String encryptedKey;
    @Getter @Setter
    private String instanceId;
    @Getter @Setter
    private String agentFirmwareId;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String orgId;
    @Getter @Setter
    private String projectId;
    @Getter @Setter
    private String status;
    @Getter @Setter
    private String cmdInstallMonitor;
    @Getter @Setter
    private String userData;
}
