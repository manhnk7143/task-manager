package com.ctel.dbaas.dto.compute;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateComputeInfo {

    private String computeId;

    private String agentId;

    private String encryptedKey;
}
