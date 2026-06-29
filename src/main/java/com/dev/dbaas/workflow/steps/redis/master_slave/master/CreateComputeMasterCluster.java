package com.dev.dbaas.workflow.steps.redis.master_slave.master;

import com.dev.dbaas.config.ComputeStatus;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.manager.ComputeManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.manager.NetworkManager;
import com.dev.dbaas.manager.OSContextManager;
import com.dev.dbaas.utils.ResourceNameUtil;
import com.dev.dbaas.utils.datastore.OpenstackResource;
import com.dev.dbaas.workflow.steps.redis.master_slave.CheckPrerequisitesMasterSlave;
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

public class CreateComputeMasterCluster implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateComputeMasterCluster.class);

    public CreateComputeMasterCluster() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(CreateComputeMasterCluster.class.getSimpleName());
        JSONObject checkPrerequisitesInput = origin.optJSONObject(CheckPrerequisitesMasterSlave.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String glanceImageId = checkPrerequisitesInput.optString("glanceImageId");
        String regionId = checkPrerequisitesInput.optString("regionId");
        String flavorId = checkPrerequisitesInput.optString("flavorId");
        String userData = origin.optJSONObject(GenerateCloudInitMasterCluster.class.getSimpleName()).optString("userData");
        String cinderVolumeId = origin.optJSONObject(CreateVolumeMasterCluster.class.getSimpleName()).optString("cinderVolumeId");
        int volumeSize = origin.optJSONObject(CreateVolumeMasterCluster.class.getSimpleName()).optInt("volumeSize");
        String datastore = checkPrerequisitesInput.optString("datastore");
        String orgId = checkPrerequisitesInput.optString("orgId");
        String instanceId = checkPrerequisitesInput.optString("instanceId");

        String keyPair = input.optString("keyPair");

        JSONArray volumeCinderIds = new JSONArray();
        JSONArray serverNovaIds = new JSONArray();

        try {
            TbCompute compute = ComputeManager.findByInstanceIdAndRole(instanceId, Constaint.MASTER_ROLE).stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("Not found compute with instanceId " + instanceId);
            }

            String serverGroupId = OSContextManager.getInstance().createServerGroupManual(datastore, regionId, instanceId, Constaint.POLICY_SERVER_GROUP);

            TbInstance instance = InstanceManager.findById(instanceId);
            if (instance != null) {
                instance.setServerGroupId(serverGroupId);
                InstanceManager.update(instance);
            }

            List<TbNetwork> networks = NetworkManager.findByComputeId(compute.getId());
            List<String> networkIds = new ArrayList<>();
            for (TbNetwork port : networks) {
                networkIds.add(port.getNeutronPortId());
            }

            String userDataEncrypted = Base64.getEncoder().encodeToString(userData.getBytes());
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

            // add data to next step
            input.put("serverGroupId", serverGroupId);
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



