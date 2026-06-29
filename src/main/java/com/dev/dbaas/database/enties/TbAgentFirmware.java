package com.dev.dbaas.database.enties;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class TbAgentFirmware implements java.io.Serializable {

    @Getter @Setter
    private String id;
    @Getter @Setter
    private LocalDateTime createdAt;
    @Getter @Setter
    private LocalDateTime updatedAt;
    @Getter @Setter
    private int buildNumber;
    @Getter @Setter
    private String configuration;
    @Getter @Setter
    private String osSupport;
    @Getter @Setter
    private String linkDownload;
    @Getter @Setter
    private String filename;
    @Getter @Setter
    private String state;
}
