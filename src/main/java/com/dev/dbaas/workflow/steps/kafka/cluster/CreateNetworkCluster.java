package com.dev.dbaas.workflow.steps.kafka.cluster;

import com.dev.dbaas.common.RegionResource;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.entity.NeutronPort;
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

public class CreateNetworkCluster implements StepBody {

    private static final Logger LOGGER = Logger.getLogger(CreateNetworkCluster.class);

    public CreateNetworkCluster() {
    }

    @Override
    public ExecutionResult run(StepExecutionContext context) throws Exception {
        LOGGER.info("[4CMS] " + getClass().getName());
        JSONObject origin = (JSONObject) context.getWorkflow().getData();

        JSONObject input = origin.optJSONObject(this.getClass().getSimpleName());
        JSONObject checkPrerequisitesInput = origin.optJSONObject(CheckPrerequisitesCluster.class.getSimpleName());
        LOGGER.info("Input : " + input.toString());

        String regionId = checkPrerequisitesInput.optString("regionId");
        String instanceId = checkPrerequisitesInput.optString("instanceId");
        String datastore = checkPrerequisitesInput.optString("datastore");
        String orgId = checkPrerequisitesInput.optString("orgId");
        String projectId = checkPrerequisitesInput.optString("projectId");
        JSONArray brokerComputeIds = checkPrerequisitesInput.optJSONArray("brokerComputeIds");

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
            List<Integer> portsInstance = new ObjectMapper().readValue(portsKafka.toString(), new TypeReference<>() {
            });
            portsInstance.addAll(opsResource.getPortDefault());

            // create security group
            String securityGroupId = OSContextManager.getInstance().createSecurityGroup(datastore, regionId, instanceId, portsInstance);

            // create network for brokers
            int brokers = brokerComputeIds.length();
            for (int i = 0; i < brokers; i++) {
                String computeId = brokerComputeIds.getString(i);

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

                NeutronPort portUser = OSContextManager.getInstance().createPort(datastore, regionId,
                        orgId + "_kafka_" + computeId, userSecurityGroupIds, vpcId, subnetId, null);
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
                NeutronPort portManager = OSContextManager.getInstance().createPort(
                        datastore, regionId, orgId + "_kafka_mng_" + computeId, List.of(securityGroupIdManager),
                        networkIdManager, subnetIdManager, null);

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
            LOGGER.error("Create network error : " + e.getMessage());
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



