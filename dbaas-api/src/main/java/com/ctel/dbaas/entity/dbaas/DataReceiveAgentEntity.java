package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "tb_data_receive_agent")
@Data
public class DataReceiveAgentEntity extends BaseEntity {

    @Column(name = "instance_id", length = 36, nullable = false)
    private String instanceId;

    @Column(name = "users_info", columnDefinition = "TEXT")
    private String usersInfo;

    @Column(name = "databases_info", columnDefinition = "TEXT")
    private String databasesInfo;

}
