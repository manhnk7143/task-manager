package com.ctel.dbaas.socket.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.ctel.dbaas.repository.dbaas.SocketInfoRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class DisconnectHandler implements DisconnectListener {

    @Autowired
    private SocketInfoRepository socketInfoRepository;

    @Override
    public void onDisconnect(SocketIOClient client) {
        log.info("Client[{}] - Disconnected from socket", client.getSessionId().toString());
        socketInfoRepository.deleteBySocketClientId(client.getSessionId().toString());
    }
}
