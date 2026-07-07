package com.ctel.dbaas.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceUsedRes {

    private Integer cpu = 0;

    private Integer ram = 0;

    private Integer systemDisk = 0;

    private Integer volumeGb = 0;

}
