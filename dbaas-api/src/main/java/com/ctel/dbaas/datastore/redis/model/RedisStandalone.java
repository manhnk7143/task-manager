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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedisStandalone {

    private String zone;

    private String password;

    public void validate() {
        if (StringUtils.isBlank(this.zone)) {
            throw new AppException(new ErrorResponse("zone is required"));
        }

        if (StringUtils.isBlank(this.password)) {
            throw new AppException(new ErrorResponse("password is required"));
        }
        CommonUtils.validatePwd(this.password);
    }

}
