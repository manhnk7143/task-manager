package com.ctel.dbaas.dto.cmc_cloud;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegionInfo {

    private List<ZoneInfo> listZone;

    @Data
    public static class ZoneInfo {
        private String region;
        private String zoneName;

        @JsonProperty("sold_out")
        private boolean soldOut;

        @JsonProperty("default")
        private int isDefault;
    }

}
