package com.dev.dbaas.workflow.steps.mongodb.replicaset.secondary;

import com.dev.dbaas.config.ComputeStatus;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.*;
import com.dev.dbaas.manager.*;
import com.dev.dbaas.database.enties.*;
import com.dev.dbaas.manager.*;
import com.dev.dbaas.utils.ResourceNameUtil;
import com.dev.dbaas.utils.datastore.OpenstackResource;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.CheckPrerequisitesReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.primary.CreateComputePrimaryReplicaset;
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

public class CreateComputeSecondaryReplicaset implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateComputeSecondaryReplicaset.class);

    public CreateComputeSecondaryReplicaset() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(CreateComputeSecondaryReplicaset.class.getSimpleName());
        JSONObject checkPrerequisitesReplInput = origin.optJSONObject(CheckPrerequisitesReplicaset.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String regionId = checkPrerequisitesReplInput.optString("regionId");
        JSONArray secondaryComputeIds = checkPrerequisitesReplInput.optJSONArray("secondaryComputeIds");
        String glanceImageId = checkPrerequisitesReplInput.optString("glanceImageId");
        String flavorId = checkPrerequisitesReplInput.optString("flavorId");
        int volumeSize = origin.optJSONObject(CreateVolumeSecondaryReplicaset.class.getSimpleName()).optInt("volumeSize");
        String datastore = checkPrerequisitesReplInput.optString("datastore");
        String orgId = checkPrerequisitesReplInput.optString("orgId");
        String serverGroupId = origin.optJSONObject(CreateComputePrimaryReplicaset.class.getSimpleName()).optString("serverGroupId");

        String instanceId = checkPrerequisitesReplInput.optString("instanceId");
        String keyPair = input.optString("keyPair");

        try {
            JSONArray volumeCinderIds = new JSONArray();
            JSONArray serverNovaIds = new JSONArray();

            int secondaryNodes = secondaryComputeIds.length();
            for (int i = 0; i < secondaryNodes; i++) {
                String computeId = secondaryComputeIds.getString(i);

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
                    volumeCinderIds.put(cinderVolumeId);
                }

                // get userdata
                TbAgent agent = AgentManager.findByComputeId(computeId).stream().findFirst().orElse(null);
                if (agent == null) {
                    throw new Exception("Not found agent with computeId " + computeId);
                }

                String userDataEncrypted = Base64.getEncoder().encodeToString(agent.getUserData().getBytes());
                String computeName = ResourceNameUtil.buildComputeName(orgId, datastore, compute.getRole(), instanceId);
                Server server = OSContextManager.getInstance().createNovaServer(datastore, computeName, regionId, compute.getZoneName(), keyPair,
                        glanceImageId, flavorId, userDataEncrypted, cinderVolumeId, volumeSize, networkIds, serverGroupId);

                // update compute information
                compute.setName(computeName);
                compute.setGlanceImageId(glanceImageId);
                compute.setNovaInstanceId(server.getId());
                compute.setStatus(ComputeStatus.STARTING.getName());
                ComputeManager.update(compute);
                serverNovaIds.put(server.getId());
            }

            OpenstackResource.sendResourceInstanceToApi(instanceId, getClass().getSimpleName(), volumeCinderIds, serverNovaIds);
        } catch (Exception e) {
            LOGGER.warn("Create compute error : " + e.getMessage());
            TbInstance instance = InstanceManager.findById(instanceId);
            instance.setMessage("Create compute error : " + e.getMessage());
            instance.setStatus(Constaint.STATUS_ERROR);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
            throw e;
        }

        return ExecutionResult.next();
    }
}



