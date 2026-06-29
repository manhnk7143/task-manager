package com.dev.dbaas.workflow.steps.redis.cluster;

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

public class CreateNetworkCluster implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateNetworkCluster.class);

    public CreateNetworkCluster() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject checkPrerequisitesInput = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName());

        String regionId = checkPrerequisitesInput.optString("regionId");
        String instanceId = checkPrerequisitesInput.optString("instanceId");
        String datastore = checkPrerequisitesInput.optString("datastore");
        String orgId = checkPrerequisitesInput.optString("orgId");
        String projectId = checkPrerequisitesInput.optString("projectId");
        JSONArray masterComputeIds = checkPrerequisitesInput.optJSONArray("masterComputeIds");
        JSONArray slaveComputeIds = checkPrerequisitesInput.optJSONArray("slaveComputeIds");

        JSONObject input = origin.optJSONObject(CreateNetworkCluster.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String vpcId = input.optString("vpcId");
        String subnetId = input.optString("subnetId");
        JSONArray securityGroupIds = input.optJSONArray("securityGroupIds");
        int portRedis = Integer.parseInt(input.optString("portDefault"));
        int busPortRedis = Integer.parseInt(input.optString("busPortRedis"));
        String securityGroupIdManager = input.optString("securityGroupIdManager");
        String networkIdManager = input.optString("networkIdManager");
        String subnetIdManager = input.optString("subnetIdManager");

        try {
            // create security group
            String securityGroupId = OSContextManager.getInstance().createSecurityGroup(datastore, regionId, instanceId, List.of(portRedis, busPortRedis));

            // create network for masters
            int masterNodes = masterComputeIds.length();
            for (int i = 0; i < masterNodes; i++) {
                String computeId = masterComputeIds.getString(i);

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
                NeutronPort portManager = OSContextManager.getInstance().createPort(datastore, regionId, "port_mng_" + computeId, List.of(securityGroupIdManager, securityGroupId), networkIdManager, subnetIdManager, null);

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

            // create network for slaves
            int slaveNodes = slaveComputeIds.length();
            for (int i = 0; i < slaveNodes; i++) {
                String computeId = slaveComputeIds.getString(i);

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
                if (!userSecurityGroupIds.contains(securityGroupId)) {
                    userSecurityGroupIds.add(securityGroupId);
                }

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
                NeutronPort portManager = OSContextManager.getInstance().createPort(datastore, regionId, "port_mng_" + computeId, List.of(securityGroupIdManager, securityGroupId), networkIdManager, subnetIdManager, null);

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
            instance.setMessage("create network error");
            instance.setStatus(Constaint.STATUS_ERROR);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
            throw e;
        }

        return ExecutionResult.next();
    }

}



