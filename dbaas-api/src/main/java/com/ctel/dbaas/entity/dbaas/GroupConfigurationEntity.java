package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_group_configuration")
@Getter
@Setter
public class GroupConfigurationEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "datastore_mode_id", length = 36, nullable = false)
    private String datastoreModeId;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "deleted_at", columnDefinition = "datetime")
    private LocalDateTime deletedAt;

}
