package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_resource")
@Getter
@Setter
public class ResourceEntity extends BaseEntity {

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "region_id", nullable = false)
    private String regionId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "resource_used", columnDefinition = "TEXT")
    private String resourceUsed = "{}";

}
