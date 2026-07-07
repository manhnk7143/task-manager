package com.ctel.dbaas.dto.cmc_cloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadBalancerInfo {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("provisioning_status")
    private String provisioningStatus;

    @JsonProperty("operatingStatus")
    private String operating_status;

    @JsonProperty("availability_zone")
    private String zone;

}
