package com.dev.dbaas.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class NeutronPort {

    @Getter @Setter
    private String portId;
    @Getter @Setter
    private String ipAddress;


}
