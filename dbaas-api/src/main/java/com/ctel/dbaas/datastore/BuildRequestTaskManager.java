package com.ctel.dbaas.datastore;

import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.common.enums.DatastoreSupport;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.utils.CommonUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class BuildRequestTaskManager {

    @SneakyThrows
    public static JSONObject checkPrerequisites(String instanceId, String flavorId, String imageTag,
                                                String vpcId, String subnetId, String datastoreCode) {
        JSONObject object = new JSONObject();
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
    public static JSONObject createNetwork(String vpcId, String subnetId,
                                           String datastoreCode, String securityGroupIds) {
        JSONObject object = new JSONObject();
        object.put("vpcId", vpcId);
        object.put("subnetId", subnetId);
        object.put("securityGroupIds", CommonUtils.isValidJson(securityGroupIds) ? new JSONArray(securityGroupIds) : null);

        // fake data openstack dev
        String regionId = EnvConfig.TEST_REGION_DEV;
        if (!"dev".equals(regionId)) {
            regionId = GrpcCtx.getReqCtx().getRegionId();
        }
        // end fake

        BuildReqTaskMng.RegionResource resource = new BuildReqTaskMng.RegionResource(regionId, datastoreCode);
        object.put("portDefault", resource.getPortDefault());
        object.put("securityGroupIdManager", resource.getSecurityGroupMng());
        object.put("networkIdManager", resource.getNetworkMng());
        object.put("subnetIdManager", resource.getSubnetMng());

        return object;
    }

    @SneakyThrows
    public static JSONObject createVolume(Integer volumeSize) {
        JSONObject object = new JSONObject();
        object.put("volumeSize", volumeSize);

        return object;
    }

    @SneakyThrows
    public static JSONObject generateCloudInit(String datastoreCode, String datastoreVersion, String datastoreMode,
                                               String password, String backupUrl) {
        JSONObject object = new JSONObject();
        if (password != null) {
            object.put("password", password);
        }

        object.put("datastore", datastoreCode);
        object.put("datastoreVersion", datastoreVersion);
        object.put("datastoreMode", datastoreMode);
        object.put("dockerRegistry", new RegionResource(GrpcCtx.getReqCtx().getRegionId(), datastoreCode).getDockerRegistry());
        if (StringUtils.isNotBlank(backupUrl)) {
            object.put("backupUrl", backupUrl);
        }

        return object;
    }

    @SneakyThrows
    public static JSONObject loadConfigGroup(String groupConfigurationId) {
        JSONObject object = new JSONObject();
        object.put("groupConfigurationId", groupConfigurationId);

        return object;
    }

    @SneakyThrows
    public static JSONObject createCompute(String datastoreCode) {
        JSONObject object = new JSONObject();
        String regionId = EnvConfig.TEST_REGION_DEV;
        if (!"dev".equals(regionId)) {
            regionId = GrpcCtx.getReqCtx().getRegionId();
        }
        // end fake

        object.put("keyPair", new BuildReqTaskMng.RegionResource(regionId, datastoreCode).getKeypair());

        return object;
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
