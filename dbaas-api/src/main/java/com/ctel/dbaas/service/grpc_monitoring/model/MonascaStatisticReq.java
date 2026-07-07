package com.ctel.dbaas.service.grpc_monitoring.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonascaStatisticReq {

    private String token;

    private String orgId;

    private String regionId;

    private String projectId;

    private Integer period;//optional

    private String dimensionValue;

    private String resourceValue;

    private MonitoredObject monitoredObject;

    private String metricValue;

    private String startTime;

    private String endTime;// optional

    private String statistics;

    @Data
    @Builder
    public static class MonitoredObject {
        private String resourceValue;
        private String attachmentId;
    }

}
