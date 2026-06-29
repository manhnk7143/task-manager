package com.dev.dbaas.workflow.steps.redis.master_slave.master;

import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.entity.NeutronPort;
import com.dev.dbaas.manager.ComputeManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.manager.NetworkManager;
import com.dev.dbaas.manager.OSContextManager;
import com.dev.dbaas.workflow.steps.redis.master_slave.CheckPrerequisitesMasterSlave;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CreateNetworkMasterCluster implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateNetworkMasterCluster.class);

    public CreateNetworkMasterCluster() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(CreateNetworkMasterCluster.class.getSimpleName());
        JSONObject checkPrerequisitesInput = origin.optJSONObject(CheckPrerequisitesMasterSlave.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String orgId = checkPrerequisitesInput.optString("orgId");
        String projectId = checkPrerequisitesInput.optString("projectId");
        String instanceId = checkPrerequisitesInput.optString("instanceId");
        String regionId = checkPrerequisitesInput.optString("regionId");
        String datastore = checkPrerequisitesInput.optString("datastore");

        String vpcId = input.optString("vpcId");
        String subnetId = input.optString("subnetId");
        JSONArray securityGroupIds = input.optJSONArray("securityGroupIds");
        int portDefault = Integer.parseInt(input.optString("portDefault"));
        String securityGroupIdManager = input.optString("securityGroupIdManager");
        String networkIdManager = input.optString("networkIdManager");
        String subnetIdManager = input.optString("subnetIdManager");
        int portSentinel = Integer.parseInt(input.optString("portSentinel"));

        try {
            TbCompute compute = ComputeManager.findByInstanceIdAndRole(instanceId, Constaint.MASTER_ROLE).stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("Compute not found with instanceId : " + instanceId);
            }
            String securityGroupId = OSContextManager.getInstance().createSecurityGroup(datastore, regionId, instanceId, List.of(portDefault, portSentinel));

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

            NeutronPort portUser = OSContextManager.getInstance().createPort(datastore, regionId, "port_" + compute.getId(), userSecurityGroupIds, vpcId, subnetId, null);

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
            NeutronPort portManager = OSContextManager.getInstance().createPort(datastore, regionId, "port_mng_" + compute.getId(),
                    List.of(securityGroupIdManager, securityGroupId), networkIdManager, subnetIdManager, null);

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
            input.put("portDefault", portDefault);
            input.put("ipAddress", portUser.getIpAddress());
            input.put("ipAddressMaster", networkUser.getIpAddress());
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



