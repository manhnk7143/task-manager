/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.manager;

import com.corundumstudio.socketio.SocketIOClient;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hieutrinh
 */
public class ChannelSocketIoAttachmentManager {

    private static final Logger LOGGER = Logger.getLogger(ChannelSocketIoAttachmentManager.class);

    private static final ConcurrentHashMap<String, SocketIOClient> MAP_APP_ATTACHMENTS = new ConcurrentHashMap<>();

    public static SocketIOClient getAppChannelAttachment(String channelId) {

        if (channelId == null) {
            return null;
        }
        return MAP_APP_ATTACHMENTS.get(channelId);
    }

    public static void addAppChannelAttachment(String channelId, SocketIOClient client) {
        MAP_APP_ATTACHMENTS.put(channelId, client);
    }

    public static void removeAppChannelAttachment(String channelId) {
        MAP_APP_ATTACHMENTS.remove(channelId);
    }

    public static Collection<SocketIOClient> getAllAppChannels() {
        return MAP_APP_ATTACHMENTS.values();
    }
}
