package com.ctel.dbaas.datastore.mongodb.model;

import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.utils.CommonUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserMongo {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateModel {

        private String username;

        private String password;

        private Map<String, String> customData = new HashMap<>();

        private List<DbAccess> databaseAccess;

        public void validate() {
            if (StringUtils.isBlank(this.username)) {
                throw new AppException(new ErrorResponse("username invalid"));
            }

            if (StringUtils.isBlank(this.password)) {
                throw new AppException(new ErrorResponse("password invalid"));
            }
            CommonUtils.validatePwd(this.password);

            if (databaseAccess == null || databaseAccess.isEmpty()) {
                throw new AppException(new ErrorResponse("databaseAccess not exist"));
            }

            for (DbAccess dbAccess : databaseAccess) {
                dbAccess.validate();
                if (StringUtils.equals(dbAccess.getAccessType(), "any_db")) {
                    dbAccess.setDatabaseName("admin");
                }
            }
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdatePermission {

        private String username;

        private String password;

        private Map<String, String> customData = new HashMap<>();

        private List<DbAccess> databaseAccess;

        public void validate() {
            if (StringUtils.isBlank(this.username)) {
                throw new AppException(new ErrorResponse("username invalid"));
            }

            if (StringUtils.isNotBlank(this.password)) {
                CommonUtils.validatePwd(this.password);
            }

            if (databaseAccess == null || databaseAccess.isEmpty()) {
                throw new AppException(new ErrorResponse("databaseAccess not exist"));
            }

            for (DbAccess dbAccess : databaseAccess) {
                dbAccess.validate();
            }
        }
    }

}
