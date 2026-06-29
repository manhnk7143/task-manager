package com.dev.dbaas.workflow.steps.mongodb.replicaset.arbiter;

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

public class CreateComputeArbiterReplicaset implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateComputeArbiterReplicaset.class);

    public CreateComputeArbiterReplicaset() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(CreateComputeArbiterReplicaset.class.getSimpleName());
        JSONObject checkPrerequisitesReplInput = origin.optJSONObject(CheckPrerequisitesReplicaset.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String glanceImageId = checkPrerequisitesReplInput.optString("glanceImageId");
        String regionId = checkPrerequisitesReplInput.optString("regionId");
        String userData = origin.optJSONObject(GenerateCloudInitArbiterReplicaset.class.getSimpleName()).optString("userData");
        String datastore = checkPrerequisitesReplInput.optString("datastore");
        String orgId = checkPrerequisitesReplInput.optString("orgId");
        String serverGroupId = origin.optJSONObject(CreateComputePrimaryReplicaset.class.getSimpleName()).optString("serverGroupId");

        String instanceId = checkPrerequisitesReplInput.optString("instanceId");
        String keyPair = input.optString("keyPair");

        try {
            JSONArray volumeCinderIds = new JSONArray();
            JSONArray serverNovaIds = new JSONArray();

            TbCompute compute = ComputeManager.findByInstanceIdAndRole(instanceId, Constaint.ARBITER_ROLE)
                    .stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("Not found arbiter compute with instanceId " + instanceId);
            }

            List<TbNetwork> networks = NetworkManager.findByComputeId(compute.getId());
            List<String> networkIds = new ArrayList<>();
            for (TbNetwork port : networks) {
                networkIds.add(port.getNeutronPortId());
            }

            String userDataEncrypted = Base64.getEncoder().encodeToString(userData.getBytes());
            String computeName = ResourceNameUtil.buildComputeName(orgId, datastore, compute.getRole(), instanceId);
            Server server = OSContextManager.getInstance().createNovaServer(datastore, computeName, regionId, compute.getZoneName(), keyPair,
                    glanceImageId, compute.getFlavorId(), userDataEncrypted, null, 0, networkIds, serverGroupId);

            // update compute information
            compute.setName(computeName);
            compute.setGlanceImageId(glanceImageId);
            compute.setNovaInstanceId(server.getId());
            compute.setStatus(ComputeStatus.STARTING.getName());
            ComputeManager.update(compute);

            // arbiter server not use volume
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
