package com.dev.dbaas.workflow.steps.mysql.replicaset;

import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.entity.NeutronPort;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.manager.NetworkManager;
import com.dev.dbaas.manager.OSContextManager;
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
        JSONObject input = origin.optJSONObject(getClass().getSimpleName());
        LOGGER.info(getClass().getName() + " INPUT : " + input.toString());

        JSONObject checkPrerequisites = origin.optJSONObject(CheckPrerequisites.class.getSimpleName());
        JSONArray masterComputeIds = checkPrerequisites.optJSONArray("masterComputeIds");
        JSONArray slaveComputeIds = checkPrerequisites.optJSONArray("slaveComputeIds");

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
            List<Integer> lstPortApplication = new ArrayList<>();
            lstPortApplication.add(portDefault);

            if (otherPorts != null && !otherPorts.isEmpty()) {
                for (int i = 0; i < otherPorts.length(); i++) {
                    lstPortApplication.add(Integer.parseInt(otherPorts.getString(i)));
                }
            }

            // create security group
            String securityGroupId = OSContextManager.getInstance().createSecurityGroup(datastore, regionId, instanceId, lstPortApplication);

            // add network for user account
            List<String> userSecurityGroupIds = new ArrayList<>();
            userSecurityGroupIds.add(securityGroupId);
            if (securityGroupIds != null && !securityGroupIds.isEmpty()) {
                int size = securityGroupIds.length();
                for (int j = 0; j < size; j++) {
                    String userSecurityGroupId = securityGroupIds.getString(j);
                    userSecurityGroupIds.add(userSecurityGroupId);
                }
            }

            int masterNodes = masterComputeIds.length();
            for (int i = 0; i < masterNodes; i++) {
                String computeId = masterComputeIds.getString(i);

                NeutronPort portUser = OSContextManager.getInstance().createPort(datastore, regionId, "port_" + computeId, userSecurityGroupIds, vpcId, subnetId, null);
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
                NeutronPort portManager = OSContextManager.getInstance().createPort(datastore, regionId, "port_mng_" + computeId,
                        List.of(osSecurityGroupIdManager, securityGroupId), osNetworkIdManager, osSubnetIdManager, null);

                TbNetwork networkManager = new TbNetwork();
                networkManager.setMode(Constaint.MODE_SYSTEM);
                networkManager.setIpAddress(portManager.getIpAddress());
                networkManager.setNeutronPortId(portManager.getPortId());
                networkManager.setNeutronVpcId(osNetworkIdManager);
                networkManager.setNeutronSecurityGroupId(osSecurityGroupIdManager);
                networkManager.setComputeId(computeId);
                networkManager.setProjectId(projectId);
                networkManager.setOrgId(orgId);
                networkManager.setUpdatedAt(LocalDateTime.now());
                networkManager.setCreatedAt(LocalDateTime.now());
                NetworkManager.add(networkManager);
            }

            // create network for slaves
            int slaveNodes = slaveComputeIds.length();
            for (int i = 0; i < slaveNodes; i++) {
                String computeId = slaveComputeIds.getString(i);

                NeutronPort portUser = OSContextManager.getInstance().createPort(datastore, regionId, "port_" + computeId,
                        userSecurityGroupIds, vpcId, subnetId, null);

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
                NeutronPort portManager = OSContextManager.getInstance().createPort(datastore, regionId, "port_mng_" + computeId,
                        List.of(osSecurityGroupIdManager, securityGroupId), osNetworkIdManager, osSubnetIdManager, null);

                TbNetwork networkManager = new TbNetwork();
                networkManager.setMode(Constaint.MODE_SYSTEM);
                networkManager.setIpAddress(portManager.getIpAddress());
                networkManager.setNeutronPortId(portManager.getPortId());
                networkManager.setNeutronVpcId(osNetworkIdManager);
                networkManager.setNeutronSecurityGroupId(osSecurityGroupIdManager);
                networkManager.setComputeId(computeId);
                networkManager.setProjectId(projectId);
                networkManager.setOrgId(orgId);
                networkManager.setUpdatedAt(LocalDateTime.now());
                networkManager.setCreatedAt(LocalDateTime.now());
                NetworkManager.add(networkManager);
            }

        } catch (Exception e) {
            LOGGER.warn(getClass().getSimpleName() + "-ERROR : " + e.getMessage());
            TbInstance instance = InstanceManager.findById(instanceId);
            instance.setMessage(getClass().getSimpleName() + " error : " + e.getMessage());
            instance.setStatus(Constaint.STATUS_ERROR);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
            throw e;
        }

        return ExecutionResult.next();
    }
}
