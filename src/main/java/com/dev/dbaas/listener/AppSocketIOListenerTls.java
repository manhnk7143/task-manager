package com.dev.dbaas.listener;

import com.dev.dbaas.handler.AppSocketIoServerHandler;
import com.dev.dbaas.protocol.AppPacket;
import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import org.apache.log4j.Logger;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class AppSocketIOListenerTls implements Runnable {

    public static final Logger LOGGER = Logger.getLogger(AppSocketIOListenerTls.class);
    private SocketIOServer socketIoServer;
    private int port;
    private String cerFile;
    private String cerPass;

    public AppSocketIOListenerTls(int port) {
        this.port = port;
    }

    public void start(String fileCer, String passCer) throws FileNotFoundException, InterruptedException {

        // config connection
        Configuration config = new Configuration();

        LOGGER.info("Certificate file " + fileCer + "; " + passCer);
        if (fileCer != null && !fileCer.isEmpty()) {
            config.setKeyStorePassword(passCer);
            config.setKeyStore(new FileInputStream(fileCer));
            port += 1;
        }
        config.setPort(port);
        config.setHostname("0.0.0.0");
        config.setMaxFramePayloadLength(1024 * 1024 * 50);
        config.setMaxHttpContentLength(1024 * 1024 * 50);
        //config.setOrigin("https://cloud-camera");

        // config for socket
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        socketConfig.setTcpNoDelay(false);
        socketConfig.setTcpKeepAlive(true);
        socketConfig.setTcpSendBufferSize(128 * 1024 * 1000);

        config.setSocketConfig(socketConfig);

        socketIoServer = new SocketIOServer(config);
        socketIoServer.addListeners(new AppSocketIoServerHandler());
        socketIoServer.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                LOGGER.info("onDisconnection - ");
                AppSocketIoServerHandler.handleDisconnect(client, false);
            }
        });
        socketIoServer.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient socketIOClient) {
                LOGGER.info("onConnected - ");
            }
        });
        socketIoServer.addEventListener("chatevent", AppPacket.class, new DataListener<AppPacket>() {
            @Override
            public void onData(SocketIOClient client, AppPacket packet, AckRequest ackRequest) throws Exception {

//                LOGGER.info("onData - " + packet.getServiceId() + " - " + packet.getMessageId() + " - " + packet.getTime() + " - " + client.getSessionId());
                AppSocketIoServerHandler.handlePacket(client, packet);
            }
        });

        LOGGER.info("Server socketios listen on port " + port);
        socketIoServer.startAsync();
        Thread.sleep(Integer.MAX_VALUE);
        socketIoServer.stop();
    }

    public void stop() {

        if (socketIoServer != null) {
            socketIoServer.stop();
        }
    }

    @Override
    public void run() {
        try {
            start(cerFile, cerPass);
        } catch (FileNotFoundException | InterruptedException ex) {
            LOGGER.error(ex, ex);
        }
    }

    public String getCerFile() {
        return cerFile;
    }

    public void setCerFile(String cerFile) {
        this.cerFile = cerFile;
    }

    public String getCerPass() {
        return cerPass;
    }

    public void setCerPass(String cerPass) {
        this.cerPass = cerPass;
    }
}
