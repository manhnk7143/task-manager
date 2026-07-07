package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_datastore")
@Getter
@Setter
public class DatastoreEntity extends BaseEntity {

    @Column(name = "default_version_id", length = 36)
    private String defaultVersionId;

    @Column(name = "name", length = 100, unique = true)
    private String name;

    @Column(name = "code", length = 100, unique = true)
    private String code;

    @Column(name = "tag", length = 50)
    private String tag;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

}
