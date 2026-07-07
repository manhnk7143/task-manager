package com.ctel.dbaas.service.grpc_monitoring.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class HealthCheckMonitorReq {

    private String teamCodeId;

    private String token;

    private String regionId;

    private String projectId;

    private String resourceId;
}
