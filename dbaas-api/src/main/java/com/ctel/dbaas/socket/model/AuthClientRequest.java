package com.ctel.dbaas.socket.model;

import lombok.Data;

@Data
public class AuthClientRequest {

    private String orgId;

    private String regionId;

    private String projectId;

    private String userId;

    private String token;

//    private String instanceId;

}
