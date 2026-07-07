package com.ctel.dbaas.service;

import com.ctel.dbaas.common.RequestInfo;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.dto.cmc_cloud.*;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.compute.FlavorInfo;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.utils.CommonUtils;
import com.ctel.dbaas.utils.RestfulUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class CmcCloudService {

    @Autowired
    private RedisTemplate<String, String> redisTokenPortalV2;

    public void validateZone(String token, String regionId, Set<String> zones) {
        Set<String> listZone = this.getZones(token, regionId).stream()
                .filter(i -> !i.isSoldOut()).map(RegionInfo.ZoneInfo::getZoneName).collect(Collectors.toSet());
        for (String zone : zones) {
            if (!listZone.contains(zone)) {
                throw new AppException(new ErrorResponse("zone %s not found or sold out", zone));
            }
        }
    }

    @SneakyThrows
    public void validateNetworkPortalV2(RequestInfo reqCtx, String securityGroupIds) {
        if (StringUtils.isNotBlank(securityGroupIds)) {
            JSONArray jsonArrSgIds = new JSONArray(securityGroupIds);
            for (int i = 0; i < jsonArrSgIds.length(); i++) {
                String sgId = jsonArrSgIds.getString(i);
                SecurityGroupInfo securityGroupInfo = this.getSecurityGroup(sgId, reqCtx);
                if (securityGroupInfo == null) {
                    throw new AppException(new ErrorResponse("Security group[%s] not found", sgId));
                }
            }
        }
    }

    public UserInfo getUserInfo(String token) {
//        String cacheKey = "portalV2-user-" + token;
//        String jsonUserCache = redisTokenPortalV2.opsForValue().get(cacheKey);
//        if (jsonUserCache == null) {
//            log.info("Get user info from CMC Cloud");
//            ResponseEntity<UserInfo> responseEntity = RestfulUtils.get("https://apiv2.cloud.cmctelecom.vn/account/info.json", token, UserInfo.class);
//            if (responseEntity.getStatusCode().is2xxSuccessful()) {
//                UserInfo userInfo = responseEntity.getBody();
//                if (userInfo != null) {
//                    redisTokenPortalV2.opsForValue().set(cacheKey, userInfo.toJson(), 3, TimeUnit.MINUTES);
//                    return userInfo;
//                }
//            }
//        } else {
//            log.info("Get user info from cache redis");
//            return CommonUtils.toObject(jsonUserCache, UserInfo.class);
//        }

        ResponseEntity<UserInfo> responseEntity = RestfulUtils.get("https://apiv2.cloud.cmctelecom.vn/account/info.json", token, UserInfo.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        }

        return null;
    }

    public ProjectInfo[] getProjects(String token, String regionId) {
//        String cacheKey = "portalV2-project-" + token;
//        String jsonProjectCache = redisTokenPortalV2.opsForValue().get(cacheKey);
//        if (jsonProjectCache == null) {
//            log.info("Get project info from CMC Cloud");
//            String url = "https://apiv2.cloud.cmctelecom.vn/account/projects.json" + "?region=" + regionId;
//            ResponseEntity<ProjectInfo[]> responseEntity = RestfulUtils.get(url, token, ProjectInfo[].class);
//            if (responseEntity.getStatusCode().is2xxSuccessful()) {
//                ProjectInfo[] body = responseEntity.getBody();
//                if (body != null && body.length > 0) {
//                    redisTokenPortalV2.opsForValue().set(cacheKey, CommonUtils.toJson(body), 3, TimeUnit.MINUTES);
//                    return body;
//                }
//            }
//        } else {
//            log.info("Get project info from cache redis");
//            return CommonUtils.toObject(jsonProjectCache, ProjectInfo[].class);
//        }
        String url = "https://apiv2.cloud.cmctelecom.vn/account/projects.json" + "?region=" + regionId;
        ResponseEntity<ProjectInfo[]> responseEntity = RestfulUtils.get(url, token, ProjectInfo[].class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            ProjectInfo[] body = responseEntity.getBody();
            if (body != null && body.length > 0) {
                return body;
            }
        }

        return null;
    }

    public SubnetInfo getSubnet(String subnetId, RequestInfo reqCtx) {
        try {
            String urlPortalV2 = "https://apiv2.cloud.cmctelecom.vn/network/subnet/" + subnetId;
            Map<String, String> headers = new HashMap<>();
            headers.put("Region-Id", reqCtx.getRegionId());
            headers.put("Project-Id", reqCtx.getProjectId());

            ResponseEntity<SubnetInfo> res = RestfulUtils.get(urlPortalV2, reqCtx.getToken(), headers, SubnetInfo.class);
            if (!res.getStatusCode().is2xxSuccessful()) {
                return null;
            }
            return res.getBody();
        } catch (HttpClientErrorException e) {
            log.error("getSubnet error: {}", e.getResponseBodyAsString());
        }

        return null;
    }

    public VpcInfo getVpc(String vpcId, RequestInfo reqCtx) {
        try {
            String urlPortalV2 = "https://apiv2.cloud.cmctelecom.vn/network/vpc/" + vpcId;
            Map<String, String> headers = new HashMap<>();
            headers.put("Region-Id", reqCtx.getRegionId());
            headers.put("Project-Id", reqCtx.getProjectId());

            ResponseEntity<VpcInfo> res = RestfulUtils.get(urlPortalV2, reqCtx.getToken(), headers, VpcInfo.class);
            if (!res.getStatusCode().is2xxSuccessful()) {
                return null;
            }
            return res.getBody();
        } catch (HttpClientErrorException e) {
            log.error("getSubnet error: {}", e.getResponseBodyAsString());
        }

        return null;
    }

    public SecurityGroupInfo getSecurityGroup(String securityGroupId, RequestInfo reqCtx) {
        try {
            String urlPortalV2 = "https://apiv2.cloud.cmctelecom.vn/network/securitygroup/" + securityGroupId;
            Map<String, String> headers = new HashMap<>();
            headers.put("Region-Id", reqCtx.getRegionId());
            headers.put("Project-Id", reqCtx.getProjectId());

            ResponseEntity<SecurityGroupInfo> res = RestfulUtils.get(urlPortalV2, reqCtx.getToken(), headers, SecurityGroupInfo.class);
            if (!res.getStatusCode().is2xxSuccessful()) {
                return null;
            }
            return res.getBody();
        } catch (HttpClientErrorException e) {
            log.error("getSecurityGroup error: {}", e.getResponseBodyAsString());
        }

        return null;
    }

    @SneakyThrows
    public List<RegionInfo.ZoneInfo> getZones(String token, String regionId) {
        String urlPortalV2 = "https://apiv2.cloud.cmctelecom.vn/account/zones?region=" + regionId;
        Map<String, String> headers = new HashMap<>();
        headers.put("Region-Id", regionId);

        ResponseEntity<Object> res = RestfulUtils.get(urlPortalV2, token, headers, Object.class);
        List<RegionInfo.ZoneInfo> zoneInfos = new ArrayList<>();
        JSONArray dataResponse = new JSONArray(CommonUtils.toJson(res.getBody()));
        for (int i = 0; i < dataResponse.length(); i++) {
            JSONObject data = dataResponse.getJSONObject(i);
            RegionInfo.ZoneInfo zone = CommonUtils.toObject(data.toString(), RegionInfo.ZoneInfo.class);
            if (zone != null) {
                zoneInfos.add(zone);
            }
        }
        return zoneInfos;
    }

    public FlavorInfo getFlavor(String flavorId, String token, String regionId) {
        try {
            String urlPortalV2 = "https://apiv2.cloud.cmctelecom.vn/server/flavor/" + flavorId + "?region=" + regionId;
            Map<String, String> headers = new HashMap<>();
            headers.put("Region-Id", regionId);
            ResponseEntity<FlavorInfo> res = RestfulUtils.get(urlPortalV2, token, headers, FlavorInfo.class);
            if (!res.getStatusCode().is2xxSuccessful()) {
                return new FlavorInfo();
            }

            return res.getBody();
        } catch (Exception e) {
            return new FlavorInfo();
        }
    }

    public QuotaInfo getQuotas(String orgId, String regionId, String type, String projectId) {
        try {
            String urlPortalV2 = "https://apiv2.cloud.cmctelecom.vn/cntt/account/custom_quotas?username=" + orgId +
                    "&region=" + regionId + "&type=" + type + "&projectId=" + projectId + "&api_key=" + EnvConfig.CNTT_API_KEY;
            ResponseEntity<QuotaInfo> res = RestfulUtils.get(urlPortalV2, new HashMap<>(), QuotaInfo.class);
            if (res.getStatusCode().is2xxSuccessful()) {
                return res.getBody();
            }
        } catch (Exception e) {
            log.error("getQuotas error: {}", e.getMessage());
        }

        return null;
    }

    public TokenDecode getTokenOpenstack(RequestInfo reqCtx) {
        try {
            String urlPortalV2 = "https://apiv2.cloud.cmctelecom.vn/cntt/account/decode_token.json?" +
                    "webtoken={webtoken}&region_id={region_id}&project_id={project_id}&api_key={api_key}"
                            .replace("{webtoken}", reqCtx.getToken())
                            .replace("{region_id}", reqCtx.getRegionId())
                            .replace("{project_id}", reqCtx.getProjectId())
                            .replace("{api_key}", EnvConfig.CNTT_API_KEY);

            ResponseEntity<TokenDecode> res = RestfulUtils.get(urlPortalV2, new HashMap<>(), TokenDecode.class);
            if (res.getStatusCode().is2xxSuccessful()) {
                return res.getBody();
            }
        } catch (Exception e) {
            log.error("getTokenOpenstack error: {}", e.getMessage());
        }

        return null;
    }

    public LoadBalancerInfo getLoadBalancer(String loadBalancerId, RequestInfo reqCtx) {
        try {
            String urlPortalV2 = "https://apiv2.cloud.cmctelecom.vn/lbaas/" + loadBalancerId;
            Map<String, String> mapHeaders = new HashMap<>();
            mapHeaders.put("Region-Id", reqCtx.getRegionId());
            mapHeaders.put("Project-Id", reqCtx.getProjectId());

            ResponseEntity<LoadBalancerInfo> res = RestfulUtils.get(urlPortalV2, reqCtx.getToken(), mapHeaders, LoadBalancerInfo.class);
            if (res.getStatusCode().is2xxSuccessful()) {
                return res.getBody();
            }
        } catch (Exception e) {
            log.error("getLoadBalancer error: {} - loadBalancerId[{}] - requestInfo[{}]", e.getMessage(), loadBalancerId, reqCtx);
        }

        return null;
    }

    public boolean sendLogResource(String instanceId, LogResourceReq logResource) {
        try {
            String urlCloudopsCore = "https://devops.cloud.cmctelecom.vn/cloudops-core/api/v1/resource-logs";
            Map<String, String> mapHeaders = new HashMap<>();
            mapHeaders.put("r-x-api-key", EnvConfig.LOG_BILLING_API_KEY);
            logResource.getMetaData().put("instanceId", instanceId);

            ResponseEntity<Object> res = RestfulUtils.post(urlCloudopsCore, mapHeaders, CommonUtils.toJson(logResource), Object.class);
            if (res.getStatusCode().is2xxSuccessful()) {
                return true;
            }
            System.out.println(res);
        } catch (Exception e) {
            log.error("sendLogResource error: {} - LogResourceReq[{}]", e.getMessage(), logResource);
        }

        return false;
    }

}
