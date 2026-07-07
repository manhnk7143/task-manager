package com.ctel.dbaas.dto.resource;

import lombok.Data;

@Data
public class ResourceStatus {

    private Integer used = 0;

    private Integer limit;

}
