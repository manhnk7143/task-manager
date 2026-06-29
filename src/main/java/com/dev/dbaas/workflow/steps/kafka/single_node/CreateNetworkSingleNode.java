package com.dev.dbaas.workflow.steps.kafka.single_node;

import com.dev.dbaas.common.RegionResource;
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

public class CreateNetworkSingleNode implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateNetworkSingleNode.class);

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        JSONObject origin = (JSONObject) context.getWorkflow().getData();
        JSONObject input = origin.optJSONObject(CreateNetworkSingleNode.class.getSimpleName());
        JSONObject checkPrerequisitesInput = origin.optJSONObject(CheckPrerequisitesSingleNode.class.getSimpleName());
        LOGGER.info("Input CreateNetworkSingleNode : " + input.toString());

        String regionId = checkPrerequisitesInput.optString("regionId");
        String instanceId = checkPrerequisitesInput.optString("instanceId");
        String orgId = checkPrerequisitesInput.optString("orgId");
        String projectId = checkPrerequisitesInput.optString("projectId");
        String datastore = checkPrerequisitesInput.optString("datastore");

        String vpcId = input.optString("vpcId");
        String subnetId = input.optString("subnetId");
        JSONArray securityGroupIds = input.optJSONArray("securityGroupIds");
//        int portDefault = Integer.parseInt(input.optString("portDefault"));

        RegionResource opsResource = new RegionResource(regionId, datastore);
        String securityGroupIdManager = opsResource.getSecurityGroupMng();
        String networkIdManager = opsResource.getNetworkMng();
        String subnetIdManager = opsResource.getSubnetMng();

        JSONArray portsKafka = input.optJSONArray("portsKafka");
        boolean enableBasicAuth = input.optBoolean("enableBasicAuth");

        try {
            List<Integer> portsInstance = new ObjectMapper().readValue(portsKafka.toString(), new TypeReference<>() {});
            portsInstance.addAll(opsResource.getPortDefault());

            // create security group
            String securityGroupId = OSContextManager.getInstance().createSecurityGroup(datastore, regionId, instanceId, portsInstance);

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

            TbCompute compute = ComputeManager.findByInstanceId(instanceId).stream().findFirst().orElse(null);
            if (compute == null) {
                throw new Exception("Compute not found with instanceId " + instanceId);
            }

            NeutronPort portUser = OSContextManager.getInstance().createPort(datastore, regionId,
                    orgId + "_kafka_single_node_" + compute.getId(), userSecurityGroupIds, vpcId, subnetId, null);
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
            NeutronPort portManager = OSContextManager.getInstance().createPort(
                    datastore, regionId, orgId + "_kafka_single_node_mng_" + compute.getId(), List.of(securityGroupIdManager),
                    networkIdManager, subnetIdManager, null);

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

        } catch (Exception e) {
            LOGGER.warn("Create network error : " + e.getMessage());
            TbInstance instance = InstanceManager.findById(instanceId);
            instance.setMessage(e.getMessage());
            instance.setStatus(Constaint.STATUS_ERROR);
            instance.setUpdatedAt(LocalDateTime.now());
            InstanceManager.update(instance);
            throw e;
        }

        return ExecutionResult.next();
    }
}
