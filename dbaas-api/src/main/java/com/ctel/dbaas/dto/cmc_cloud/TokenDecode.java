package com.ctel.dbaas.dto.cmc_cloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenDecode {

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("domain_name")
    private String domainName;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("expires_at")
    private String expiresAt;

    @JsonProperty("token")
    private String token;

}
