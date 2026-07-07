package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_socket_info")
@Getter
@Setter
public class SocketInfoEntity extends BaseEntity {

    @Column(name = "socket_client_id", length = 36)
    private String socketClientId;

    @Column(name = "org_id", nullable = false, length = 50)
    private String orgId;

    @Column(name = "region_id", nullable = false, length = 50)
    private String regionId;

    @Column(name = "project_id", nullable = false, length = 50)
    private String projectId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

}
