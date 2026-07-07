package com.ctel.dbaas.datastore.mongodb.model;

import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Data
public class DbAccess {

    private String databaseName;

    private String accessType;

    private List<String> roles;


    private static final Map<String, List<String>> BUILD_IN_ROLE = new HashMap<>();
    static {
        BUILD_IN_ROLE.put("any_db", List.of("readAnyDatabase", "readWriteAnyDatabase", "userAdminAnyDatabase", "dbAdminAnyDatabase"));
        BUILD_IN_ROLE.put("one_db", List.of("read", "readWrite", "dbAdmin", "userAdmin", "dbOwner"));
    }

    public void validate() {
        List<String> lstRole = BUILD_IN_ROLE.get(this.accessType);
        if (lstRole == null) {
            throw new AppException(new ErrorResponse("accessType invalid"));
        }

        if (!new HashSet<>(lstRole).containsAll(this.roles)) {
            throw new AppException(new ErrorResponse("role invalid"));
        }

        if ("one_db".equals(this.accessType) && StringUtils.isBlank(this.databaseName)) {
            throw new AppException(new ErrorResponse("databaseName should not be blank"));
        }
    }

}
