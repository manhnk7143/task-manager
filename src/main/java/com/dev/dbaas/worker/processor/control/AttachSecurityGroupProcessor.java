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
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;

public class AttachSecurityGroupProcessor implements ProcessorBase<ControlJob> {

    private static final Logger LOGGER = Logger.getLogger(AttachSecurityGroupProcessor.class);

    @Override
    public boolean process(ControlJob job) throws Exception {
        LOGGER.info("[4CMS] AttachSecurityGroupProcessor " + job.getServiceId() + " - " + job.getData());
        job.decodePacket();

        JSONObject input = job.getJsonData();
        String instanceId = input.optString("instanceId");
        String datastore = input.optString("datastore");
        String regionId = input.optString("regionId");
        JSONArray jsonSecurityGroupIds = new JSONArray(input.optString("securityGroupIds"));

        HashSet<String> securityGroupIds = new HashSet<>();
        for (int i = 0; i < jsonSecurityGroupIds.length(); i++) {
            securityGroupIds.add(jsonSecurityGroupIds.getString(i));
        }
        if (securityGroupIds.isEmpty()) {
            return true;
        }
        List<TbCompute> computes = ComputeManager.findByInstanceId(instanceId);

        for (TbCompute compute : computes) {
            TbNetwork network = NetworkManager.findByComputeIdAndMode(compute.getId(), Constaint.MODE_USER);
            OSContextManager.getInstance().updateSecurityGroupManualForPort(datastore, regionId, network.getNeutronPortId(), securityGroupIds, true);
        }

        TbInstance instance = InstanceManager.findById(instanceId);
        JSONArray securityGroupUpdated = new JSONArray();
        for (String securityGroupId : securityGroupIds) {
            securityGroupUpdated.put(securityGroupId);
        }
        instance.setNeutronSecurityGroupClientIds(securityGroupUpdated.toString());
        InstanceManager.update(instance);

        return true;
    }
}