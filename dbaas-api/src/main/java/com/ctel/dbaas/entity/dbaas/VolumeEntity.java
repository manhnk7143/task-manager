package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_volume")
@Getter
@Setter
public class VolumeEntity extends BaseEntity {

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "cinder_volume_id", length = 36)
    private String cinderVolumeId;

    @Column(name = "size", nullable = false)
    private Integer size;

    @Column(name = "format", nullable = false)
    private String format;

    @Column(name = "compute_id", nullable = false)
    private String computeId;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "zone_name", length = 50)
    private String zoneName;

}
