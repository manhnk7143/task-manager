package com.ctel.dbaas.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceUsedReq {

    private Integer cpu;

    private Integer ram;

    private Integer systemDisk;

    private Integer volumeGb;
}
