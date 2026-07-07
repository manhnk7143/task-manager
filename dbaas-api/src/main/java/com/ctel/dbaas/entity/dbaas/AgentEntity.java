package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_agent")
@Getter
@Setter
public class AgentEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "encrypted_key", nullable = false)
    private String encryptedKey;

    @Column(name = "agent_version", nullable = false, length = 10)
    private String agentVersion;

    @Column(name = "agent_firmware_id", length = 36)
    private String agentFirmwareId;

    @Column(name = "instance_id", length = 36)
    private String instanceId;

    @Column(name = "compute_id", length = 36)
    private String computeId;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "cmd_install_monitor", length = 500)
    private String cmdInstallMonitor;

    @Column(name = "user_data", columnDefinition = "TEXT")
    private String userData;

}
