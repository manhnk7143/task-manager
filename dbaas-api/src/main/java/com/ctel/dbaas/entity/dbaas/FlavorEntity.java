package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_flavor")
@Getter
@Setter
public class FlavorEntity extends BaseEntity {

    @Column(name = "flavor_name")
    private String flavorName;

    @Column(name = "os_flavor_id", length = 36, unique = true, nullable = false)
    private String osFlavorId;

    @Column(name = "ram")
    private int ram; // MB

    @Column(name = "v_cpus")
    private int vCpus;

    @Column(name = "disk")
    private int disk; // GB

}
