package com.ctel.dbaas.dto.openstack;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePortReq {

    private String name;

    private List<String> securityGroupId;

    private String vpcId;

    private String subnetId;

    private String ipAddress;

    private List<AllowAddressPair> allowAddressPair;

    @Data
    @AllArgsConstructor
    public static class AllowAddressPair {
        private String ipAddress;
        private String macAddress;
    }

}
