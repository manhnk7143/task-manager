package com.ctel.dbaas.dto.instance;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.utils.CommonUtils;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import java.lang.reflect.Field;
import java.util.Set;

@Data
public class InstanceCreateReq {

    private String name;

    private Integer volumeSize;

    private String flavorId;

    private String groupConfigurationId;

    private String networkId;

    private String subnetId;

    private String securityGroupIds;

    private Datastore datastore;

    private String requestMetadata;

    private String backupId;

    @SneakyThrows
    public void validate() {
        Class<?> clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("securityGroupIds") || field.getName().equals("backupId")) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(this);
            if (value == null) {
                throw new AppException(new ErrorResponse("%s cannot be null", field.getName()));
            }
        }

        if (this.volumeSize < Constant.MIN_VOLUME_SIZE || this.volumeSize > Constant.MAX_VOLUME_SIZE) {
            throw new AppException(new ErrorResponse("volumeSize must be greater than equal 20 and less than equal 32000"));
        }

        if (!CommonUtils.isValidJson(this.requestMetadata)) {
            throw new AppException(new ErrorResponse("requestMetadata invalid"));
        }

        if (StringUtils.isNotEmpty(this.securityGroupIds)) {
            this.securityGroupIds = securityGroupIds.trim();
            String[] arrSgIds = this.securityGroupIds.split(",");
            JSONArray sgIds = new JSONArray();
            for (String sgId : arrSgIds) {
                if (!CommonUtils.isValidUUID(sgId)) {
                    throw new AppException(new ErrorResponse("securityGroupId invalid - %s", sgId));
                }
                sgIds.put(sgId);
            }

            if (sgIds.length() > 10) {
                throw new AppException(new ErrorResponse("The number of security group cannot exceed 10"));
            } else if (sgIds.length() <= 0) {
                this.securityGroupIds = null;
            } else {
                this.securityGroupIds = sgIds.toString();
            }
        } else {
            this.securityGroupIds = null;
        }

        if (StringUtils.isNotBlank(this.backupId) && !CommonUtils.isValidUUID(this.backupId)) {
            throw new AppException(new ErrorResponse("backup invalid - %s", this.backupId));
        }

        this.datastore.validate();
    }

    @Data
    public static class Datastore {
        private String datastoreName;
        private String datastoreCode;
        private String datastoreVersionId;
        private String datastoreModeId;

        public void validate() {
            if (StringUtils.isBlank(this.datastoreName)) {
                throw new AppException(new ErrorResponse("datastoreId cannot be blank"));
            }

            if (StringUtils.isBlank(this.datastoreVersionId)) {
                throw new AppException(new ErrorResponse("datastoreVersionId cannot be blank"));
            }

            if (StringUtils.isBlank(this.datastoreModeId)) {
                throw new AppException(new ErrorResponse("datastoreModeId cannot be blank"));
            }
        }
    }

    private DtValid dtValid = new DtValid();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DtValid {
        private String datastoreId;
        private String datastoreName;
        private String datastoreCode;
        private String datastoreVersionId;
        private String datastoreVersion;
        private String datastoreModeId;
        private String datastoreMode;
        private String groupConfigurationId;
        private String instanceName;
        private String regionId;
//        private Set<String> zones;

        private String instanceId;
        private String vpcId;
        private String networkId;
        private String subnetId;
        private String securityGroupIds;
        private String glanceImageTag;
        private String flavorId;
        private Integer volumeSize;
        private String requestMetadata;
        private String backupId;
        private String backupMode;
    }

}

