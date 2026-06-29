package com.dev.dbaas.database.enties;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TbDatastoreConfiguration implements java.io.Serializable{
    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    private LocalDateTime createdAt;
    @Getter
    @Setter
    private LocalDateTime updatedAt;
    @Getter
    @Setter
    private String afterApply;
    @Getter
    @Setter
    private String beforeApply;
    @Getter
    @Setter
    private String datastoreModeId;
    @Getter
    @Setter
    private String defaultValue;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private String paramName;
    @Getter
    @Setter
    private String rangeValue;
    @Getter
    @Setter
    private String status;
    @Getter
    @Setter
    private String suggest;
    @Getter
    @Setter
    private String typeValue;
}
