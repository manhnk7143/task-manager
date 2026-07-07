package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_backup_strategy")
@Getter
@Setter
public class BackupStrategyEntity extends BaseEntity {

    @Column(name = "type")
    private String type;

    @Column(name = "is_actived")
    private Boolean active;

    @Column(name = "configuration")
    private String configuration = "{}";

}
