package com.ctel.dbaas.datastore.redis.model;

import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.utils.CommonUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedisMasterSlave {

    private Set<String> zones;

    private String password;

    private Integer numOfSlaves;

    public void validate() {
        if (this.zones == null || this.zones.isEmpty()) {
            throw new AppException(new ErrorResponse("zones is required"));
        }

        if (StringUtils.isBlank(this.password)) {
            throw new AppException(new ErrorResponse("password is required"));
        }

        if (this.numOfSlaves < 2 || this.numOfSlaves > 5) {
            throw new AppException(new ErrorResponse("numOfSlaves must be between 2 and 5"));
        }

        CommonUtils.validatePwd(this.password);
    }

}
