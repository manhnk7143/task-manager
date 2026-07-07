package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_datastore_configuration")
@Getter
@Setter
public class DatastoreConfigurationEntity extends BaseEntity {

    @Column(name = "suggest")
    private String suggest;

    @JsonProperty("param_name")
    @Column(name = "param_name")
    private String paramName;

    @JsonProperty("default_value")
    @Column(name = "default_value", nullable = false)
    private String defaultValue;

    @JsonProperty("value_range")
    @Column(name = "range_value")
    private String rangeValue;

    @JsonProperty("value_type")
    @Column(name = "type_value", length = 100)
    private String typeValue;

    @JsonProperty("description")
    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "before_apply")
    private String beforeApply;

    @Column(name = "after_apply")
    private String afterApply;

    @JsonProperty(value = "need_restart")
    @Column(name = "need_restart")
    private Boolean needRestart = false;

    @Column(name = "datastore_mode_id", length = 36)
    private String datastoreModeId;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

}
