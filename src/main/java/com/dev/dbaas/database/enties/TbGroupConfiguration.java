package com.dev.dbaas.database.enties;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;


public class TbGroupConfiguration implements java.io.Serializable{
    @Getter
    @Setter
    private String id;
    @Getter @Setter
    private LocalDateTime createdAt;
    @Getter @Setter
    private LocalDateTime updatedAt;
    @Getter @Setter
    private String datastoreModeId;
    @Getter @Setter
    private LocalDateTime deletedAt;
    @Getter @Setter
    private String description;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String orgId;
    @Getter @Setter
    private boolean isDefault;
}
