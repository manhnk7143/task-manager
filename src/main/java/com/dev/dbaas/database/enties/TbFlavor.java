package com.dev.dbaas.database.enties;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


public class TbFlavor implements java.io.Serializable{
    @Getter
    @Setter
    private String id;
    @Getter @Setter
    private LocalDateTime createdAt;
    @Getter @Setter
    private LocalDateTime updatedAt;
    @Getter @Setter
    private String flavorName;
    @Getter @Setter
    private String osFlavorId;
    @Getter @Setter
    private int ram;
    @Getter @Setter
    private int vCpus;
    @Getter @Setter
    private int disk;
}
