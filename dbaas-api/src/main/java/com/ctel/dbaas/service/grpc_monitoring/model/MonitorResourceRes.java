package com.ctel.dbaas.service.grpc_monitoring.model;

import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorResourceRes {
    private String resourceId;
    private String curlAgentMonitor = "";
    private ConfigRedisMonitorData config;

    public void valid() {
        if (StringUtils.isBlank(this.resourceId) || StringUtils.isBlank(this.curlAgentMonitor)) {
            throw new AppException(new ErrorResponse("monitor service response invalid"));
        }
    }

    @Data
    public static class ConfigRedisMonitorData {
        private Integer agentMonitorId;
        private String resourceTypeId;
        private String resourceTypeValue;
        private String config;
    }

}
