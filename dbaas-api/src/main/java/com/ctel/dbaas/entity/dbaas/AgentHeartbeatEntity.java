package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_agent_heartbeat")
@Getter
@Setter
public class AgentHeartbeatEntity extends BaseEntity {

    @Column(name = "agent_id", length = 36)
    private String agentId;

    @Column(name = "instance_id", length = 36)
    private String instanceId;

    @Column(name = "compute_id", length = 36)
    private String computeId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "deleted_at", columnDefinition = "datetime")
    private LocalDateTime deletedAt;

}
