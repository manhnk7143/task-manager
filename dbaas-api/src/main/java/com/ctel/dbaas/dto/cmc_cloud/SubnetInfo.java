package com.ctel.dbaas.dto.cmc_cloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubnetInfo {

    @JsonProperty("network_id")
    private String networkId;

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("name")
    private String name;

}
