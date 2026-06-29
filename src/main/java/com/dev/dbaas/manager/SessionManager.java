package com.dev.dbaas.manager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SessionManager {

    private static final ConcurrentMap<UUID, String> MAP_SESSION_ACCOUNT = new ConcurrentHashMap<UUID, String>();

    public static void addSession(UUID sessionId, String agentId) {
        MAP_SESSION_ACCOUNT.put(sessionId, agentId);
    }

    public static String getAccountIdBySessionId(UUID sessionId) {
        return MAP_SESSION_ACCOUNT.get(sessionId);
    }

    public static void removeAccountIdBySessionId(UUID sessionId) {
        MAP_SESSION_ACCOUNT.remove(sessionId);
    }
}
