package com.dev.dbaas.worker.processor.control;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbAgent;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.enties.TbInstance;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.manager.AgentManager;
import com.dev.dbaas.manager.ComputeManager;
import com.dev.dbaas.manager.InstanceManager;
import com.dev.dbaas.packet.AppServiceType;
import com.dev.dbaas.protocol.ResponsePacket;
import com.dev.dbaas.worker.job.ControlJob;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CreateBackupProcessor implements ProcessorBase<ControlJob> {

    private static final Logger LOGGER = Logger.getLogger(CreateBackupProcessor.class);

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

        LOGGER.info("[4CMS] " + job.getServiceId() + " - " + job.getData());
        job.decodePacket();

        JSONObject input = job.getJsonData();
        String backupId = input.optString("backupId");
        String instanceId = input.optString("instanceId");
        String datastore = input.optString("datastore");
        String url = input.optString("url");
        String datastoreModeCode = input.optString("datastoreModeCode");

        TbInstance instance = InstanceManager.findById(instanceId);
        if (instance == null) {
            LOGGER.warn("Not found Instance by instanceId = " + instanceId);
            return false;
        }

        List<TbCompute> computes = new ArrayList<>();
        if (datastore.equalsIgnoreCase(Constaint.DATASTORE_REDIS)) {
            List<TbCompute> masterComputes = ComputeManager.findByInstanceIdAndRole(instanceId, Constaint.MASTER_ROLE);
            computes.add(masterComputes.get(0));
        } else if (datastore.equalsIgnoreCase(Constaint.DATASTORE_MONGO_DB)) {
            List<TbCompute> primaryComputes = ComputeManager.findByInstanceIdAndRole(instanceId, Constaint.PRIMARY_ROLE);
            computes.add(primaryComputes.get(0));
        } else if (datastore.equalsIgnoreCase(Constaint.DATASTORE_MYSQL)) {
            List<TbCompute> masterComputes = ComputeManager.findByInstanceIdAndRole(instanceId, Constaint.MASTER_ROLE);
            computes.add(masterComputes.get(0));
        }

        for (TbCompute compute : computes) {
            List<TbAgent> agents = AgentManager.findByComputeId(compute.getId());
            for (TbAgent agent : agents) {
                Account account = AgentManager.getAccount(agent.getId());
                if (account == null) {
                    LOGGER.warn("DBaaS Agent is offline, cannot create backup");
                    return false;
                }

                LOGGER.info("Send message to " + agent.getId() + ", " + account.getClients().size());
                JSONObject dataJson = new JSONObject();
                dataJson.put("backupId", backupId);
                dataJson.put("url", url);

                ResponsePacket responsePacket = new ResponsePacket();
                responsePacket.setServiceId(AppServiceType.CREATE_BACKUP);
                responsePacket.setResult(1);
                responsePacket.setMessage("");
                responsePacket.setMessageId(System.currentTimeMillis() + "");
                responsePacket.setData(dataJson.toString());
                account.receivePacket(responsePacket);
            }
        }

        return true;
    }
}
