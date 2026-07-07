package com.ctel.dbaas.dto.load_balancer;

import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class CreateReplicasLbReq {

    private String loadBalancerId;

    private Integer replicasServer;

    private String zones;

    private List<String> lstZone = new ArrayList<>();

    public void validate() {
        if (loadBalancerId == null || loadBalancerId.isEmpty()) {
            throw new AppException(new ErrorResponse("loadBalancerId is required"));
        }

        if (zones == null || zones.isEmpty()) {
            throw new AppException(new ErrorResponse("zones is required"));
        }

        if (replicasServer == null || replicasServer < 1 || replicasServer > 10) {
            throw new AppException(new ErrorResponse("replicasServer must be between 1 and 10"));
        }

        String[] zoneArr = zones.split(",");
        lstZone = Arrays.asList(zoneArr);
    }
}
