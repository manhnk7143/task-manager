package com.ctel.dbaas.dto.cmc_cloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class QuotaInfo {

    @JsonProperty("dbv2_cpu")
    private Integer dbv2Cpu;

    @JsonProperty("dbv2_ram")
    private Integer dbv2Ram;

    @JsonProperty("dbv2_system_disk_gb")
    private Integer dbv2SystemDiskGb;

    @JsonProperty("dbv2_volume_gb")
    private Integer dbv2VolumeGb;

    @JsonProperty("dbv2_eip")
    private Integer dbv2Eip;

}
