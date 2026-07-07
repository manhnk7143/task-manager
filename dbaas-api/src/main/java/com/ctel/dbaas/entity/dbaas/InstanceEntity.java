package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_instance")
@Getter
@Setter
public class InstanceEntity extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "flavor_id", length = 36)
    private String flavorId;

    @Column(name = "datastore_id", length = 36, nullable = false)
    private String datastoreId;

    @Column(name = "datastore_version_id", length = 36, nullable = false)
    private String datastoreVersionId;

    @Column(name = "datastore_mode_id", length = 36, nullable = false)
    private String datastoreModeId;

    @Column(name = "group_configuration_id", length = 36)
    private String groupConfigurationId;

    @Column(name = "server_group_id", length = 36)
    private String serverGroupId;

    @Column(name = "subnet_id", length = 36)
    private String subnetId;

    @Column(name = "network_id", length = 36)
    private String networkId;

    @Column(name = "vpc_id", length = 36)
    private String vpcId;

    @Column(name = "apply_group_config")
    private Boolean applyGroupConfig = false;

    @Column(name = "neutron_security_group_client_ids", length = 400)
    private String neutronSecurityGroupClientIds = "[]";

    @Column(name = "resource_package", length = 500)
    private String resourcePackage = "{}";

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "message")
    private String message = "";

    @Column(name = "volume_size")
    private Integer volumeSize;

    @Column(name = "status_monitor_agent", length = 50)
    private String statusMonitorAgent = "disable";

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "org_id", nullable = false)
    private String orgId;

    @Column(name = "region_id", nullable = false)
    private String regionId;

    @Column(name = "deleted_at", columnDefinition = "datetime")
    private LocalDateTime deletedAt;

    @Column(name = "instance_config_encrypt", length = 500)
    private String instanceConfigEncrypt;


    @PrePersist
    private void prePersist() {
        if (StringUtils.isBlank(this.neutronSecurityGroupClientIds)) {
            this.neutronSecurityGroupClientIds = "[]";
        }
    }

}
