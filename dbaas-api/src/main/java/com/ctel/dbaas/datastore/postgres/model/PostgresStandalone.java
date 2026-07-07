package com.ctel.dbaas.datastore.postgres.model;

import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.utils.CommonUtils;
import lombok.Data;

@Data
public class PostgresStandalone {

    private String adminUser;

    private String adminPassword;

    private String allowedHost;

    public void validate() {
        if (adminUser == null || adminPassword == null) {
            throw new AppException(new ErrorResponse("adminUser and adminPassword are required"));
        }
        CommonUtils.validatePwd(adminPassword);
    }

}
