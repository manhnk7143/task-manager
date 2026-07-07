package com.ctel.dbaas.dto.instance;

import com.ctel.dbaas.entity.dbaas.InstanceEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InstanceDeleteReq {

    private String instanceId;

    private String datastoreId;

    private String datastoreName;

    private String datastoreVersionId;

    private String datastoreVersion;

    private String datastoreModeId;

    private String datastoreMode;

}
