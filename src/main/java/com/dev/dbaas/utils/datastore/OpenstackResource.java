package com.dev.dbaas.utils.datastore;

import com.dev.dbaas.config.APIServiceType;
import com.dev.dbaas.config.Config;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.database.enties.TbVolume;
import com.dev.dbaas.manager.*;
import com.dev.dbaas.manager.*;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

public class OpenstackResource {

    private static final Logger LOGGER = Logger.getLogger(OpenstackResource.class);

    public static void deleteVolumeAndNotify(String datastore, String instanceId) {
        try {
            JSONArray volumes = new JSONArray();
            List<TbCompute> computes = ComputeManager.findByInstanceId(instanceId);
            for (TbCompute compute : computes) {
                List<TbVolume> listVolume = VolumeManager.findByComputeId(compute.getId());
                for (TbVolume volume : listVolume) {
                    if (volume.getCinderVolumeId() != null) {
                        boolean volumeDeleted = OSContextManager.getInstance().deleteVolume(datastore, compute.getRegionId(), volume.getCinderVolumeId());
                        if (volumeDeleted) {
                            JSONObject jsonVolume = new JSONObject();
                            jsonVolume.put("cinderVolumeId", volume.getCinderVolumeId());
                            jsonVolume.put("itemVolumeId", volume.getId());
                            jsonVolume.put("name", volume.getName());
                            volumes.put(jsonVolume);

                            volume.setDeletedAt(LocalDateTime.now());
                            volume.setStatus(Constaint.DELETED);
                            VolumeManager.update(volume);
                        }
                    }
                }
            }

            // send event to API
            JSONObject data = new JSONObject();
            data.put("serviceId", APIServiceType.VOLUMES_DELETED);
            JSONObject bodyData = new JSONObject();
            bodyData.put("instanceId", instanceId);
            bodyData.put("volumes", volumes);

            data.put("data", bodyData.toString());
            data.put("messageId", "");
            data.put("time", System.currentTimeMillis());
            ClusterManager.sendTopic(Config.rabbit_connection, Config.rabbit_port, Config.rabbit_username, Config.rabbit_password, Config.rabbit_api_exchange, Config.rabbit_api_queue, data);

        } catch (Exception e) {
            LOGGER.error(e, e);
        }
    }

    public static void deleteServerAndNotify(String datastore, String instanceId) {
        try {
            List<TbCompute> listCompute = ComputeManager.findByInstanceId(instanceId);
            JSONArray servers = new JSONArray();
            for (TbCompute compute : listCompute) {
                if (compute.getNovaInstanceId() != null) {
                    boolean serverDeleted = OSContextManager.getInstance().deleteNovaServer(datastore, compute.getRegionId(), compute.getNovaInstanceId());
                    if (serverDeleted) {
                        JSONObject jsonServer = new JSONObject();
                        jsonServer.put("novaServerId", compute.getNovaInstanceId());
                        jsonServer.put("itemServerId", compute.getId());
                        jsonServer.put("serverName", compute.getName());
                        servers.put(jsonServer);

                        compute.setDeletedAt(LocalDateTime.now());
                        compute.setStatus(Constaint.DELETED);
                        ComputeManager.update(compute);
                    }
                }
                compute.setDeletedAt(LocalDateTime.now());
                ComputeManager.update(compute);
            }

            // send event to API
            JSONObject data = new JSONObject();
            data.put("serviceId", APIServiceType.SERVERS_DELETED);
            JSONObject bodyData = new JSONObject();
            bodyData.put("instanceId", instanceId);
            bodyData.put("servers", servers);

            data.put("data", bodyData.toString());
            data.put("messageId", "");
            data.put("time", System.currentTimeMillis());
            ClusterManager.sendTopic(Config.rabbit_connection, Config.rabbit_port, Config.rabbit_username, Config.rabbit_password, Config.rabbit_api_exchange, Config.rabbit_api_queue, data);

        } catch (Exception e) {
            LOGGER.error(e, e);
        }
    }

    public static void deleteNetworkAndNotify(String datastore, String instanceId) {
        try {
            List<TbCompute> computes = ComputeManager.findByInstanceId(instanceId);
            JSONArray networks = new JSONArray();
            for (TbCompute compute : computes) {
                // delete port openstack
                List<TbNetwork> listNetwork = NetworkManager.findByComputeId(compute.getId());
                HashSet<String> securityGroupIds = new HashSet<>();
                for (TbNetwork network : listNetwork) {
                    if (network.getNeutronPortId() != null) {
                        boolean portDeleted = OSContextManager.getInstance().deletePort(datastore, compute.getRegionId(), network.getNeutronPortId());
                        if (portDeleted) {
                            JSONObject jsonNetwork = new JSONObject();
                            jsonNetwork.put("neutronPortId", network.getNeutronPortId());
                            networks.put(jsonNetwork);

                            network.setDeletedAt(LocalDateTime.now());
                            network.setStatus(Constaint.DELETED);
                            NetworkManager.update(network);
                        }
                    }

                    if (Constaint.MODE_USER.equalsIgnoreCase(network.getMode())) {
                        securityGroupIds.add(network.getNeutronSecurityGroupId());
                    }
                }

                // delete security group openstack
                for (String securityGroupId : securityGroupIds) {
                    boolean securityGroupDeleted = OSContextManager.getInstance().deleteSecurityGroup(datastore, compute.getRegionId(), securityGroupId);
                    if (!securityGroupDeleted) {
                        LOGGER.warn("Security group " + securityGroupId + " not deleted");
                    }
                }
            }

            // send event to API
            JSONObject data = new JSONObject();
            data.put("serviceId", APIServiceType.NETWORKS_DELETED);
            JSONObject bodyData = new JSONObject();
            bodyData.put("instanceId", instanceId);
            bodyData.put("networks", networks);

            data.put("data", bodyData.toString());
            data.put("messageId", "");
            data.put("time", System.currentTimeMillis());
            ClusterManager.sendTopic(Config.rabbit_connection, Config.rabbit_port, Config.rabbit_username, Config.rabbit_password, Config.rabbit_api_exchange, Config.rabbit_api_queue, data);
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
    }

    public static void sendResourceInstanceToApi(String instanceId, String action, JSONArray volumeCinderIds, JSONArray serverNovaIds) {
        JSONObject data = new JSONObject();
        data.put("serviceId", APIServiceType.UPDATE_STATUS_INSTANCE);
        JSONObject bodyData = new JSONObject();
        bodyData.put("action", action);
        bodyData.put("instanceId", instanceId);
        bodyData.put("status", Constaint.DONE);
        bodyData.put("volumeCinderIds", volumeCinderIds);
        bodyData.put("serverNovaIds", serverNovaIds);

        data.put("data", bodyData.toString());
        data.put("messageId", "");
        data.put("time", System.currentTimeMillis());
        ClusterManager.sendTopic(Config.rabbit_connection, Config.rabbit_port, Config.rabbit_username, Config.rabbit_password, Config.rabbit_api_exchange, Config.rabbit_api_queue, data);
    }

}
