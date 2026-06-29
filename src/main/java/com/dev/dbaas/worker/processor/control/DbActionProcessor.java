package com.dev.dbaas.worker.processor.control;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.config.ComputeStatus;
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

public class DbActionProcessor implements ProcessorBase<ControlJob> {

    private static final Logger LOGGER = Logger.getLogger(DbActionProcessor.class);

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

        LOGGER.info("[4CMS] DbActionProcessor " + job.getServiceId() + " - " + job.getData());
        job.decodePacket();
        JSONObject input = job.getJsonData();
        String instanceId = input.optString("instanceId");
        String datastore = input.optString("datastore");
        String command = input.optString("command");

        TbInstance instance = InstanceManager.findById(instanceId);
        if (instance == null) {
            LOGGER.warn("Not found Instance by instanceId = " + instanceId);
            return false;
        }
        List<TbCompute> computes = ComputeManager.findByInstanceId(instanceId);

        if (!computes.isEmpty()) {
            List<TbCompute> listComputeSendRequest = new ArrayList<>();
            if (Constaint.DATASTORE_KAFKA.equals(datastore)) {
                if (List.of("edit_cluster_config", "add_user_sasl", "remove_user_sasl").contains(command)) {
                    listComputeSendRequest.addAll(computes);
                } else {
                    computes.stream().filter(c -> ComputeStatus.RUNNING.name().equalsIgnoreCase(c.getStatus()))
                            .findFirst().ifPresent(listComputeSendRequest::add);
                }
            } else if (Constaint.DATASTORE_MONGO_DB.equals(datastore)) {
                computes.stream().filter(c -> Constaint.PRIMARY_ROLE.equalsIgnoreCase(c.getRole()))
                        .findFirst().ifPresent(listComputeSendRequest::add);
            } else if (Constaint.DATASTORE_MYSQL.equals(datastore)) {
                computes.stream().filter(c -> Constaint.MASTER_ROLE.equalsIgnoreCase(c.getRole()))
                        .findFirst().ifPresent(listComputeSendRequest::add);
            } else {
                listComputeSendRequest.addAll(computes);
            }

            for (TbCompute compute : listComputeSendRequest) {
                List<TbAgent> agents = AgentManager.findByComputeId(compute.getId());
                for (TbAgent agent : agents) {
                    Account account = AgentManager.getAccount(agent.getId());
                    if (account == null) {
                        LOGGER.warn("DBaaS Agent is offline, cannot create backup");
                        return false;
                    }
                    LOGGER.info("Send message to agentId " + agent.getId() + ", " + account.getClients().size());
                    ResponsePacket responsePacket = new ResponsePacket();
                    responsePacket.setServiceId(AppServiceType.REQUEST_DB_ACTION);
                    responsePacket.setResult(1);
                    responsePacket.setMessage("");
                    responsePacket.setMessageId(System.currentTimeMillis() + "");
                    responsePacket.setData(job.getJsonData().toString());
                    account.receivePacket(responsePacket);
                }
            }
        }
        return true;
    }
}
