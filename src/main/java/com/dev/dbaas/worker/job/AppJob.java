package com.dev.dbaas.worker.job;

import com.dev.dbaas.common.JobBase;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.protocol.AppPacket;
import com.corundumstudio.socketio.SocketIOClient;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;


public class AppJob extends JobBase{

    private final static Logger LOGGER = Logger.getLogger("AppJob");

    @Getter
    @Setter
    private Account account;
    @Getter
    @Setter
    private SocketIOClient client;
    @Getter
    @Setter
    private AppPacket packet;
    @Getter
    @Setter
    private Integer accountId;

    public AppJob() {
        super();
    }

    public void show() {
        LOGGER.warn("[show] account: " + account.getAgentId() + ", service: " + packet.getServiceId() + ", data: " + packet.getData());
    }
}
