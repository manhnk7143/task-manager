package com.dev.dbaas.workflow.steps.mongodb.replicaset.secondary;

import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.entity.NeutronPort;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.manager.NetworkManager;
import com.dev.dbaas.manager.OSContextManager;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.CheckPrerequisitesReplicaset;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.primary.CreateNetworkPrimaryReplicaset;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CreateNetworkSecondaryReplicaset implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateNetworkSecondaryReplicaset.class);

    public CreateNetworkSecondaryReplicaset() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(CreateNetworkSecondaryReplicaset.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        JSONObject checkPrerequisites = origin.optJSONObject(CheckPrerequisitesReplicaset.class.getSimpleName());
        String regionId = checkPrerequisites.optString("regionId");
        String datastore = checkPrerequisites.optString("datastore");
        String orgId = checkPrerequisites.optString("orgId");
        String projectId = checkPrerequisites.optString("projectId");
        String instanceId = checkPrerequisites.optString("instanceId");
        JSONArray secondaryComputeIds = checkPrerequisites.optJSONArray("secondaryComputeIds");

        String subnetId = input.optString("subnetId");
        String networkIdManager = input.optString("networkIdManager");
        String securityGroupIdManager = input.optString("securityGroupIdManager");
        String vpcId = input.optString("vpcId");
        String subnetIdManager = input.optString("subnetIdManager");
        JSONArray securityGroupIds = input.optJSONArray("securityGroupIds");
        String ipAddress = input.optString("ipAddress");

        String securityGroupId = origin.optJSONObject(CreateNetworkPrimaryReplicaset.class.getSimpleName()).optString("securityGroupId");

        try {
            int secondaryNodes = secondaryComputeIds.length();
            for (int i = 0; i < secondaryNodes; i++) {
                String computeId = secondaryComputeIds.getString(i);

                // add network for user account
                List<String> userSecurityGroupIds = new ArrayList<>();
                userSecurityGroupIds.add(securityGroupId);
                if (securityGroupIds != null) {
                    int size = securityGroupIds.length();
                    for (int j = 0; j < size; j++) {
                        String userSecurityGroupId = securityGroupIds.getString(j);
                        userSecurityGroupIds.add(userSecurityGroupId);
                    }
                }
                if (!userSecurityGroupIds.contains(securityGroupId)) {
                    userSecurityGroupIds.add(securityGroupId);
                }

                NeutronPort portUser = OSContextManager.getInstance().createPort(datastore, regionId, "port_" + computeId, userSecurityGroupIds, vpcId, subnetId, ipAddress);

                TbNetwork networkUser = new TbNetwork();
                networkUser.setMode(Constaint.MODE_USER);
                networkUser.setIpAddress(portUser.getIpAddress());
                networkUser.setNeutronPortId(portUser.getPortId());
                networkUser.setNeutronSubnetId(subnetId);
                networkUser.setNeutronVpcId(vpcId);
                networkUser.setNeutronSecurityGroupId(securityGroupId);
                networkUser.setComputeId(computeId);
                networkUser.setProjectId(projectId);
                networkUser.setOrgId(orgId);
                networkUser.setUpdatedAt(LocalDateTime.now());
                networkUser.setCreatedAt(LocalDateTime.now());
                NetworkManager.add(networkUser);

                // add network for manager account (VLAN)
                NeutronPort portManager = OSContextManager.getInstance().createPort(datastore, regionId, "port_mng_" + computeId, List.of(securityGroupIdManager, securityGroupId), networkIdManager, subnetIdManager, ipAddress);

                TbNetwork networkManager = new TbNetwork();
                networkManager.setMode(Constaint.MODE_SYSTEM);
                networkManager.setIpAddress(portManager.getIpAddress());
                networkManager.setNeutronPortId(portManager.getPortId());
                networkManager.setNeutronVpcId(networkIdManager);
                networkManager.setNeutronSecurityGroupId(securityGroupIdManager);
                networkManager.setComputeId(computeId);
                networkManager.setProjectId(projectId);
                networkManager.setOrgId(orgId);
                networkManager.setUpdatedAt(LocalDateTime.now());
                networkManager.setCreatedAt(LocalDateTime.now());
                NetworkManager.add(networkManager);
            }
        } catch (Exception e) {
            LOGGER.warn("Create network error : " + e.getMessage());
            TbInstance instance = InstanceManager.findById(instanceId);
            instance.setMessage("Create network error : " + e.getMessage());
            instance.setStatus(Constaint.STATUS_ERROR);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
            throw e;
        }

        return ExecutionResult.next();
    }
}



