package com.dev.dbaas.workflow.steps.mysql.replicaset;

import com.dev.dbaas.config.ComputeStatus;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.*;
import com.dev.dbaas.manager.*;
import com.dev.dbaas.database.enties.*;
import com.dev.dbaas.manager.*;
import com.dev.dbaas.utils.ResourceNameUtil;
import com.dev.dbaas.utils.datastore.OpenstackResource;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openstack4j.model.compute.Server;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CreateCompute implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateCompute.class);

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(getClass().getSimpleName());
        LOGGER.info(getClass().getName() + " INPUT : " + input.toString());

        JSONObject checkPrerequisites = origin.optJSONObject(CheckPrerequisites.class.getSimpleName());
        JSONArray masterComputeIds = checkPrerequisites.optJSONArray("masterComputeIds");
        JSONArray slaveComputeIds = checkPrerequisites.optJSONArray("slaveComputeIds");
        String instanceId = checkPrerequisites.optString("instanceId");
        String regionId = checkPrerequisites.optString("regionId");
        String projectId = checkPrerequisites.optString("projectId");
        String userId = checkPrerequisites.optString("userId");
        String orgId = checkPrerequisites.optString("orgId");

        String glanceImageId = checkPrerequisites.optString("glanceImageId");
        String flavorId = checkPrerequisites.optString("flavorId");
        String datastore = checkPrerequisites.optString("datastore");
        int volumeSize = origin.optJSONObject(CreateVolume.class.getSimpleName()).optInt("volumeSize");

        String keyPair = input.optString("keyPair");

        JSONArray volumeCinderIds = new JSONArray();
        JSONArray serverNovaIds = new JSONArray();
        try {
            String serverGroupId = OSContextManager.getInstance().createServerGroupManual(datastore, regionId, instanceId, Constaint.POLICY_SERVER_GROUP);
            TbInstance instance = InstanceManager.findById(instanceId);
            if (instance != null) {
                instance.setServerGroupId(serverGroupId);
                InstanceManager.update(instance);
            }

            int masterNodes = masterComputeIds.length();
            for (int i = 0; i < masterNodes; i++) {
                String computeId = masterComputeIds.getString(i);
                TbCompute compute = ComputeManager.findById(computeId);
                if (compute == null) {
                    throw new Exception("Not found computeId " + computeId);
                }

                // get networks
                List<TbNetwork> networks = NetworkManager.findByComputeId(computeId);
                List<String> networkIds = new ArrayList<>();
                for (TbNetwork port : networks) {
                    networkIds.add(port.getNeutronPortId());
                }

                // get volume
                String cinderVolumeId = null;
                List<TbVolume> volumes = VolumeManager.findByComputeId(computeId);
                if (volumes != null && !volumes.isEmpty()) {
                    cinderVolumeId = volumes.get(0).getCinderVolumeId();
                }

                // get userdata
                String agentUserData = "";
                List<TbAgent> agents = AgentManager.findByComputeId(computeId);
                if (agents != null && !agents.isEmpty()) {
                    agentUserData = agents.get(0).getUserData();
                }

                String userDataEncrypted = Base64.getEncoder().encodeToString(agentUserData.getBytes());
                String computeName = ResourceNameUtil.buildComputeName(orgId, datastore, compute.getRole(), instanceId);
                Server server = OSContextManager.getInstance().createNovaServer(datastore, computeName, regionId, compute.getZoneName(), keyPair,
                        glanceImageId, flavorId, userDataEncrypted, cinderVolumeId, volumeSize, networkIds, serverGroupId);

                // update compute information
                compute.setName(computeName);
                compute.setGlanceImageId(glanceImageId);
                compute.setNovaInstanceId(server.getId());
                compute.setStatus(ComputeStatus.STARTING.getName());
                ComputeManager.update(compute);

                volumeCinderIds.put(cinderVolumeId);
                serverNovaIds.put(server.getId());
            }

            int slaveNodes = slaveComputeIds.length();
            for (int i = 0; i < slaveNodes; i++) {
                String computeId = slaveComputeIds.getString(i);
                TbCompute compute = ComputeManager.findById(computeId);
                if (compute == null) {
                    throw new Exception("Not found computeId " + computeId);
                }

                // get networks
                List<TbNetwork> networks = NetworkManager.findByComputeId(computeId);
                List<String> networkIds = new ArrayList<>();
                for (TbNetwork port : networks) {
                    networkIds.add(port.getNeutronPortId());
                }

                // get volume
                String cinderVolumeId = null;
                List<TbVolume> volumes = VolumeManager.findByComputeId(computeId);
                if (volumes != null && !volumes.isEmpty()) {
                    cinderVolumeId = volumes.get(0).getCinderVolumeId();
                }

                // get userdata
                String agentUserData = "";
                List<TbAgent> agents = AgentManager.findByComputeId(computeId);
                if (agents != null && !agents.isEmpty()) {
                    agentUserData = agents.get(0).getUserData();
                }

                String userDataEncrypted = Base64.getEncoder().encodeToString(agentUserData.getBytes());
                String computeName = ResourceNameUtil.buildComputeName(orgId, datastore, compute.getRole(), instanceId);
                Server server = OSContextManager.getInstance().createNovaServer(datastore, computeName, regionId, compute.getZoneName(),
                        keyPair, glanceImageId, flavorId, userDataEncrypted, cinderVolumeId, volumeSize, networkIds);

                // update compute information
                compute.setName(computeName);
                compute.setGlanceImageId(glanceImageId);
                compute.setNovaInstanceId(server.getId());
                compute.setStatus(ComputeStatus.STARTING.getName());
                ComputeManager.update(compute);

                volumeCinderIds.put(cinderVolumeId);
                serverNovaIds.put(server.getId());
            }
            OpenstackResource.sendResourceInstanceToApi(instanceId, getClass().getSimpleName(), volumeCinderIds, serverNovaIds);
        } catch (Exception e) {
            LOGGER.warn(getClass().getSimpleName() + "-ERROR : " + e.getMessage());
            TbInstance instance = InstanceManager.findById(instanceId);
            instance.setMessage(getClass().getSimpleName() + " error : " + e.getMessage());
            instance.setStatus(Constaint.STATUS_ERROR);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
            throw e;
        }

        return ExecutionResult.next();
    }
}
