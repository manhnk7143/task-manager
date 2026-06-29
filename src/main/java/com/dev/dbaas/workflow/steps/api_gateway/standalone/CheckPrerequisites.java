package com.dev.dbaas.workflow.steps.api_gateway.standalone;

import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.manager.OSContextManager;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.image.v2.Image;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Subnet;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckPrerequisites implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CheckPrerequisites.class);

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(CheckPrerequisites.class.getSimpleName());
        LOGGER.info(getClass().getName() + " INPUT : " + input.toString());

        String orgId = input.optString("orgId");
        String projectId = input.optString("projectId");
        String userId = input.optString("userId");
        String regionId = input.optString("regionId");

        String instanceId = input.optString("instanceId");
        String datastore = input.optString("datastore");
        String flavorId = input.optString("flavorId");
        String imageTag = input.optString("tag");
        String vpcId = input.optString("vpcId");
        String subnetId = input.optString("subnetId");

        try {
            OSClient.OSClientV3 os = OSContextManager.getInstance().getClient(regionId, datastore);

            // checkExistFlavor
            Flavor flavor = os.compute().flavors().get(flavorId);
            if (flavor == null) {
                LOGGER.error("Not found flavor : " + flavorId);
                throw new Exception("Not found flavor : " + flavorId);
            }

            Map<String, String> filterParams = new HashMap<>();
            filterParams.put("tag", imageTag);
            filterParams.put("status", "active");
            filterParams.put("sort", "created_at:desc");
            filterParams.put("limit", "1");
            List<? extends Image> images = os.imagesV2().list(filterParams);
            Image image = images != null && !images.isEmpty() ? images.get(0) : null;
            if (image == null) {
                LOGGER.error("Not found Image : " + imageTag);
                throw new Exception("Not found Image : " + imageTag);
            }

            // network and subnet
            Network vpc = os.networking().network().get(vpcId);
            if (vpc == null) {
                LOGGER.error("Not found VPC : " + vpcId);
                throw new Exception("Not found VPC : " + vpcId);
            }

            Subnet subnet = os.networking().subnet().get(subnetId);
            if (subnet == null) {
                LOGGER.error("Not found VPC : " + imageTag);
                throw new Exception();
            }

            if (!subnet.getNetworkId().equals(vpcId)) {
                LOGGER.error("Subnet[" + subnetId + "] does not belong to this VPC " + vpcId);
                throw new Exception();
            }
            input.put("glanceImageId", image.getId());
        } catch (Exception e) {
            LOGGER.warn("Check prerequisites error : " + e.getMessage());
            TbInstance instance = InstanceManager.findById(instanceId);
            instance.setMessage("create prerequisites error : " + e.getMessage());
            instance.setStatus(Constaint.STATUS_ERROR);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
            throw e;
        }

        return ExecutionResult.next();
    }
}
