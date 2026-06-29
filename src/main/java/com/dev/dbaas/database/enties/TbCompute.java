package com.dev.dbaas.database.enties;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TbCompute implements java.io.Serializable{
    @Getter
    @Setter
    private String id;
    @Getter @Setter
    private LocalDateTime createdAt;
    @Getter @Setter
    private LocalDateTime updatedAt;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String flavorId;
    @Getter @Setter
    private String glanceImageId;
    @Getter @Setter
    private String instanceId;
    @Getter @Setter
    private String neutronSecurityGroupId;
    @Getter @Setter
    private String novaInstanceId;
    @Getter @Setter
    private String orgId;
    @Getter @Setter
    private String projectId;
    @Getter @Setter
    private String role;
    @Getter @Setter
    private String regionId;
    @Getter @Setter
    private String monitorResourceId;
    @Getter @Setter
    private String statusAgentMonitor;
    @Getter @Setter
    private String zoneName;
    @Getter @Setter
    private String status;
    @Getter @Setter
    private LocalDateTime deletedAt;
    @Getter @Setter
    private Boolean sendLogResource;
}
