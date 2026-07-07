package com.ctel.dbaas.socket;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.socket.handler.AuthRequestHandler;
import com.ctel.dbaas.socket.handler.ClientRequestHandler;
import com.ctel.dbaas.socket.handler.DisconnectHandler;
import com.ctel.dbaas.socket.model.AuthClientRequest;
import com.ctel.dbaas.socket.model.ClientRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
public class SocketIOConfig {

    @Autowired
    private ClientRequestHandler clientRequestHandler;

    @Autowired
    private AuthRequestHandler authRequestHandler;

    @Autowired
    private DisconnectHandler disconnectHandler;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(EnvConfig.SOCKET_SERVER_HOST);
        config.setPort(EnvConfig.SOCKET_SERVER_PORT);

        SocketIOServer socketIOServer = new SocketIOServer(config);
        socketIOServer.addConnectListener(onConnected());
        socketIOServer.addDisconnectListener(disconnectHandler);
        socketIOServer.addEventListener("authen", AuthClientRequest.class, authRequestHandler);
        socketIOServer.addEventListener("chatevent", ClientRequest.class, clientRequestHandler);
        socketIOServer.start();

        return socketIOServer;
    }

    private ConnectListener onConnected() {
        return (client) -> log.info("Socket ID[{}]  Connected to socket", client.getSessionId().toString());
    }

}
