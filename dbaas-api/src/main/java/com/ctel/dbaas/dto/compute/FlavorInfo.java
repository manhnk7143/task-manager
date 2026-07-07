package com.ctel.dbaas.dto.compute;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FlavorInfo {

    @JsonProperty("id")
    private String flavorId;

    @JsonProperty("name")
    private String flavorName;

    @JsonProperty("ram")
    private int ram;

    @JsonProperty("vcpus")
    private int vCpus;

    @JsonProperty("disk")
    private int disk;

}
