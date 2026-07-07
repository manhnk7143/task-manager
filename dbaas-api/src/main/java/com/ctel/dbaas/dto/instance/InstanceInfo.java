package com.ctel.dbaas.dto.instance;

import com.ctel.dbaas.entity.dbaas.InstanceEntity;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class InstanceInfo {
    private InstanceEntity instance;

    private String datastoreId;
    private String datastoreName;
    private String datastoreCode;

    private String datastoreVersionId;
    private String datastoreVersion;

    private String datastoreModeId;
    private String datastoreModeCode;
    private String datastoreModeName;
}
