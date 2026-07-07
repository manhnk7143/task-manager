package com.ctel.dbaas.dto.load_balancer;

import lombok.Data;

@Data
public class ReplicasLbRes {

    private String loadBalancerId;

    private String parentId;

    private String status;

}
