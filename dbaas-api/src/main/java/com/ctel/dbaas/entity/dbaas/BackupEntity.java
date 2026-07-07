package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_backup")
@Getter
@Setter
public class BackupEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "agent_id", length = 36)
    private String agentId;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "path_file")
    private String pathFile;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "configuration")
    private String configuration = "{}";

    @Column(name = "datastore_code", length = 50)
    private String datastoreCode;

    @Column(name = "datastore_version", length = 50)
    private String datastoreVersion;

    @Column(name = "datastore_mode", length = 50)
    private String datastoreMode;

    @Column(name = "backup_mode", length = 50)
    private String backupMode;

    @Column(name = "size")
    private Long size = 0L; // byte

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "message", length = 512)
    private String message;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "datastore_version_id", length = 36)
    private String datastoreVersionId;

    @Column(name = "instance_id", length = 36)
    private String instanceId;

    @Column(name = "backup_strategy_id", length = 36)
    private String backupStrategyId;

    @Column(name = "org_id")
    private String orgId;

    @Column(name = "backup_schedule_id", length = 36)
    private String backupScheduleId;

    @Column(name = "backup_start_at", columnDefinition = "datetime")
    private LocalDateTime backupStartAt;

    @Column(name = "backup_end_at", columnDefinition = "datetime")
    private LocalDateTime backupEndAt;

    @Column(name = "deleted_at", columnDefinition = "datetime")
    private LocalDateTime deletedAt;

}
