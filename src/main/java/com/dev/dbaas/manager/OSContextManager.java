package com.dev.dbaas.manager;

import com.dev.dbaas.entity.NeutronPort;
import com.dev.dbaas.entity.ZoneInfo;
import com.dev.dbaas.utils.NetworkUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.client.CloudProvider;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.core.transport.ClientConstants;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.BDMDestType;
import org.openstack4j.model.compute.BDMSourceType;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerGroup;
import org.openstack4j.model.compute.builder.ServerCreateBuilder;
import org.openstack4j.model.network.IP;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.network.SecurityGroup;
import org.openstack4j.model.network.builder.PortBuilder;
import org.openstack4j.model.storage.block.Volume;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.openstack.identity.v3.domain.KeystoneToken;
import org.openstack4j.openstack.internal.OSClientSession;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.*;

public class OSContextManager {

    private static final Logger LOGGER = Logger.getLogger(OSContextManager.class);

    private static class OSContextManagerHolder {
        private static final OSContextManager INSTANCE = new OSContextManager();
    }

    private OSContextManager() {
    }

    public static OSContextManager getInstance() {
        return OSContextManagerHolder.INSTANCE;
    }

    public synchronized OSClient.OSClientV3 getClient(String zoneId, String datastoreCode) {
        return refreshToken(zoneId, datastoreCode);
    }

    private synchronized OSClient.OSClientV3 refreshToken(String zoneId, String datastore) {
        ZoneInfo info = ZoneManager.getByZoneIdAndDatastore(zoneId, datastore);
        OSClient.OSClientV3 os = null;
        if (info != null) {
            if (StringUtils.isNoneEmpty(info.getApplicationCredentialId(), info.getApplicationCredentialSecret())) {
                os = refreshTokenV2(zoneId, datastore);
            } else {
                os = OSFactory.builderV3()
                        .endpoint(info.getEndpoint())
                        .credentials(info.getUsername(), info.getPassword(), Identifier.byName(info.getDomain()))
                        .scopeToProject(Identifier.byId(info.getProjectId()), Identifier.byName(info.getDomain()))
                        .authenticate();
            }
        }

        if (os != null) {
            os.useRegion(zoneId);
        }

        return os;
    }

    @SneakyThrows
    private synchronized OSClient.OSClientV3 refreshTokenV2(String zoneId, String datastore) {
        ZoneInfo info = ZoneManager.getByZoneIdAndDatastore(zoneId, datastore);
        if (info != null) {
            String jsonBody = "{\n" +
                    "    \"auth\": {\n" +
                    "        \"identity\": {\n" +
                    "            \"methods\": [\n" +
                    "                \"application_credential\"\n" +
                    "            ],\n" +
                    "            \"application_credential\": {\n" +
                    "                \"id\": \"" + info.getApplicationCredentialId() + "\",\n" +
                    "                \"secret\": \"" + info.getApplicationCredentialSecret() + "\"\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";

            String url = "https://" + zoneId + ".cloud.dev.vn:5000/v3/auth/tokens";
            ResponseEntity<String> response = NetworkUtil.httpPost(url, new HashMap<>(), jsonBody);

            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                KeystoneToken token = mapper.readValue(new JSONObject(Objects.requireNonNull(response.getBody())).optJSONObject("token").toString(), KeystoneToken.class);
                token.setId(response.getHeaders().getFirst(ClientConstants.HEADER_X_SUBJECT_TOKEN));
                OSClientSession.OSClientSessionV3 v3 = OSClientSession.OSClientSessionV3.createSession(token, null, CloudProvider.UNKNOWN, null);
                Field reqIdField = v3.getClass().getDeclaredField("reqId");
                reqIdField.setAccessible(true);
                reqIdField.set(v3, response.getHeaders().getFirst(ClientConstants.X_OPENSTACK_REQUEST_ID));
                return v3;
            }
            LOGGER.error("Error authen with application credential");
        }
        return null;
    }

    public NeutronPort createPort(String datastore, String zoneId, String name, String securityGroupId, String vpcId, String subnetId, String ipAddress) throws Exception {
        OSClient.OSClientV3 os = getClient(zoneId, datastore);
        PortBuilder portBuilder = Builders.port()
                .name(name)
                .networkId(vpcId)
                .securityGroup(securityGroupId);

        if (StringUtils.isNotBlank(subnetId)) {
            portBuilder = portBuilder.fixedIp(StringUtils.isBlank(ipAddress) ? null :
                    Objects.requireNonNull(ipAddress), subnetId);
        }
        Port port = os.networking().port().create(portBuilder.build());
        Set<? extends IP> fixedIps = port.getFixedIps();
        String ipv4Address = null;
        for (IP ip : fixedIps) {
            String ipLan = ip.getIpAddress();
            if (InetAddressUtils.isIPv4Address(ipLan)) {
                ipv4Address = ipLan;
                break;
            }
        }
        if (port.getId() == null || ipv4Address == null) {
            throw new Exception("Create port failed");
        }
        return new NeutronPort(port.getId(), ipv4Address);
    }

    public void updateSecurityGroupManualForPort(String datastore, String regionId, String portId, HashSet<String> securityGroupIds, boolean isAttach) throws Exception {
        OSClient.OSClientV3 os = getClient(regionId, datastore);
        Port port = os.networking().port().get(portId);
        if (port == null) {
            throw new Exception(String.format("port [%s] not found", portId));
        }

        HashSet<String> listSecurityGroupId = new HashSet<>(port.getSecurityGroups());
        if (isAttach) {
            listSecurityGroupId.addAll(securityGroupIds);
        } else {
            listSecurityGroupId.removeAll(securityGroupIds);
        }

        String url = ((OSClientSession.OSClientSessionV3) os).getEndpoint(ServiceType.NETWORK) + "/v2.0/ports/" + portId;
        String token = os.getToken().getId();
        LOGGER.warn("URL call portalV2 : url[" + url + "] - [portId: " + portId + "] - token[" + token + "]");

        JSONObject securityGroups = new JSONObject();
        securityGroups.put("security_groups", new JSONArray(listSecurityGroupId));
        JSONObject portReq = new JSONObject();
        portReq.put("port", securityGroups);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("X-Auth-Token", token);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<?> httpEntity = new HttpEntity<>(portReq.toString(), httpHeaders);

        LOGGER.warn("POST =====> REQUEST[" + url + "] => data[" + httpEntity + "]");
        ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, Object.class);
        LOGGER.warn("POST =====> RESPONSE[" + url + "] => data[" + response + "]");

        os.networking().port().get(portId);
    }

    public String createServerGroupManual(String datastore, String regionId, String name, String policy) throws Exception {
        OSClient.OSClientV3 os = OSContextManager.getInstance().getClient(regionId, datastore);
        boolean isExited = false;
        String serverGroupId = "";
        List<? extends ServerGroup> serverGroups = os.compute().serverGroups().list();
        for (ServerGroup serverGroup : serverGroups) {
            LOGGER.info("ServerGroup: " + serverGroup.getName());
            if (serverGroup.getName().equalsIgnoreCase(name)) {
                serverGroupId = serverGroup.getId();
                isExited = true;
            }
        }
        if (!isExited) {
            LOGGER.info("Create server group " + name + " - " + policy);
            String url = ((OSClientSession.OSClientSessionV3) os).getEndpoint(ServiceType.COMPUTE) + "/os-server-groups";
            String token = os.getToken().getId();
            LOGGER.warn("Url: " + url + " , token: " + token);

            JSONArray policies = new JSONArray();
            policies.put(policy);

            JSONObject serverGroupJson = new JSONObject();
            serverGroupJson.put("name", name);
            serverGroupJson.put("policies", policies);

            JSONObject bodyRequest = new JSONObject();
            bodyRequest.put("server_group", serverGroupJson);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.set("X-Auth-Token", token);
            httpHeaders.set("X-OpenStack-Nova-API-Version", "2.37");

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<?> httpEntity = new HttpEntity<>(bodyRequest.toString(), httpHeaders);

            LOGGER.warn("POST =====> REQUEST[" + url + "] => data[" + httpEntity + "]");
            ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Object.class);
            LOGGER.warn("POST =====> RESPONSE[" + url + "] => data[" + response + "]");

            JSONObject jsonObject = new JSONObject(new ObjectMapper().writeValueAsString(response.getBody())).optJSONObject("server_group");
            serverGroupId = jsonObject.getString("id");
        }
        return serverGroupId;
    }

    public boolean deleteServerGroupsManual(String datastore, String regionId, String serverGroupId) {
        try {
            if (StringUtils.isBlank(serverGroupId)) {
                return true;
            }
            OSClient.OSClientV3 os = OSContextManager.getInstance().getClient(regionId, datastore);
            ServerGroup serverGroup = getServerGroup(datastore, regionId, serverGroupId);
            if (serverGroup == null) {
                return true;
            }

            String token = os.getToken().getId();
            String url = ((OSClientSession.OSClientSessionV3) os).getEndpoint(ServiceType.COMPUTE) + "/os-server-groups/" + serverGroupId;

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.set("X-Auth-Token", token);
            httpHeaders.set("X-OpenStack-Nova-API-Version", "2.37");

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<?> httpEntity = new HttpEntity<>(httpHeaders);

            LOGGER.warn("DELETE =====> REQUEST[" + url + "] => data[" + httpEntity + "]");
            ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, Object.class);
            LOGGER.warn("DELETE =====> RESPONSE[" + url + "] => data[" + response + "]");

            return true;
        } catch (Exception e) {
            LOGGER.error(e, e);
        }

        return false;
    }

    public NeutronPort createPort(String datastore, String zoneId, String name, List<String> securityGroupIds, String vpcId, String subnetId, String ipAddress) throws Exception {

        OSClient.OSClientV3 os = getClient(zoneId, datastore);
        PortBuilder portBuilder = Builders.port()
                .name(name)
                .networkId(vpcId);
        for (String securityGroupId : securityGroupIds) {
            portBuilder.securityGroup(securityGroupId);
        }
        if (StringUtils.isNotBlank(subnetId)) {
            portBuilder = portBuilder.fixedIp(StringUtils.isBlank(ipAddress) ? null :
                    Objects.requireNonNull(ipAddress), subnetId);
        }
        Port port = os.networking().port().create(portBuilder.build());
        Set<? extends IP> fixedIps = port.getFixedIps();
        String ipv4Address = null;
        for (IP ip : fixedIps) {
            String ipLan = ip.getIpAddress();
            if (InetAddressUtils.isIPv4Address(ipLan)) {
                ipv4Address = ipLan;
                break;
            }
        }
        if (port.getId() == null || ipv4Address == null) {
            throw new Exception("Create port failed");
        }
        return new NeutronPort(port.getId(), ipv4Address);
    }

    public boolean deletePort(String datastore, String zoneId, String neutronPortId) {
        boolean result = false;
        try {
            OSClient.OSClientV3 os = getClient(zoneId, datastore);
            Port port = os.networking().port().get(neutronPortId);
            if (port != null) {
                ActionResponse resDelPort = os.networking().port().delete(neutronPortId);
                if (resDelPort.isSuccess()) {
                    result = true;
                }
            } else {
                result = true;
            }
        } catch (Exception e) {
            LOGGER.error(e, e);
        }

        return result;
    }

    public boolean deleteVolume(String datastore, String zoneId, String cinderVolumeId) {

        boolean result = false;
        try {
            OSClient.OSClientV3 os = getClient(zoneId, datastore);
            Volume volume = os.blockStorage().volumes().get(cinderVolumeId);
            if (volume != null) {
                ActionResponse response = os.blockStorage().volumes().delete(cinderVolumeId);
                if (response.isSuccess()) {
                    result = true;
                }
            } else {
                result = true;
            }
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
        return result;
    }

    public Volume createVolume(String datastore, String zoneId, String name, int size, String description, String zoneName) {
        OSClient.OSClientV3 os = getClient(zoneId, datastore);
        return os.blockStorage().volumes()
                .create(Builders.volume()
                        .name(name)
                        .size(size)
                        .zone(zoneName)
                        .description(description)
                        .build()
                );
    }

    public Volume getVolume(String datastore, String zoneId, String volumeId) {
        OSClient.OSClientV3 os = getClient(zoneId, datastore);
        return os.blockStorage().volumes().get(volumeId);
    }

    public ServerGroup getServerGroup(String datastore, String zoneId, String serverGroupId) {
        OSClient.OSClientV3 os = getClient(zoneId, datastore);
        return os.compute().serverGroups().get(serverGroupId);
    }

    public ActionResponse resetStateVolume(String datastore, String zoneId, String volumeId) {
        OSClient.OSClientV3 os = getClient(zoneId, datastore);
        return os.blockStorage().volumes().resetState(volumeId, Volume.Status.AVAILABLE);
    }

    public ActionResponse resizeVolume(String datastore, String regionId, String volumeId, int newSize) {
        OSClient.OSClientV3 os = getClient(regionId, datastore);
        os.blockStorage().volumes().resetState(volumeId, Volume.Status.AVAILABLE);
        return os.blockStorage().volumes().extend(volumeId, newSize);
    }

    public Server createNovaServer(String datastore, String serverName, String zoneId, String zoneName, String keyPair, String glanceImageId,
                                   String flavorId, String userData, String cinderVolumeId, int volumeSize,
                                   List<String> networkIds, String serverGroupId) {
        OSClient.OSClientV3 os = getClient(zoneId, datastore);
        ServerCreateBuilder sc = Builders.server()
                .name(serverName)
                .image(glanceImageId)
                .flavor(flavorId)
                .userData(userData)
                .keypairName(keyPair)
                .availabilityZone(zoneName)
                .blockDevice(Builders.blockDeviceMapping()
                        .uuid(glanceImageId)
                        .sourceType(BDMSourceType.IMAGE)
                        .destinationType(BDMDestType.LOCAL)
                        .bootIndex(0)
                        .deleteOnTermination(true)
                        .build()
                );

        if (cinderVolumeId != null) {
            sc.blockDevice(Builders.blockDeviceMapping()
                    .uuid(cinderVolumeId)
                    .sourceType(BDMSourceType.VOLUME)
                    .destinationType(BDMDestType.VOLUME)
                    .deviceName("vdb")
                    .volumeSize(volumeSize)
                    .deleteOnTermination(true)
                    .build()
            );
        }

        if (StringUtils.isNotBlank(serverGroupId)) {
            sc.addSchedulerHint("group", serverGroupId);
        }

        Set<String> networkIdsSet = new HashSet<>(networkIds);
        for (String networkId : networkIdsSet) {
            sc.addNetworkPort(networkId);
        }
        return os.compute().servers().boot(sc.build());
    }

    public Server createNovaServer(String datastore, String serverName, String zoneId, String zoneName, String keyPair, String glanceImageId,
                                   String flavorId, String userData, String cinderVolumeId, int volumeSize,
                                   List<String> networkIds) {
        return createNovaServer(datastore, serverName, zoneId, zoneName, keyPair, glanceImageId, flavorId, userData,
                cinderVolumeId, volumeSize, networkIds, null);
    }

    public boolean deleteNovaServer(String datastore, String zoneId, String novaInstanceId) {
        boolean result = false;
        try {
            OSClient.OSClientV3 os = getClient(zoneId, datastore);
            Server server = os.compute().servers().get(novaInstanceId);
            if (server != null) {
                ActionResponse response = os.compute().servers().delete(novaInstanceId);
                if (response.isSuccess()) {
                    result = true;
                }
            } else {
                result = true;
            }
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
        return result;
    }

    public String createSecurityGroup(String datastore, String zoneId, String instanceId, int portDefault) {

        OSClient.OSClientV3 os = getClient(zoneId, datastore);
        SecurityGroup sg = os.networking().securitygroup().create(
                Builders.securityGroup()
                        .name("sg_for_" + instanceId)
                        .description("Security group for instance " + instanceId)
                        .build()
        );

        // create rule for security group
        os.networking().securityrule().create(
                Builders.securityGroupRule()
                        .direction("ingress")
                        .ethertype("IPv4")
                        .protocol("tcp")
                        .securityGroupId(sg.getId())
                        .portRangeMin(portDefault)
                        .portRangeMax(portDefault)
                        .remoteIpPrefix("0.0.0.0/0")
                        .build()
        );

        return sg.getId();
    }

    public String createSecurityGroup(String datastore, String zoneId, String instanceId, List<Integer> listPort) {
        OSClient.OSClientV3 os = getClient(zoneId, datastore);
        SecurityGroup sg = os.networking().securitygroup().create(
                Builders.securityGroup()
                        .name("sg_for_" + instanceId)
                        .description("Security group for instance " + instanceId)
                        .build()
        );

        // create rule for security group
        for (Integer port : listPort) {
            os.networking().securityrule().create(
                    Builders.securityGroupRule()
                            .direction("ingress")
                            .ethertype("IPv4")
                            .protocol("tcp")
                            .securityGroupId(sg.getId())
                            .portRangeMin(port)
                            .portRangeMax(port)
                            .remoteIpPrefix("0.0.0.0/0")
                            .build()
            );
        }

        return sg.getId();
    }

    public boolean deleteSecurityGroup(String datastore, String zoneId, String securityGroupId) {
        boolean result = false;
        try {
            OSClient.OSClientV3 os = getClient(zoneId, datastore);
            SecurityGroup securityGroup = os.networking().securitygroup().get(securityGroupId);
            if (securityGroup != null) {
                ActionResponse response = os.networking().securitygroup().delete(securityGroupId);
                if (response.isSuccess()) {
                    result = true;
                }
            } else {
                result = true;
            }
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
        return result;
    }
}
