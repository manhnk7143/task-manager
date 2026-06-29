package com.dev.dbaas.handler;

import com.dev.dbaas.manager.*;
import com.dev.dbaas.manager.*;
import com.dev.dbaas.packet.AppServiceType;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.protocol.AppPacket;
import com.dev.dbaas.protocol.ResponsePacket;
import com.dev.dbaas.worker.job.AppJob;
import com.corundumstudio.socketio.SocketIOClient;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppSocketIoServerHandler {

    private static final Logger LOGGER = Logger.getLogger(AppSocketIoServerHandler.class);

    private static final List<String> IGNORE_SERVICES = Arrays.asList(
            AppServiceType.AUTHENTICATION.getValue());

    private static final Map<String, Boolean> AUTH_SERVICE_WORKER = new HashMap<>();
    static {
        AUTH_SERVICE_WORKER.put(AppServiceType.AUTHENTICATION.getValue(), Boolean.TRUE);
    }

    public static void handleDisconnect(SocketIOClient client, boolean isDestroy) {

        ChannelSocketIoAttachmentManager.removeAppChannelAttachment(client.getSessionId().toString());
        String agentId = SessionManager.getAccountIdBySessionId(client.getSessionId());
        LOGGER.warn("ON DISCONNECT clientId: " + client.getSessionId() + " - agentId: " + agentId);
        if (agentId != null) {
            LOGGER.warn("Client disconnection, "+agentId);
            // remove session
            SessionManager.removeAccountIdBySessionId(client.getSessionId());
            AgentManager.removeAccountClient(agentId, client);
            ComputeManager.handlerDisconnectAgent(agentId);
        }
    }

    public static void handlePacket(SocketIOClient client, AppPacket packet) {
//        LOGGER.info("Receive packet " + client.getSessionId() + " - " + packet.getServiceId()+" - "+packet.getMessageId());
        AppJob userJob = new AppJob();
        boolean isPass = true;

        SocketIOClient chanelClient = ChannelSocketIoAttachmentManager.getAppChannelAttachment(client.getSessionId().toString());
        if (chanelClient == null) {
//            LOGGER.info("Channel client is null");
            ChannelSocketIoAttachmentManager.addAppChannelAttachment(client.getSessionId().toString(), client);
        }
        String agentId = SessionManager.getAccountIdBySessionId(client.getSessionId());
//        LOGGER.info("AccountId = "+agentId);
        if (agentId == null) {
            if (!IGNORE_SERVICES.contains(packet.getServiceId())) {
//                LOGGER.info("Require login, " + packet.getServiceId());
                packet.setServiceId(AppServiceType.REQUIRE_LOGIN.getValue());
            }
            isPass = false;
        }
        if (isPass) {
            Account account = AgentManager.getAccount(agentId);
//            LOGGER.info("Clients = "+account.getClients().size());
            userJob.setAccount(account);
        }
        userJob.setId(Math.abs(client.getSessionId().hashCode()));
        if (userJob.getAccount() == null) {
            userJob.setClient(client);
            userJob.setPacket(packet);
            if (AUTH_SERVICE_WORKER.get(packet.getServiceId()) != null) {
                WorkerManager.putAuthAppJob(userJob);
            } else {
                if(userJob.getAccount() != null){
                    WorkerManager.putAppJob(userJob);
                }
            }
        } else {
            userJob.setClient(client);
            userJob.setPacket(packet);
            userJob.setEventTimestamp(System.currentTimeMillis());
            if (AUTH_SERVICE_WORKER.get(packet.getServiceId()) != null) {
                WorkerManager.putAuthAppJob(userJob);
            }
            else {
                if(userJob.getAccount() != null){
                    WorkerManager.putAppJob(userJob);
                }
            }
        }
    }

    public static void requireLogin(SocketIOClient client) {

        String content = "Your session expired, result : " + 1;
        ResponsePacket responsePacket = new ResponsePacket();
        responsePacket.setServiceId(AppServiceType.REQUIRE_LOGIN);
        responsePacket.setResult(1);
        responsePacket.setMessage(content);
        responsePacket.setData("{}");
        responsePacket.setMessageId(System.currentTimeMillis()+"");
        responsePacket.setTime(System.currentTimeMillis());
        Account.receivePacketWithChannelDetail(client, responsePacket);
    }
}
