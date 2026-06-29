package com.dev.dbaas.worker.processor.control;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.config.APIServiceType;
import com.dev.dbaas.config.Config;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.database.enties.TbVolume;
import com.dev.dbaas.manager.*;
import com.dev.dbaas.manager.*;
import com.dev.dbaas.worker.job.ControlJob;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

public class DeleteInstanceProcessor implements ProcessorBase<ControlJob> {

    private static final Logger LOGGER = Logger.getLogger(DeleteInstanceProcessor.class);

    private static final int SERVER_INTERNAL_ERROR = -100;
    private static final int SUCCESSFUL = 1;
    private static final int REQUIRE_NAMESPACE = -5;
    private static final int REQUIRE_STATUS = -6;
    private static final int REQUIRE_BACKUP_ID = -7;
    private static final int REQUIRE_ACCOUNT_ID = -8;
    private static final int NOT_FOUND_ACCOUNT_MEMBER = -9;
    private static final int PERMISSION_DENIED = -99;

    @Override
    public boolean process(ControlJob job) throws Exception {

        LOGGER.info("[4CMS] DeleteInstanceProcessor" + job.getServiceId() + " - " + job.getData());
        job.decodePacket();

        JSONObject input = job.getJsonData();
        LOGGER.info("Input: " + input.toString());

        String instanceId = input.optString("instanceId");
        String datastore = input.optString("datastore");
        TbInstance instance = InstanceManager.findById(instanceId);

        JSONArray servers = new JSONArray();
        JSONArray volumes = new JSONArray();
        JSONArray networks = new JSONArray();

        if (instance != null) {
            instance.setStatus(Constaint.DELETING);
            InstanceManager.update(instance);

            List<TbCompute> listCompute = ComputeManager.findByInstanceId(instanceId);
            for (TbCompute compute : listCompute) {

                // delete volume openstack
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

                // delete server openstack
                if (compute.getNovaInstanceId() != null) {
                    boolean serverDeleted =  OSContextManager.getInstance().deleteNovaServer(datastore, compute.getRegionId(), compute.getNovaInstanceId());
                    if (serverDeleted) {
                        JSONObject jsonServer = new JSONObject();
                        jsonServer.put("novaServerId", compute.getNovaInstanceId());
                        jsonServer.put("itemServerId", compute.getId());
                        servers.put(jsonServer);

                        compute.setDeletedAt(LocalDateTime.now());
                        compute.setStatus(Constaint.DELETED);
                        ComputeManager.update(compute);
                    }
                }
            }

            // delete server group
            if (instance.getServerGroupId() != null) {
                boolean serverGroupDeleted = OSContextManager.getInstance().deleteServerGroupsManual(datastore, instance.getRegionId(), instance.getServerGroupId());
                if (!serverGroupDeleted) {
                    LOGGER.warn("Server group " + instance.getServerGroupId() + " not deleted");
                }
            }

            instance.setDeletedAt(LocalDateTime.now());
            instance.setStatus(Constaint.DELETED);
            InstanceManager.update(instance);

            // send event to API
            JSONObject data = new JSONObject();
            data.put("serviceId", APIServiceType.INSTANCE_DELETED);

            JSONObject bodyData = new JSONObject();
            bodyData.put("instanceId", instanceId);
            bodyData.put("servers", servers);
            bodyData.put("volumes", volumes);
            bodyData.put("networks", networks);

            data.put("data", bodyData.toString());
            data.put("messageId", "");
            data.put("time", System.currentTimeMillis());
            ClusterManager.sendTopic(Config.rabbit_connection, Config.rabbit_port, Config.rabbit_username,
                    Config.rabbit_password, Config.rabbit_resource_instance_exchange, Config.rabbit_resource_instance_queue, data);

        }

//        String workFlowId = WorkFlowManager.getInstance().deleteInstance(input);
//        LOGGER.info("Delete WorkFlowId = " + workFlowId);
        return true;
    }
}
