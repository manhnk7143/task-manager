package com.dev.dbaas.database.enties;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;


public class TbNetwork implements java.io.Serializable{
    @Getter
    @Setter
    private String id;
    @Getter @Setter
    private LocalDateTime createdAt;
    @Getter @Setter
    private LocalDateTime updatedAt;
    @Getter @Setter
    private String computeId;
    @Getter @Setter
    private String ipAddress;
    @Getter @Setter
    private String mode;
    @Getter @Setter
    private String neutronPortId;
    @Getter @Setter
    private String neutronSecurityGroupId;
    @Getter @Setter
    private String neutronSubnetId;
    @Getter @Setter
    private String neutronVpcId;
    @Getter @Setter
    private String orgId;
    @Getter @Setter
    private String projectId;
    @Getter @Setter
    private String status;
    @Getter @Setter
    private LocalDateTime deletedAt;
    @Getter @Setter
    private Boolean sendLogResource;

}
