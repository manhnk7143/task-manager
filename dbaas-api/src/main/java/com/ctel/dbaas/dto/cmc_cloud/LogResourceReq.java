package com.ctel.dbaas.dto.cmc_cloud;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class LogResourceReq {

    private String serviceType;

    private String computeId;

    private String parentId;

    private String itemId;

    private String regionId;

    private String projectId;

    private String teamCode;

    private Map<String, Object> metaData = new HashMap<>();

}
