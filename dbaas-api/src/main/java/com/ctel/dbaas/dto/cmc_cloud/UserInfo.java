package com.ctel.dbaas.dto.cmc_cloud;

import com.ctel.dbaas.utils.CommonUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo implements Serializable {

    private String id;

    private long createdTimestamp;

    private String username;

    private boolean enabled;

    private boolean totp;

    private boolean emailVerified;

    private String firstName;

    private String lastName;

    private Map<String, Object> attributes;

    private long notBefore;

    private Map<String, Object> access;

    public String toJson() {
        return CommonUtils.toJson(this);
    }

}
