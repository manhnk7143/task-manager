package com.dev.dbaas.workflow.steps.mongodb.replicaset.primary;

import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.entity.NeutronPort;
import com.dev.dbaas.manager.ComputeManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.manager.NetworkManager;
import com.dev.dbaas.manager.OSContextManager;
import com.dev.dbaas.workflow.steps.mongodb.replicaset.CheckPrerequisitesReplicaset;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CreateNetworkPrimaryReplicaset implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateNetworkPrimaryReplicaset.class);

    public CreateNetworkPrimaryReplicaset() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(CreateNetworkPrimaryReplicaset.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        JSONObject checkPrerequisites = origin.optJSONObject(CheckPrerequisitesReplicaset.class.getSimpleName());
        String regionId = checkPrerequisites.optString("regionId");
        String datastore = checkPrerequisites.optString("datastore");
        String orgId = checkPrerequisites.optString("orgId");
        String projectId = checkPrerequisites.optString("projectId");
        String instanceId = checkPrerequisites.optString("instanceId");

        String subnetId = input.optString("subnetId");
        int portDefault = Integer.parseInt(input.optString("portDefault"));
        String networkIdManager = input.optString("networkIdManager");
        String securityGroupIdManager = input.optString("securityGroupIdManager");
        String vpcId = input.optString("vpcId");
        String subnetIdManager = input.optString("subnetIdManager");
        JSONArray securityGroupIds = input.optJSONArray("securityGroupIds");

        try {
            TbCompute compute = ComputeManager.findByInstanceIdAndRole(instanceId, Constaint.PRIMARY_ROLE).stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("Compute not found with instanceId : " + instanceId);
            }

            String securityGroupId = OSContextManager.getInstance().createSecurityGroup(datastore, regionId, instanceId, portDefault);

            // add network for user account
            List<String> userSecurityGroupIds = new ArrayList<>();
            if (securityGroupIds != null) {
                int size = securityGroupIds.length();
                for (int i = 0; i < size; i++) {
                    String userSecurityGroupId = securityGroupIds.getString(i);
                    userSecurityGroupIds.add(userSecurityGroupId);
                }
            }

            // save list security group create by user
            TbInstance instance = InstanceManager.findById(instanceId);
            if (instance != null) {
                instance.setNeutronSecurityGroupClientIds(new JSONArray(userSecurityGroupIds).toString());
                InstanceManager.update(instance);
            }
            userSecurityGroupIds.add(securityGroupId);

            NeutronPort portUser = OSContextManager.getInstance().createPort(datastore, regionId, "port_" + compute.getId(), userSecurityGroupIds, vpcId, subnetId, "");

            TbNetwork networkUser = new TbNetwork();
            networkUser.setMode(Constaint.MODE_USER);
            networkUser.setIpAddress(portUser.getIpAddress());
            networkUser.setNeutronPortId(portUser.getPortId());
            networkUser.setNeutronSubnetId(subnetId);
            networkUser.setNeutronVpcId(vpcId);
            networkUser.setNeutronSecurityGroupId(securityGroupId);
            networkUser.setComputeId(compute.getId());
            networkUser.setProjectId(projectId);
            networkUser.setOrgId(orgId);
            networkUser.setUpdatedAt(LocalDateTime.now());
            networkUser.setCreatedAt(LocalDateTime.now());
            NetworkManager.add(networkUser);

            // add network for manager account (VLAN)
            NeutronPort portManager = OSContextManager.getInstance().createPort(datastore, regionId, "port_mng_" + compute.getId(), List.of(securityGroupIdManager, securityGroupId), networkIdManager, subnetIdManager, "");

            TbNetwork networkManager = new TbNetwork();
            networkManager.setMode(Constaint.MODE_SYSTEM);
            networkManager.setIpAddress(portManager.getIpAddress());
            networkManager.setNeutronPortId(portManager.getPortId());
            networkManager.setNeutronVpcId(networkIdManager);
            networkManager.setNeutronSecurityGroupId(securityGroupIdManager);
            networkManager.setComputeId(compute.getId());
            networkManager.setProjectId(projectId);
            networkManager.setOrgId(orgId);
            networkManager.setUpdatedAt(LocalDateTime.now());
            networkManager.setCreatedAt(LocalDateTime.now());
            NetworkManager.add(networkManager);

            // add data to next step
            input.put("ipAddress", networkUser.getIpAddress());
            input.put("managerIpAddress", portManager.getIpAddress());
            input.put("ipPrimary", networkUser.getIpAddress());
            input.put("securityGroupId", securityGroupId);
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



