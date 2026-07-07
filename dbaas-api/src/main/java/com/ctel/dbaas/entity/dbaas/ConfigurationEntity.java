package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_configuration")
@Getter
@Setter
public class ConfigurationEntity extends BaseEntity {

    @Column(name = "param_name", length = 128, nullable = false)
    private String paramName;

    @Column(name = "param_value", nullable = false)
    private String paramValue;

    @Column(name = "group_configuration_id", length = 36, nullable = false)
    private String groupConfigurationId;

    @Column(name = "datastore_configuration_id", length = 36, nullable = false)
    private String datastoreConfigurationId;

    @Column(name = "org_id", nullable = false)
    private String orgId;

}
