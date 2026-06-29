package com.dev.dbaas.database.enties;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TbDatastoreVersion implements java.io.Serializable{
    @Getter
    @Setter
    private String id;
    @Getter @Setter
    private LocalDateTime createdAt;
    @Getter @Setter
    private LocalDateTime updatedAt;
    @Getter @Setter
    private String datastoreId;
    @Getter @Setter
    private String glanceImageTags;
    @Getter @Setter
    private String platformSupports;
    @Getter @Setter
    private String provider;
    @Getter @Setter
    private String repoInformation;
    @Getter @Setter
    private String status;
    @Getter @Setter
    private String version;
}
