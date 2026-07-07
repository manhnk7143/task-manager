package com.ctel.dbaas.entity.auto_scale;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_load_balancer")
@Getter
@Setter
public class LoadBalancerEntity extends BaseEntity {

    @Column(name = "load_balancer_id", length = 36)
    private String loadBalancerId;

    @Column(name = "origin_load_balancer_id", length = 36)
    private String originLoadBalancerId;

    @Column(name = "zone", length = 36)
    private String zone;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "region_id", nullable = false)
    private String regionId;

    @Column(name = "replicate_index")
    private Integer replicateIndex;

    @Column(name = "deleted_at", columnDefinition = "datetime")
    private LocalDateTime deletedAt;
}
