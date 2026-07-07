package com.ctel.dbaas.socket.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientRequest {
    private String instanceId;
    private String action;
    private Map<String, Object> requestData;
}
