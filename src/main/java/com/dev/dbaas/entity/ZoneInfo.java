package com.dev.dbaas.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ZoneInfo {
    private String endpoint;
    private String username;
    private String password;
    private String domain;
    private String projectId;
    private String projectName;
    private String securityGroupIdManager;
    private String networkIdManager;
    private String subnetIdManager;
    private String applicationCredentialId;
    private String applicationCredentialSecret;
}
