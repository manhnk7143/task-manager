package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_resource_instance")
@Getter
@Setter
public class ResourceInstanceEntity extends BaseEntity {

    @Column(name = "resource_id", length = 36, nullable = false)
    private String resourceId;

    @Column(name = "instance_id", length = 36, nullable = false, unique = true)
    private String instanceId;

    @Column(name = "resource_used", columnDefinition = "TEXT")
    private String resourceUsed = "{}";

    @Column(name = "deleted_at", columnDefinition = "datetime")
    private LocalDateTime deletedAt;

//    @Column(name = "action", length = 20)
//    private String action;
//
//    @Column(name = "status", length = 20)
//    private String status;

}
