package com.dev.dbaas.workflow.steps.redis.cluster;

import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbFlavor;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.manager.FlavorManager;
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

public class CheckPrerequisitesCluster implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CheckPrerequisitesCluster.class);

    public CheckPrerequisitesCluster() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String orgId = input.optString("orgId");
        String projectId = input.optString("projectId");
        String userId = input.optString("userId");
        String regionId = input.optString("regionId");

        String instanceId = input.optString("instanceId");
        String datastore = input.optString("datastore");
        String flavorId = input.optString("flavorId");
        String tag = input.optString("tag");
        String vpcId = input.optString("vpcId");
        String subnetId = input.optString("subnetId");
        String masterComputeIds = input.optString("masterComputeIds");
        String slaveComputeIds = input.optString("slaveComputeIds");

        try {
            OSClient.OSClientV3 os = OSContextManager.getInstance().getClient(regionId, datastore);

            TbInstance instance = InstanceManager.findById(instanceId);
            if (instance == null) {
                LOGGER.error("Not found instance : " + instanceId);
                throw new Exception();
            }

            // checkExistFlavor
            Flavor flavor = os.compute().flavors().get(flavorId);
            if (flavor == null) {
                LOGGER.error("Not found flavor : " + flavorId);
                instance.setMessage("Not found flavor : " + flavorId);
                instance.setStatus(Constaint.STATUS_ERROR);
                instance.setUpdatedAt(LocalDateTime.now());
                InstanceManager.update(instance);
                throw new Exception();
            }

            // check and insert Flavor
            TbFlavor itemFlavor = FlavorManager.findByFlavorId(flavorId);
            if (itemFlavor == null) {
                itemFlavor = new TbFlavor();
                itemFlavor.setFlavorName(flavor.getName());
                itemFlavor.setOsFlavorId(flavorId);
                itemFlavor.setDisk(flavor.getDisk());
                itemFlavor.setRam(flavor.getRam());
                itemFlavor.setVCpus(flavor.getVcpus());
                itemFlavor.setCreatedAt(LocalDateTime.now());
                itemFlavor.setUpdatedAt(LocalDateTime.now());
                FlavorManager.add(itemFlavor);
            }


            Map<String, String> filterParams = new HashMap<>();
            filterParams.put("tag", tag);
            filterParams.put("status", "active");
            filterParams.put("sort", "created_at:desc");
            filterParams.put("limit", "1");
            List<? extends Image> images = os.imagesV2().list(filterParams);
            Image image = images != null && !images.isEmpty() ? images.get(0) : null;
            if (image == null) {
                LOGGER.error("Not found image : " + tag);
                instance.setStatus(Constaint.STATUS_ERROR);
                instance.setMessage("Not found image to create server");
                instance.setUpdatedAt(LocalDateTime.now());
                InstanceManager.update(instance);
                throw new Exception();
            }

            // network and subnet
            Network vpc = os.networking().network().get(vpcId);
            if (vpc == null) {
                LOGGER.error("Not found VPC : " + vpcId);
                instance.setStatus(Constaint.STATUS_ERROR);
                instance.setMessage("Not found VPC");
                instance.setUpdatedAt(LocalDateTime.now());
                InstanceManager.update(instance);
                throw new Exception();
            }

            Subnet subnet = os.networking().subnet().get(subnetId);
            if (subnet == null) {
                LOGGER.error("Not found subnet : " + subnetId);
                instance.setStatus(Constaint.STATUS_ERROR);
                instance.setMessage("Not found subnet");
                instance.setUpdatedAt(LocalDateTime.now());
                InstanceManager.update(instance);
                throw new Exception();
            }

            if (!subnet.getNetworkId().equals(vpcId)) {
                LOGGER.error("Subnet[" + subnetId + "] does not belong to this VPC " + vpcId);
                instance.setStatus(Constaint.STATUS_ERROR);
                instance.setMessage("Subnet[" + subnetId + "] does not belong to this VPC " + vpcId);
                instance.setUpdatedAt(LocalDateTime.now());
                InstanceManager.update(instance);
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



