package com.ctel.dbaas.service.grpc_monitoring.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorResourceReq {

    private String groupId;

    private String resourceId;

    private String resourceName;

    private String groupName;

}
