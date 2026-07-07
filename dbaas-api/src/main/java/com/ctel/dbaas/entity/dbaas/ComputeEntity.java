package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_compute")
@Getter
@Setter
public class ComputeEntity extends BaseEntity {

    @Column(name = "nova_instance_id", length = 36)
    private String novaInstanceId;

    @Column(name = "glance_image_id", length = 36)
    private String glanceImageId;

    @Column(name = "neutron_security_group_id", length = 36)
    private String neutronSecurityGroupId;

    @Column(name = "flavor_id", length = 36)
    private String flavorId;

    @Column(name = "monitor_resource_id", length = 36)
    private String monitorResourceId;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "instance_id", length = 36, nullable = false)
    private String instanceId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "zone_name", length = 50)
    private String zoneName;

    @Column(name = "region_id", nullable = false)
    private String regionId;

    @Column(name = "deleted_at", columnDefinition = "datetime")
    private LocalDateTime deletedAt;

}
