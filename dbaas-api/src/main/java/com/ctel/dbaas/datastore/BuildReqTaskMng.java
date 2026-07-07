package com.ctel.dbaas.datastore;

import com.ctel.dbaas.common.Constant;
import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.common.enums.DatastoreSupport;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.utils.CommonUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class BuildReqTaskMng {

    @SneakyThrows
    public static JSONObject checkPrerequisites(String instanceId, String computeId, String flavorId, String imageTag,
                                                String vpcId, String subnetId, String datastoreCode) {
        JSONObject object = new JSONObject();
        object.put("computeId", computeId);
        object.put("flavorId", flavorId);
        object.put("tag", imageTag);
        object.put("vpcId", vpcId);
        object.put("subnetId", subnetId);
        object.put("datastore", datastoreCode);
        object.put("monitorResourceType", DatastoreSupport.getOrThrow(datastoreCode).getMonitorResourceTypeName());

        // fake data openstack dev
        String regionId = EnvConfig.TEST_REGION_DEV;
        if (!"dev".equals(regionId)) {
            regionId = GrpcCtx.getReqCtx().getRegionId();
        }
        // end fake

        object.put("instanceId", instanceId);
        object.put("orgId", GrpcCtx.getReqCtx().getOrgId());
        object.put("regionId", regionId);
        object.put("projectId", GrpcCtx.getReqCtx().getProjectId());
        object.put("userId", GrpcCtx.getReqCtx().getUserId());

        return object;
    }

    @SneakyThrows
    public static JSONObject createNetwork(String instanceId, String computeId, String vpcId, String subnetId,
                                           String ipAddress, String datastoreCode, String securityGroupIds) {
        JSONObject object = new JSONObject();
        if (computeId != null) {
            object.put("computeId", computeId);
        }
        object.put("vpcId", vpcId);
        object.put("subnetId", subnetId);
        object.put("ipAddress", ipAddress);
        object.put("securityGroupIds", CommonUtils.isValidJson(securityGroupIds) ? new JSONArray(securityGroupIds) : null);

        // fake data openstack dev
        String regionId = EnvConfig.TEST_REGION_DEV;
        if (!"dev".equals(regionId)) {
            regionId = GrpcCtx.getReqCtx().getRegionId();
        }
        // end fake

        RegionResource resource = new RegionResource(regionId, datastoreCode);
        object.put("portDefault", resource.getPortDefault());
        object.put("securityGroupIdManager", resource.getSecurityGroupMng());
        object.put("networkIdManager", resource.getNetworkMng());
        object.put("subnetIdManager", resource.getSubnetMng());
        addDataReqCtx(object, instanceId);

        return object;
    }

    @SneakyThrows
    public static JSONObject createVolume(String instanceId, String computeId, Integer volumeSize, String zoneName) {
        JSONObject object = new JSONObject();
        if (computeId != null) {
            object.put("computeId", computeId);
        }
        object.put("volumeSize", volumeSize);

        // fake data openstack dev
        String zone = EnvConfig.TEST_ZONE_DEV;
        if (!StringUtils.equals("nova", zone)) {
            zone = zoneName;
        }
        // end fake

        object.put("zoneName", zone);
        addDataReqCtx(object, instanceId);

        return object;
    }

    @SneakyThrows
    public static JSONObject generateCloudInit(String datastoreName, String datastoreVersion, String datastoreMode,
                                               String instanceId, String role, String agentId, String encryptedKey,
                                               String password, String computeId, String backupUrl) {
        JSONObject object = new JSONObject();
        if (password != null) {
            object.put("password", password);
        }
        object.put("datastoreName", datastoreName);
        object.put("datastoreVersion", datastoreVersion);
        object.put("datastoreMode", datastoreMode);
        object.put("role", role);
        object.put("agentId", agentId);
        object.put("encryptedKey", encryptedKey);
        object.put("computeId", computeId);
        object.put("dockerRegistry", new RegionResource(GrpcCtx.getReqCtx().getRegionId(), datastoreName).getDockerRegistry());
        if (StringUtils.isNotBlank(backupUrl)) {
            object.put("backupUrl", backupUrl);
        }

        addDataReqCtx(object, instanceId);

        return object;
    }

    @SneakyThrows
    public static JSONObject generateCloudInitMongodbReplicaset(String datastoreName, String datastoreVersion, String datastoreMode,
                                                                String instanceId, String mongoRole, String rootPassword, String backupUrl) {
        JSONObject object = new JSONObject();
        object.put("datastoreName", datastoreName);
        object.put("datastoreVersion", datastoreVersion);
        object.put("datastoreMode", datastoreMode);
        object.put("rootUser", Constant.ROOT_USER_MONGODB);
        object.put("rootPassword", rootPassword);
        object.put("mongoRole", mongoRole);
        object.put("replicasetKey", instanceId.replace("-", ""));
        object.put("replicasetName", Constant.REPLICASET_MONGODB);
        object.put("dockerRegistry", new RegionResource(GrpcCtx.getReqCtx().getRegionId(), datastoreName).getDockerRegistry());
        object.put("backupUrl", StringUtils.isBlank(backupUrl) ? "" : backupUrl);
        addDataReqCtx(object, instanceId);

        return object;
    }

    @SneakyThrows
    public static JSONObject loadConfigGroup(String instanceId, String groupConfigurationId, String computeId) {
        JSONObject object = new JSONObject();
        object.put("groupConfigurationId", groupConfigurationId);
        object.put("computeId", computeId);
        addDataReqCtx(object, instanceId);

        return object;
    }

    @SneakyThrows
    public static JSONObject createCompute(String instanceId, String computeId, String zoneName, String datastoreCode) {
        JSONObject object = new JSONObject();
        if (computeId != null) {
            object.put("computeId", computeId);
        }

        // fake data openstack dev
        String zone = EnvConfig.TEST_ZONE_DEV;
        if (!StringUtils.equals("nova", zone)) {
            zone = zoneName;
        }

        String regionId = EnvConfig.TEST_REGION_DEV;
        if (!"dev".equals(regionId)) {
            regionId = GrpcCtx.getReqCtx().getRegionId();
        }
        // end fake

        object.put("keyPair", new RegionResource(regionId, datastoreCode).getKeypair());
        object.put("zoneName", zone);
        addDataReqCtx(object, instanceId);

        return object;
    }


    @SneakyThrows
    private static void addDataReqCtx(JSONObject obj, String instanceId) {
        if (instanceId != null) {
            obj.put("instanceId", instanceId);
        }

        // fake data openstack dev
        String regionId = EnvConfig.TEST_REGION_DEV;
        if (!"dev".equals(regionId)) {
            regionId = GrpcCtx.getReqCtx().getRegionId();
        }
        // end fake

        obj.put("orgId", GrpcCtx.getReqCtx().getOrgId());
        obj.put("regionId", regionId);
        obj.put("projectId", GrpcCtx.getReqCtx().getProjectId());
        obj.put("userId", GrpcCtx.getReqCtx().getUserId());
    }


    @Getter
    public static class RegionResource {
        private String keypair;
        private String networkMng;
        private String securityGroupMng;
        private String subnetMng;
        private String dockerRegistry;
        private Integer portDefault;

        public RegionResource(String region, String datastoreCode) {
            DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(datastoreCode);
            switch (datastoreSupport) {
                case REDIS -> this.portDefault = 6379;
                case MONGODB -> this.portDefault = 27017;
                case POSTGRESQL -> this.portDefault = 5432;
                case KAFKA -> this.portDefault = 9092;
            }

            if ("hn-1".equals(region)) {
                this.dockerRegistry = EnvConfig.DOCKER_REGISTRY_HN;
                switch (datastoreSupport) {
                    case REDIS -> {
                        this.keypair = EnvConfig.OS_REDIS_KEY_PAIR;
                        this.networkMng = EnvConfig.OS_REDIS_HN1_NETWORK;
                        this.securityGroupMng = EnvConfig.OS_REDIS_HN1_SECURITY_GROUP;
                        this.subnetMng = EnvConfig.OS_REDIS_HN1_SUBNET;
                    }
                    case MONGODB -> {
                        this.keypair = EnvConfig.OS_MONGODB_KEY_PAIR;
                        this.networkMng = EnvConfig.OS_MONGODB_HN1_NETWORK;
                        this.securityGroupMng = EnvConfig.OS_MONGODB_HN1_SECURITY_GROUP;
                        this.subnetMng = EnvConfig.OS_MONGODB_HN1_SUBNET;
                    }
                    case KAFKA -> {
                        this.keypair = EnvConfig.OS_KAFKA_KEY_PAIR;
                        this.networkMng = EnvConfig.OS_KAFKA_HN1_NETWORK;
                        this.securityGroupMng = EnvConfig.OS_KAFKA_HN1_SECURITY_GROUP;
                        this.subnetMng = EnvConfig.OS_KAFKA_HN1_SUBNET;
                    }
                }
            } else if ("hcm-1".equals(region)) {
                this.dockerRegistry = EnvConfig.DOCKER_REGISTRY_HCM;
                switch (datastoreSupport) {
                    case REDIS -> {
                        this.keypair = EnvConfig.OS_REDIS_KEY_PAIR;
                        this.networkMng = EnvConfig.OS_REDIS_HCM1_NETWORK;
                        this.securityGroupMng = EnvConfig.OS_REDIS_HCM1_SECURITY_GROUP;
                        this.subnetMng = EnvConfig.OS_REDIS_HCM1_SUBNET;
                    }
                    case MONGODB -> {
                        this.keypair = EnvConfig.OS_MONGODB_KEY_PAIR;
                        this.networkMng = EnvConfig.OS_MONGODB_HCM1_NETWORK;
                        this.securityGroupMng = EnvConfig.OS_MONGODB_HCM1_SECURITY_GROUP;
                        this.subnetMng = EnvConfig.OS_MONGODB_HCM1_SUBNET;
                    }
                    case KAFKA -> {
                        this.keypair = EnvConfig.OS_KAFKA_KEY_PAIR;
                        this.networkMng = EnvConfig.OS_KAFKA_HCM1_NETWORK;
                        this.securityGroupMng = EnvConfig.OS_KAFKA_HCM1_SECURITY_GROUP;
                        this.subnetMng = EnvConfig.OS_KAFKA_HCM1_SUBNET;
                    }
                }
            } else if ("dev".equals(region)) { // for test devstack
                this.keypair = EnvConfig.TEST_OS_KEY_PAIR;
                this.networkMng = EnvConfig.TEST_OS_NETWORK;
                this.securityGroupMng = EnvConfig.TEST_OS_SECURITY_GROUP;
                this.subnetMng = EnvConfig.TEST_OS_SUBNET;
            }
        }
    }

}
