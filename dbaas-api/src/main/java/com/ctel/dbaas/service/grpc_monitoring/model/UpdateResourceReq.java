package com.ctel.dbaas.service.grpc_monitoring.model;

import lombok.Data;

@Data
public class UpdateResourceReq {

    private String resourceId;

    private String resourceName;

    private String jsonConfig;

    private String resourceTypeName;

}
