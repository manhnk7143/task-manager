package com.dev.dbaas.database.enties;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


public class TbInstance implements java.io.Serializable{
    @Getter
    @Setter
    private String id;
    @Getter @Setter
    private LocalDateTime createdAt;
    @Getter @Setter
    private LocalDateTime updatedAt;
    @Getter @Setter
    private String flavorId;
    @Getter @Setter
    private String datastoreId;
    @Getter @Setter
    private String datastoreModeId;
    @Getter @Setter
    private String datastoreVersionId;
    @Getter @Setter
    private LocalDateTime deletedAt;
    @Getter @Setter
    private String groupConfigurationId;
    @Getter @Setter
    private String serverGroupId;
    @Getter @Setter
    private String subnetId;
    @Getter @Setter
    private String networkId;
    @Getter @Setter
    private String vpcId;
    @Getter @Setter
    private String message;
    @Getter @Setter
    private Integer volumeSize;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String orgId;
    @Getter @Setter
    private String projectId;
    @Getter @Setter
    private String regionId;
    @Getter @Setter
    private String resourcePackage;
    @Getter @Setter
    private String statusMonitorAgent;
    @Getter @Setter
    private String status;
    @Getter @Setter
    private String enableMonitor;
    @Getter @Setter
    private String neutronSecurityGroupClientIds;
    @Getter @Setter
    private String billingMode;
}
