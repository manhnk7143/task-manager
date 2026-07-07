package com.ctel.dbaas.dto.openstack;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePortRes {

    private String portId;

    private String ipAddress;

    private String macAddress;

}
