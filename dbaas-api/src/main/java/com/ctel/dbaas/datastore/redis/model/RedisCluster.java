package com.ctel.dbaas.datastore.redis.model;

import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.utils.CommonUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedisCluster {

    private Integer numOfMasterServer;

    private Integer replicas;

    private Set<String> zones;

    private String password;

    public void validate() {
        if (this.zones == null || this.zones.isEmpty()) {
            throw new AppException(new ErrorResponse("zones is required"));
        }

        if (StringUtils.isBlank(this.password)) {
            throw new AppException(new ErrorResponse("password is required"));
        }
        CommonUtils.validatePwd(this.password);

        if (this.numOfMasterServer == null || this.replicas == null) {
            throw new AppException(new ErrorResponse("numOfMasterServer and replicas is required"));
        }

        if (this.numOfMasterServer < 3 || this.numOfMasterServer > 5) {
            throw new AppException(new ErrorResponse("numOfMasterServer must be greater than equal 3 or less than equal 5"));
        }

        if (this.replicas < 1 || this.replicas > 2) {
            throw new AppException(new ErrorResponse("replicas must be greater than 0 and less than equal 2"));
        }
    }

}
