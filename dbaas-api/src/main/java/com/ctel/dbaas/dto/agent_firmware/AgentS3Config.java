package com.ctel.dbaas.dto.agent_firmware;

import lombok.Data;

@Data
public class AgentS3Config {

    private String type;

    private String region;

    private String endPoint;

    private String accessKey;

    private String secretKey;

    private String bucketName;

    private String objectKey;

}
