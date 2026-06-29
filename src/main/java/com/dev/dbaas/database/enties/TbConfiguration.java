package com.dev.dbaas.database.enties;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TbConfiguration implements java.io.Serializable{
    @Getter
    @Setter
    private String id;
    @Getter @Setter
    private LocalDateTime createdAt;
    @Getter @Setter
    private LocalDateTime updatedAt;
    @Getter @Setter
    private String datastoreConfigurationId;
    @Getter @Setter
    private String groupConfigurationId;
    @Getter @Setter
    private String orgId;
    @Getter @Setter
    private String paramName;
    @Getter @Setter
    private String paramValue;

}
