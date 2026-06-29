package com.dev.dbaas.database.enties;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;


public class TbBackup implements java.io.Serializable{
    @Getter
    @Setter
    private String id;
    @Getter @Setter
    private LocalDateTime createdAt;
    @Getter @Setter
    private LocalDateTime updatedAt;
    @Getter @Setter
    private String agentId;
    @Getter @Setter
    private LocalDateTime backupEndAt;
    @Getter @Setter
    private LocalDateTime backupStartAt;
    @Getter @Setter
    private String checksum;
    @Getter @Setter
    private String configuration;
    @Getter @Setter
    private LocalDateTime deletedAt;
    @Getter @Setter
    private String endpoint;
    @Getter @Setter
    private String instanceId;
    @Getter @Setter
    private String message;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String orgId;
    @Getter @Setter
    private String pathFile;
    @Getter @Setter
    private Long size;
    @Getter @Setter
    private String status;
    @Getter @Setter
    private String backupStrategyId;
    @Getter @Setter
    private String backupScheduleId;
    @Getter @Setter
    private String datastoreVersionId;
    @Getter @Setter
    private String fileName;
}
