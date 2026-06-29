package com.dev.dbaas.worker.processor.control;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.database.enties.TbAgent;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbVolume;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.manager.AgentManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.manager.OSContextManager;
import com.dev.dbaas.manager.VolumeManager;
import com.dev.dbaas.packet.AppServiceType;
import com.dev.dbaas.protocol.ResponsePacket;
import com.dev.dbaas.worker.job.ControlJob;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.storage.block.Volume;

import java.time.LocalDateTime;
import java.util.List;

public class ResizeVolumeProcessor implements ProcessorBase<ControlJob> {

    private static final Logger LOGGER = Logger.getLogger(ResizeVolumeProcessor.class);

    @Override
    public boolean process(ControlJob job) throws Exception {
        LOGGER.warn("[4CMS] ResizeVolumeProcessor " + job.getServiceId() + " - " + job.getData());
        job.decodePacket();
        JSONObject input = job.getJsonData();

        String regionId = input.optString("regionId");
        String instanceId = input.optString("instanceId");
        String datastore = input.optString("datastore");
        int newVolumeSize = Integer.parseInt(input.optString("newVolumeSize"));
        JSONArray computeIds = input.optString("computeIds").isEmpty() ? new JSONArray() : new JSONArray(input.optString("computeIds"));

        TbInstance instance = InstanceManager.findById(instanceId);
        if (instance == null) {
            return false;
        }

        for (int i = 0; i < computeIds.length(); i++) {
            String computeId = computeIds.getString(i);
            List<TbAgent> agents = AgentManager.findByComputeId(computeId);
            if (agents.isEmpty()) {
                LOGGER.warn("Not found agents by computeId = " + computeId);
                return false;
            }
            TbAgent agent = agents.get(0);

            List<TbVolume> volumes = VolumeManager.findByComputeId(computeId);
            for (TbVolume volume : volumes) {
                Account account = AgentManager.getAccount(agent.getId());
                if (account == null) {
                    LOGGER.warn("Not found account by agentId = " + agent.getId());
                    return false;
                }
                Volume volumeOs = OSContextManager.getInstance().getVolume(datastore, regionId, volume.getCinderVolumeId());
                if (volumeOs == null) {
                    LOGGER.warn("Not found volume by cinderVolumeId = " + volume.getCinderVolumeId());
                    return false;
                }

                ActionResponse resetStateRes = OSContextManager.getInstance().resetStateVolume(datastore, regionId, volume.getCinderVolumeId());
                if (!resetStateRes.isSuccess()) {
                    LOGGER.error("Reset state volume not success - cinderVolumeId:" + volume.getCinderVolumeId() + " - cause: " + resetStateRes.getFault());
                    return false;
                }

                ActionResponse resizeResponse = OSContextManager.getInstance().resizeVolume(datastore, regionId, volume.getCinderVolumeId(), newVolumeSize);
                if (!resizeResponse.isSuccess()) {
                    LOGGER.error("Resize volume not success - cinderVolumeId:" + volume.getCinderVolumeId() + " - cause: " + resizeResponse.getFault());
                    return false;
                }

                LOGGER.warn("Send message to " + agent.getId() + ", " + account.getClients().size());
                JSONObject dataJson = new JSONObject();
                dataJson.put("newVolumeSize", newVolumeSize);

                ResponsePacket responsePacket = new ResponsePacket();
                responsePacket.setServiceId(AppServiceType.RESIZE_FILESYSTEM);
                responsePacket.setResult(1);
                responsePacket.setMessage("");
                responsePacket.setMessageId(System.currentTimeMillis() + "");
                responsePacket.setData(dataJson.toString());
                account.receivePacket(responsePacket);

                volume.setSize(newVolumeSize);
                volume.setUpdatedAt(LocalDateTime.now());
                VolumeManager.update(volume);
            }

            instance.setVolumeSize(newVolumeSize);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
        }

        LOGGER.warn("[4CMS] ResizeVolumeProcessor DONE: " + job.getServiceId() + " - " + job.getData());
        return true;
    }
}
