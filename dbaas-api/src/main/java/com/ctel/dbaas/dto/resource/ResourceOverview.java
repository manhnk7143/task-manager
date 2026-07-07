package com.ctel.dbaas.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ResourceOverview {

    private Map<String, ResourceStatus> resource;

    @Data
    @AllArgsConstructor
    public static class ResourceStatus {

        private Integer used = 0;

        private Integer limit;

    }

}
