package com.dev.dbaas.workflow.steps.redis.cluster.undo;

import com.dev.dbaas.database.enties.*;
import com.dev.dbaas.manager.*;
import com.dev.dbaas.database.enties.*;
import com.dev.dbaas.manager.*;
import com.dev.dbaas.workflow.steps.redis.cluster.CheckPrerequisitesCluster;
import com.dev.dbaas.workflow.steps.redis.cluster.GenerateCloudInitCluster;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.List;

public class UndoCluster implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(UndoCluster.class);

    public UndoCluster() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        String instanceId = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName()).optString("instanceId");
        String regionId = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName()).optString("regionId");
        String datastore = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName()).optString("datastore");

        JSONObject input = origin.optJSONObject(GenerateCloudInitCluster.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        this.deleteInstance(datastore, instanceId, regionId);

        return ExecutionResult.next();
    }

    private void deleteInstance(String datastore, String instanceId, String regionId) {
        TbInstance instance = InstanceManager.findById(instanceId);
        if (instance == null || instance.getDeletedAt() != null) {
            return;
        }

        LOGGER.info("Starting to delete resources for instance " + instanceId);
        List<TbCompute> computes = ComputeManager.findByInstanceId(instanceId);
        for (TbCompute compute : computes) {
            String computeId = compute.getId();

            // delete port
            List<TbNetwork> networks = NetworkManager.findByComputeId(computeId);
            networks.forEach(network -> {
                LOGGER.info("Deleting port " + network.getNeutronPortId() + " for compute " + compute.getId());
                if (network.getNeutronPortId() != null) {
                    boolean portDeleted = OSContextManager.getInstance().deletePort(datastore, regionId, network.getNeutronPortId());
                    if (portDeleted) {
                        NetworkManager.delete(network);
                    }
                }
            });

            // delete cinder volume
            List<TbVolume> volumes = VolumeManager.findByComputeId(computeId);
            if (volumes != null && !volumes.isEmpty() && volumes.get(0).getCinderVolumeId() != null) {
                boolean volumeDeleted = OSContextManager.getInstance().deleteVolume(datastore, regionId, volumes.get(0).getCinderVolumeId());
                if (volumeDeleted) {
                    VolumeManager.delete(volumes.get(0));
                }
            }

            TbAgent agent = AgentManager.findFirstByComputeIdAndInstanceId(instanceId, computeId);
            if (agent != null) {
                AgentManager.delete(agent);
            }

            if (compute.getNovaInstanceId() != null) {
                boolean serverDeleted = OSContextManager.getInstance().deleteNovaServer(datastore, regionId, compute.getNovaInstanceId());
                if (serverDeleted) {
                    ComputeManager.delete(compute);
                }
            }
        }

        instance.setDeletedAt(LocalDateTime.now());
        InstanceManager.update(instance);

        LOGGER.info("Finished to delete resources for instance : " + instanceId);
    }
}
