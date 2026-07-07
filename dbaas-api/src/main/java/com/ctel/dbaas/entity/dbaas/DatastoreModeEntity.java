package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_datastore_mode",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "datastore_version_id"})})
@Getter
@Setter
public class DatastoreModeEntity extends BaseEntity {

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "code", length = 100, nullable = false)
    private String code;

    @Column(name = "datastore_version_id", length = 36, nullable = false)
    private String datastoreVersionId;

    @Column(name = "requirement", length = 300, nullable = false)
    private String requirement = "{}";

    @Column(name = "deployment_steps", length = 400, nullable = false)
    private String deploymentSteps = "{}";

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "tag", length = 50)
    private String tag;

}
