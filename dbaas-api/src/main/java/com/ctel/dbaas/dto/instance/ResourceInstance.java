package com.ctel.dbaas.dto.instance;

import lombok.Data;

@Data
public class ResourceInstance {

    private Integer vCpus = 0;

    private Integer ram = 0;

    private Integer disk = 0;

    private Integer volumeSize = 0;

}
