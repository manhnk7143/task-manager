package com.ctel.dbaas.datastore.mongodb;

import com.ctel.dbaas.datastore.mongodb.model.DbAccess;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.datastore.mongodb.model.UserMongo;
import com.ctel.dbaas.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class MongoDBAction {

    private final Map<String, Object> data;

    public MongoDBAction(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getListDatabase() {
        return new HashMap<>();
    }

    public Map<String, Object> getListCollection() {
        Map<String, Object> res = new HashMap<>();
        List<String> lstDbName = (List<String>) this.data.get("listDatabase");
        if (lstDbName == null || lstDbName.isEmpty()) {
            throw new AppException(new ErrorResponse("no database selected"));
        }

        for (String dbName : lstDbName) {
            if (StringUtils.isBlank(dbName)) {
                throw new AppException(new ErrorResponse("database name should be cannot empty"));
            }
        }
        res.put("listDatabase", lstDbName);

        return res;
    }

    public Map<String, Object> getListUser() {
        return new HashMap<>();
    }

    public Map<String, Object> createUser() {
        UserMongo.CreateModel userMongo = CommonUtils.toObject(this.data, UserMongo.CreateModel.class);
        if (userMongo == null) {
            throw new AppException(new ErrorResponse("data invalid"));
        }
        userMongo.validate();

        Map<String, Object> res = new HashMap<>();
        res.put("username", userMongo.getUsername());
        res.put("password", userMongo.getPassword());
        res.put("customData", new Object());

        List<Map<String, Object>> mapDbAccess = new ArrayList<>();
        for (DbAccess dbAccess : userMongo.getDatabaseAccess()) {
            Map<String, Object> mapRole = new HashMap<>();
            mapRole.put("db", dbAccess.getDatabaseName());
            mapRole.put("role", dbAccess.getRoles());
            mapDbAccess.add(mapRole);
        }
        res.put("roles", mapDbAccess);

        return res;
    }

    public Map<String, Object> dropUser() {
        String username = (String) this.data.get("username");
        if (StringUtils.isBlank(username)) {
            throw new AppException(new ErrorResponse("username should cannot be blank"));
        }
        return Collections.singletonMap("username", username);
    }

    public Map<String, Object> updatePermission() {
        UserMongo.UpdatePermission permissionInfo = CommonUtils.toObject(this.data, UserMongo.UpdatePermission.class);
        if (permissionInfo == null) {
            throw new AppException(new ErrorResponse("data invalid"));
        }
        permissionInfo.validate();

        Map<String, Object> res = new HashMap<>();
        res.put("username", permissionInfo.getUsername());

        List<Map<String, Object>> mapDbAccess = new ArrayList<>();
        for (DbAccess dbAccess : permissionInfo.getDatabaseAccess()) {
            Map<String, Object> mapRole = new HashMap<>();
            mapRole.put("db", dbAccess.getDatabaseName());
            mapRole.put("role", dbAccess.getRoles());
            mapDbAccess.add(mapRole);
        }
        res.put("roles", mapDbAccess);

        return res;
    }

    public Map<String, Object> createDatabase() {
        String databaseName = (String) this.data.get("databaseName");
        List<String> collections = (List<String>) this.data.get("collections");
        if (StringUtils.isBlank(databaseName) || StringUtils.contains(databaseName, StringUtils.SPACE)) {
            throw new AppException(new ErrorResponse("databaseName invalid"));
        }
        Map<String, Object> mapRequest = new HashMap<>();
        mapRequest.put("database", databaseName);
        mapRequest.put("collections", collections);

        return mapRequest;
    }

    public Map<String, Object> dropDatabase() {
        String databaseName = (String) this.data.get("databaseName");
        if (StringUtils.isBlank(databaseName)) {
            throw new AppException(new ErrorResponse("databaseName should cannot be blank"));
        }
        return Collections.singletonMap("database", databaseName);
    }

    public Map<String, Object> dropCollection() {
        String database = (String) this.data.get("database");
        List<String> collections = (List<String>) this.data.get("collections");
        if (StringUtils.isBlank(database)) {
            throw new AppException(new ErrorResponse("database should cannot be blank"));
        }

        if (collections == null || collections.isEmpty()) {
            throw new AppException(new ErrorResponse("collections invalid"));
        }

        Map<String, Object> mapRequest = new HashMap<>();
        mapRequest.put("database", database);
        mapRequest.put("collections", collections);

        return mapRequest;
    }

}
