package com.dev.dbaas.database.enties;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TbAgentHeartbeat implements java.io.Serializable{
    @Getter
    @Setter
    private String id;
    @Getter @Setter
    private LocalDateTime createdAt;
    @Getter @Setter
    private LocalDateTime updatedAt;
    @Getter @Setter
    private String agentId;
    @Getter @Setter
    private String computeId;
    @Getter @Setter
    private LocalDateTime deletedAt;
    @Getter @Setter
    private String instanceId;
    @Getter @Setter
    private String orgId;
    @Getter @Setter
    private String projectId;
}
