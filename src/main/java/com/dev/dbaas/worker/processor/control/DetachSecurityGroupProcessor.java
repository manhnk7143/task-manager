package com.dev.dbaas.worker.processor.control;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.manager.ComputeManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.manager.NetworkManager;
import com.dev.dbaas.manager.OSContextManager;
import com.dev.dbaas.worker.job.ControlJob;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

public class DetachSecurityGroupProcessor implements ProcessorBase<ControlJob> {

    private static final Logger LOGGER = Logger.getLogger(DetachSecurityGroupProcessor.class);

    private static final int SERVER_INTERNAL_ERROR = -100;
    private static final int SUCCESSFUL = 1;
    private static final int REQUIRE_NAMESPACE = -5;
    private static final int REQUIRE_STATUS = -6;
    private static final int REQUIRE_BACKUP_ID = -7;
    private static final int REQUIRE_ACCOUNT_ID = -8;
    private static final int NOT_FOUND_ACCOUNT_MEMBER = -9;
    private static final int PERMISSION_DENIED = -99;

    @Override
    public boolean process(ControlJob job) throws Exception {
        LOGGER.info("[4CMS] DetachSecurityGroupProcessor " + job.getServiceId() + " - " + job.getData());
        job.decodePacket();

        JSONObject input = job.getJsonData();
        String instanceId = input.optString("instanceId");
        String datastore = input.optString("datastore");
        String regionId = input.optString("regionId");
        JSONArray jsonSecurityGroupIds = new JSONArray(input.optString("securityGroupIds"));

        LinkedHashSet<String> securityGroupIds = new ObjectMapper().readValue(jsonSecurityGroupIds.toString(), new TypeReference<>() {});
        if (securityGroupIds.isEmpty()) {
            return true;
        }
        List<TbCompute> computes = ComputeManager.findByInstanceId(instanceId);

        for (TbCompute compute : computes) {
            TbNetwork network = NetworkManager.findByComputeIdAndMode(compute.getId(), Constaint.MODE_USER);
            OSContextManager.getInstance().updateSecurityGroupManualForPort(datastore, regionId, network.getNeutronPortId(), securityGroupIds, false);
        }

        TbInstance instance = InstanceManager.findById(instanceId);
        HashSet<String> securityGroupAttachedInstance = new ObjectMapper().readValue(instance.getNeutronSecurityGroupClientIds(), new TypeReference<>() {
        });
        securityGroupAttachedInstance.removeAll(securityGroupIds);

        JSONArray securityGroupUpdated = new JSONArray();
        for (String securityGroupId : securityGroupAttachedInstance) {
            securityGroupUpdated.put(securityGroupId);
        }
        instance.setNeutronSecurityGroupClientIds(securityGroupUpdated.toString());
        InstanceManager.update(instance);

        return true;
    }
}
