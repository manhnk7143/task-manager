package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_backup_schedule")
@Getter
@Setter
public class BackupScheduleEntity extends BaseEntity {

    @Column(name = "instance_id", length = 36)
    private String instanceId;

    @Column(name = "status")
    private String status;

    @Column(name = "hour")
    private Integer hour;

    @Column(name = "minute")
    private Integer minute;

    @Column(name = "second")
    private Integer second;

    @Column(name = "time_zone")
    private String timeZone;

    @Column(name = "interval_num")
    private Integer intervalNum;

    @Column(name = "interval_type")
    private String intervalType;

    @Column(name = "datastore_code")
    private String datastoreCode;

    @Column(name = "keep_record_backup", nullable = false)
    private Integer keepRecordBackup;

    @Column(name = "last_backup_time", columnDefinition = "datetime")
    private LocalDateTime lastBackupTime;

    @Column(name = "next_backup_time", columnDefinition = "datetime")
    private LocalDateTime nextBackupTime;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "user_id", nullable = false)
    private String userId;
}