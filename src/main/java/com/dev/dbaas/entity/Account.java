package com.dev.dbaas.entity;

import com.dev.dbaas.protocol.ResponsePacket;
import com.corundumstudio.socketio.SocketIOClient;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.CRC32;


public class Account {

    private static final Logger LOGGER = Logger.getLogger(Account.class);

    @Getter
    @Setter
    private String agentId;
    @Getter
    @Setter
    private String agentVersion;
    @Getter @Setter
    private String computeId;
    @Getter @Setter
    private String encryptedKey;
    @Getter @Setter
    private String instanceId;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String orgId;
    @Getter @Setter
    private String projectId;
    @Getter @Setter
    private String agentFirmwareId;
    @Getter
    @Setter
    private boolean isEmpty;
    @Getter
    @Setter
    private CopyOnWriteArrayList<SocketIOClient> clients = new CopyOnWriteArrayList<>();
    @Getter
    @Setter
    private ConcurrentMap<String, String> languageSessions = new ConcurrentHashMap<>();
    @Getter
    @Setter
    public RateLimiter rateLimiter = RateLimiter.create(5);

    public Account() {
        isEmpty = true;
    }

    public static boolean receivePacketWithChannelDetail(SocketIOClient client, ResponsePacket packet) {

        boolean result = false;
        if (client.isChannelOpen()) {
            client.sendEvent("chatevent", packet.toString());
            result = true;
        } else {
            LOGGER.error("Channel client is closed");
        }
        return result;
    }

    public static boolean receivePacketWithChannel(SocketIOClient client, ResponsePacket packet) {

        boolean result = false;
        if (client.isChannelOpen()) {
            client.sendEvent("chatevent", packet.toString());
            result = true;
        } else {
            LOGGER.error("Channel client is closed");
        }
        return result;
    }

    public static Account toUser(String data) {

        Account userInfo = new Account();
        return userInfo;
    }

    public String getLanguageCode(SocketIOClient client) {

        String languageCode = languageSessions.get(client.getSessionId().toString());
        if (languageCode == null) {
            languageCode = "en";
        }
        return languageCode;
    }

    public void setLanguageCode(SocketIOClient client, String languageCode) {
        //LOGGER.info("Set language : "+languageCode);
        languageSessions.put(client.getSessionId().toString(), languageCode);
    }

    public boolean receivePacket(ResponsePacket jsondata) {

        boolean result = false;
//        LOGGER.info("Send to "+clients.size()+", agentId = "+agentId+", "+jsondata.toString());
        for (int i = 0; i < clients.size(); i++) {
            SocketIOClient client = clients.get(i);
            if (client.isChannelOpen()) {

                client.sendEvent("chatevent", jsondata.toString());
                result = true;
            } else {
                LOGGER.info("Channel client is closed, " + client.getSessionId().toString());
            }
        }
        return result;
    }

    public boolean receivePacket(ResponsePacket jsondata, String channelUuid) {

        boolean result = false;
        for (int i = 0; i < clients.size(); i++) {

            SocketIOClient client = clients.get(i);
            if (client.isChannelOpen()) {
                if (client.getSessionId().toString().equalsIgnoreCase(channelUuid)) {
                    client.sendEvent("chatevent", jsondata.toString());
                    result = true;
                } else {
                    LOGGER.info("Not found channelUuid, " + channelUuid);
                }
            } else {
                LOGGER.info("Channel client is closed, " + client.getSessionId().toString());
            }
        }
        return result;
    }

    public boolean isExisted(SocketIOClient client) {

        for (SocketIOClient item : clients) {
            if (item.getSessionId().toString().equalsIgnoreCase(client.getSessionId().toString())) {
                return true;
            }
        }
        return false;
    }

    public boolean removeIfExisted(SocketIOClient client) {

        SocketIOClient socketInstance = null;

        for (SocketIOClient item : clients) {
            if (item.getSessionId().toString().equalsIgnoreCase(client.getSessionId().toString())) {
                socketInstance = item;
                break;
            }
        }
        if (socketInstance != null) {
            clients.remove(socketInstance);
        }
        return false;
    }

    public boolean tryAcquire(){
        //return true;
        return rateLimiter.tryAcquire();
    }
}
