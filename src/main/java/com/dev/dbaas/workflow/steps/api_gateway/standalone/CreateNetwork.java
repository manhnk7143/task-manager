package com.dev.dbaas.workflow.steps.api_gateway.standalone;

import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.entity.NeutronPort;
import com.dev.dbaas.manager.ComputeManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.manager.NetworkManager;
import com.dev.dbaas.manager.OSContextManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jworkflow.kernel.interfaces.StepBody;
import net.jworkflow.kernel.models.ExecutionResult;
import net.jworkflow.kernel.models.StepExecutionContext;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CreateNetwork implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateNetwork.class);

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());

        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(CreateNetwork.class.getSimpleName());
        LOGGER.info(getClass().getName() + " INPUT : " + input.toString());

        JSONObject checkPrerequisites = origin.optJSONObject(CheckPrerequisites.class.getSimpleName());
        String regionId = checkPrerequisites.optString("regionId");
        String projectId = checkPrerequisites.optString("projectId");
        String userId = checkPrerequisites.optString("userId");
        String orgId = checkPrerequisites.optString("orgId");
        String instanceId = checkPrerequisites.optString("instanceId");
        String datastore = checkPrerequisites.optString("datastore");

        String vpcId = input.optString("vpcId");
        String subnetId = input.optString("subnetId");
        String osSecurityGroupIdManager = input.optString("securityGroupIdManager");
        String osNetworkIdManager = input.optString("networkIdManager");
        String osSubnetIdManager = input.optString("subnetIdManager");

        JSONArray securityGroupIds = input.optJSONArray("securityGroupIds");
        JSONArray otherPorts = input.optJSONArray("otherPorts");
        int portDefault = Integer.parseInt(input.optString("portDefault"));

        try {
            TbCompute compute = ComputeManager.findByInstanceId(instanceId).stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("Compute not found with instanceId : " + instanceId);
            }

            List<Integer> portsInstance = new ObjectMapper().readValue(otherPorts.toString(), new TypeReference<>() {
            });
            portsInstance.add(portDefault);

            // create security group for client accept to DB instance
            String securityGroupId = OSContextManager.getInstance().createSecurityGroup(datastore, regionId, instanceId, portsInstance);

            // add network for user account
            List<String> userSecurityGroupIds = new ArrayList<>();
            userSecurityGroupIds.add(securityGroupId);
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

            NeutronPort portUser = OSContextManager.getInstance()
                    .createPort(datastore, regionId, "port_" + compute.getId(), userSecurityGroupIds, vpcId, subnetId, null);

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
            NeutronPort portManager = OSContextManager.getInstance().createPort(datastore, regionId, "port_mng_" +
                    compute.getId(), List.of(osSecurityGroupIdManager), osNetworkIdManager, osSubnetIdManager, null);

            TbNetwork networkManager = new TbNetwork();
            networkManager.setMode(Constaint.MODE_SYSTEM);
            networkManager.setIpAddress(portManager.getIpAddress());
            networkManager.setNeutronPortId(portManager.getPortId());
            networkManager.setNeutronVpcId(osNetworkIdManager);
            networkManager.setNeutronSecurityGroupId(osSecurityGroupIdManager);
            networkManager.setComputeId(compute.getId());
            networkManager.setProjectId(projectId);
            networkManager.setOrgId(orgId);
            networkManager.setUpdatedAt(LocalDateTime.now());
            networkManager.setCreatedAt(LocalDateTime.now());
            NetworkManager.add(networkManager);

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
