package com.ctel.dbaas.dto.backup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3StorageConfigDto {

    private String endpoint;

    private String region;

    private String accessKey;

    private String secretKey;

}
