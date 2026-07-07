package com.ctel.dbaas.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GrpcRequestInfo {

    private String userId;

    private String tokenBarbican;

    private String orgId;

    private String portalProjectId;

}
