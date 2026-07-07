package com.ctel.dbaas.service.grpc_monitoring.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GraphInfrasReq {

    private String orgId;

    private String projectId;

    private String resourceValue;

    private String token;

    private String regionId;

    private String resourceName;

    private String instanceId;

    private String dimensionValue;
}
