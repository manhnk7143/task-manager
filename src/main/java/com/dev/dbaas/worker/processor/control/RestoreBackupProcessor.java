package com.dev.dbaas.worker.processor.control;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbAgent;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.manager.AgentManager;
import com.dev.dbaas.manager.ComputeManager;
import com.dev.dbaas.packet.AppServiceType;
import com.dev.dbaas.protocol.ResponsePacket;
import com.dev.dbaas.worker.job.ControlJob;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RestoreBackupProcessor implements ProcessorBase<ControlJob> {

    private static final Logger LOGGER = Logger.getLogger(RestoreBackupProcessor.class);

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
        String instanceId = input.optString("instanceId");
        String datastore = input.optString("datastore");
        String backupMode = input.optString("backupMode");
        String url = input.optString("url");

        // new
        List<TbCompute> listComputeToRestore = new ArrayList<>();
        switch (datastore) {
            case Constaint.DATASTORE_REDIS: {
                List<TbCompute> listComputeMaster = ComputeManager.findByInstanceIdAndRole(instanceId, Constaint.MASTER_ROLE);
                if (listComputeMaster != null && !listComputeMaster.isEmpty()) {
                    listComputeToRestore.add(listComputeMaster.get(0));
                }
                break;
            }
            case Constaint.DATASTORE_MONGO_DB: {
                List<TbCompute> listComputePrimary = ComputeManager.findByInstanceIdAndRole(instanceId, Constaint.PRIMARY_ROLE);
                if (listComputePrimary != null && !listComputePrimary.isEmpty()) {
                    listComputeToRestore.add(listComputePrimary.get(0));
                }
                break;
            }
            default: {
                throw new Exception("Datastore " + datastore + " not supported");
            }
        }

        for (TbCompute compute : listComputeToRestore) {
            List<TbAgent> agents = AgentManager.findByComputeId(compute.getId());
            if (agents == null || agents.isEmpty()) {
                LOGGER.warn("Not found agents by instanceId = " + instanceId);
                return false;
            }

            for (TbAgent agent : agents) {
                Account account = AgentManager.getAccount(agent.getId());
                if (account != null) {
                    LOGGER.info("Send message to agentId " + agent.getId() + ", " + account.getClients().size());
                    JSONObject dataJson = new JSONObject();
                    dataJson.put("agentId", agent.getId());
                    dataJson.put("url", url);
                    dataJson.put("backupMode", backupMode);

                    ResponsePacket responsePacket = new ResponsePacket();
                    responsePacket.setServiceId(AppServiceType.RESTORE_BACKUP);
                    responsePacket.setResult(1);
                    responsePacket.setMessage("");
                    responsePacket.setMessageId(System.currentTimeMillis() + "");
                    responsePacket.setData(dataJson.toString());
                    account.receivePacket(responsePacket);
                }
            }
        }

        return true;

//        List<TbAgent> agents = AgentManager.findByInstanceId(instanceId);
//        if (agents == null || agents.isEmpty()) {
//            LOGGER.warn("Not found agents by instanceId = " + instanceId);
//            return false;
//        }
//
//        for (TbAgent agent : agents) {
//            Account account = AgentManager.getAccount(agent.getId());
//            if (account != null) {
//                LOGGER.info("Send message to agentId " + agent.getId() + ", " + account.getClients().size());
//                JSONObject dataJson = new JSONObject();
//                dataJson.put("agentId", agent.getId());
//                dataJson.put("url", url);
//
//                ResponsePacket responsePacket = new ResponsePacket();
//                responsePacket.setServiceId(AppServiceType.RESTORE_BACKUP);
//                responsePacket.setResult(1);
//                responsePacket.setMessage("");
//                responsePacket.setMessageId(System.currentTimeMillis() + "");
//                responsePacket.setData(dataJson.toString());
//                account.receivePacket(responsePacket);
//            }
//        }
//
//        return true;
    }
}
