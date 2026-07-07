package com.ctel.dbaas.datastore.api_gateway;

import com.ctel.dbaas.common.context.GrpcCtx;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.datastore.BuildReqTaskMng;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class ApiGatewayBuildRequest {

    @SneakyThrows
    public static JSONObject checkPrerequisites(String instanceId, String computeId, String flavorId, String imageTag,
                                                String vpcId, String subnetId) {
        JSONObject object = new JSONObject();
        object.put("computeId", computeId);
        object.put("flavorId", flavorId);
        object.put("tag", imageTag);
        object.put("vpcId", vpcId);
        object.put("subnetId", subnetId);

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
    public static JSONObject createNetwork(String computeId, String vpcId, String subnetId,
                                           String datastoreName) {
        JSONObject object = new JSONObject();
        if (computeId != null) {
            object.put("computeId", computeId);
        }
        object.put("vpcId", vpcId);
        object.put("subnetId", subnetId);

        // fake data openstack dev
        String regionId = EnvConfig.TEST_REGION_DEV;
        if (!"dev".equals(regionId)) {
            regionId = GrpcCtx.getReqCtx().getRegionId();
        }
        // end fake

        BuildReqTaskMng.RegionResource resource = new BuildReqTaskMng.RegionResource(regionId, datastoreName);
        object.put("securityGroupIdManager", resource.getSecurityGroupMng());
        object.put("networkIdManager", resource.getNetworkMng());
        object.put("subnetIdManager", resource.getSubnetMng());

        return object;
    }

    @SneakyThrows
    public static JSONObject createVolume(String computeId, Integer volumeSize, String zoneName) {
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

        return object;
    }

    @SneakyThrows
    public static JSONObject generateCloudInit(String datastoreName, String datastoreVersion, String datastoreMode,
                                               String role, String agentId, String encryptedKey,
                                               String computeId, String curlAgentMonitor) {
        JSONObject object = new JSONObject();
        object.put("datastoreName", datastoreName);
        object.put("datastoreVersion", datastoreVersion);
        object.put("datastoreMode", datastoreMode);
        object.put("role", role);
        object.put("agentId", agentId);
        object.put("encryptedKey", encryptedKey);
        object.put("computeId", computeId);
        object.put("curlAgentMonitor", curlAgentMonitor);

        return object;
    }

    @SneakyThrows
    public static JSONObject createCompute(String computeId, String zoneName, String datastoreName) {
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

        object.put("keyPair", new BuildReqTaskMng.RegionResource(regionId, datastoreName).getKeypair());
        object.put("zoneName", zone);

        return object;
    }

}
