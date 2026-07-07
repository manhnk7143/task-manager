package com.ctel.dbaas.entity.dbaas;

import com.ctel.dbaas.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_agent_firmware")
@Getter
@Setter
public class AgentFirmwareEntity extends BaseEntity {

    @Column(name = "build_number")
    private Integer buildNumber;

    @Column(name = "os_support")
    private String osSupport;

    @Column(name = "state")
    private String state;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "configuration", length = 1000)
    private String configuration;

    @Column(name = "link_download", length = 1000)
    private String linkDownload;

}
