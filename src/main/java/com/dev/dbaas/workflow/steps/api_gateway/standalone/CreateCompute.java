package com.dev.dbaas.workflow.steps.api_gateway.standalone;

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
        JSONObject input = origin.optJSONObject(CreateCompute.class.getSimpleName());
        LOGGER.info(getClass().getName() + " INPUT : " + input.toString());

        JSONObject checkPrerequisites = origin.optJSONObject(CheckPrerequisites.class.getSimpleName());
        String orgId = checkPrerequisites.optString("orgId");
        String instanceId = checkPrerequisites.optString("instanceId");
        String regionId = checkPrerequisites.optString("regionId");
        String projectId = checkPrerequisites.optString("projectId");
        String glanceImageId = checkPrerequisites.optString("glanceImageId");
        String flavorId = checkPrerequisites.optString("flavorId");
        String datastore = checkPrerequisites.optString("datastore");

        JSONObject generateCloudInitStandalone = origin.optJSONObject(GenerateCloudInit.class.getSimpleName());
        String datastoreMode = generateCloudInitStandalone.optString("datastoreMode");

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

            TbCompute compute = ComputeManager.findByInstanceId(instanceId).stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("Not found computeId with instanceId" + instanceId);
            }

            // get networks
            List<TbNetwork> networks = NetworkManager.findByComputeId(compute.getId());
            List<String> networkIds = new ArrayList<>();
            for (TbNetwork port : networks) {
                networkIds.add(port.getNeutronPortId());
            }

            // get volume
            TbVolume volume = VolumeManager.findByComputeId(compute.getId()).stream().findFirst().orElse(null);
            if (volume == null || volume.getId() == null) {
                throw new Exception("Not found volume of computeId " + compute.getId());
            }

            // get userdata
            String agentUserData = "";
            List<TbAgent> agents = AgentManager.findByComputeId(compute.getId());
            if (agents != null && !agents.isEmpty()) {
                agentUserData = agents.get(0).getUserData();
            }

            String userDataEncrypted = Base64.getEncoder().encodeToString(agentUserData.getBytes());
            String computeName = ResourceNameUtil.buildComputeName(orgId, datastore, compute.getRole(), instanceId);
            Server server = OSContextManager.getInstance().createNovaServer(datastore, computeName, regionId, compute.getZoneName(),
                    keyPair, glanceImageId, flavorId, userDataEncrypted, volume.getCinderVolumeId(), volume.getSize(), networkIds, serverGroupId);

            // update compute information
            compute.setName(computeName);
            compute.setGlanceImageId(glanceImageId);
            compute.setNovaInstanceId(server.getId());
            compute.setStatus(ComputeStatus.STARTING.getName());
            ComputeManager.update(compute);

            volumeCinderIds.put(volume.getCinderVolumeId());
            serverNovaIds.put(server.getId());
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
