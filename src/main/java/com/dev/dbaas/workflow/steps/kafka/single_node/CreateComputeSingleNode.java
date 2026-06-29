package com.dev.dbaas.workflow.steps.kafka.single_node;

import com.dev.dbaas.common.RegionResource;
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

public class CreateComputeSingleNode implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateComputeSingleNode.class);

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(CreateComputeSingleNode.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        JSONObject checkPrerequisitesSingleNode = origin.optJSONObject(CheckPrerequisitesSingleNode.class.getSimpleName());
        String orgId = checkPrerequisitesSingleNode.optString("orgId");
        String instanceId = checkPrerequisitesSingleNode.optString("instanceId");
        String regionId = checkPrerequisitesSingleNode.optString("regionId");
        String projectId = checkPrerequisitesSingleNode.optString("projectId");

        String glanceImageId = checkPrerequisitesSingleNode.optString("glanceImageId");
        String flavorId = checkPrerequisitesSingleNode.optString("flavorId");
        String datastore = checkPrerequisitesSingleNode.optString("datastore");

        int volumeSize = origin.optJSONObject(CreateVolumeSingleNode.class.getSimpleName()).optInt("volumeSize");

        String keyPair = new RegionResource(regionId, datastore).getKeypair();
        JSONArray volumeCinderIds = new JSONArray();
        JSONArray serverNovaIds = new JSONArray();

        try {
            String serverGroupName = orgId + "_" + datastore + "_" + instanceId;
            String serverGroupId = OSContextManager.getInstance().createServerGroupManual(datastore, regionId, serverGroupName, Constaint.POLICY_SERVER_GROUP);

            TbInstance instance = InstanceManager.findById(instanceId);
            if (instance != null) {
                instance.setServerGroupId(serverGroupId);
                InstanceManager.update(instance);
            }

            TbCompute compute = ComputeManager.findByInstanceId(instanceId).stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("Not found compute with instanceId " + instanceId);
            }

            // get networks
            List<TbNetwork> networks = NetworkManager.findByComputeId(compute.getId());
            List<String> networkIds = new ArrayList<>();
            for (TbNetwork port : networks) {
                networkIds.add(port.getNeutronPortId());
            }

            // get volume
            String cinderVolumeId = null;
            List<TbVolume> volumes = VolumeManager.findByComputeId(compute.getId());
            if (volumes != null && !volumes.isEmpty()) {
                cinderVolumeId = volumes.get(0).getCinderVolumeId();
            }

            // get userdata
            List<TbAgent> agents = AgentManager.findByComputeId(compute.getId());
            if (agents.isEmpty()) {
                throw new Exception("Not found agent with computeId " + compute.getId());
            }
            String agentUserData = agents.get(0).getUserData();

            String userDataEncrypted = Base64.getEncoder().encodeToString(agentUserData.getBytes());
            String computeName = ResourceNameUtil.buildComputeName(orgId, datastore, compute.getRole(), instanceId);
            Server server = OSContextManager.getInstance().createNovaServer(datastore, computeName, regionId, compute.getZoneName(),
                    keyPair, glanceImageId, flavorId, userDataEncrypted, cinderVolumeId, volumeSize, networkIds, serverGroupId);

            // update compute information
            compute.setName(computeName);
            compute.setGlanceImageId(glanceImageId);
            compute.setNovaInstanceId(server.getId());
            compute.setStatus(ComputeStatus.STARTING.getName());
            ComputeManager.update(compute);

            volumeCinderIds.put(cinderVolumeId);
            serverNovaIds.put(server.getId());
            OpenstackResource.sendResourceInstanceToApi(instanceId, getClass().getSimpleName(), volumeCinderIds, serverNovaIds);
        } catch (Exception e) {
            LOGGER.warn("Create compute error : " + e.getMessage());
            TbInstance instance = InstanceManager.findById(instanceId);
            instance.setMessage("create server error");
            instance.setStatus(Constaint.STATUS_ERROR);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
            throw e;
        }

        return ExecutionResult.next();
    }
}
