package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_network")
@Getter
@Setter
public class NetworkEntity extends BaseEntity {

    @Column(name = "mode", nullable = false)
    private String mode = "system"; // user, system

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "neutron_port_id", length = 36)
    private String neutronPortId;

    @Column(name = "neutron_subnet_id", length = 36)
    private String neutronSubnetId;

    @Column(name = "neutron_vpc_id", length = 36)
    private String neutronVpcId;

    @Column(name = "neutron_security_group_id", length = 36)
    private String neutronSgId;

    @Column(name = "compute_id", length = 36)
    private String computeId;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "deleted_at", columnDefinition = "datetime")
    private LocalDateTime deletedAt;
}
