/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.manager;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dev.dbaas.database.enties.TbAgent;
import com.dev.dbaas.database.utils.AgentUtil;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.pool.SessionPool;
import com.corundumstudio.socketio.SocketIOClient;
import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 *
 * @author hieutrinh
 */
public class AgentManager {

    private static final Logger LOGGER = Logger.getLogger(AgentManager.class);
    private static final ConcurrentMap<String, Account> MAP_AGENT_ACCOUNTS = new ConcurrentHashMap<>();
    private static final SessionPool SESSION_POOL;
    private static final int QUEUE_SIZE = 4;
    private static final int POOL_SIZE = 4;
    private static final SecureRandom RAND = new SecureRandom();


    static {
        SESSION_POOL = new SessionPool.Builder().setPoolSize(POOL_SIZE).setQueueSize(QUEUE_SIZE).setPoolName("AgentManager").build();
    }

    public static String add(TbAgent record) {

        long timestamp = System.currentTimeMillis();
        String id = null;
        int index = RAND.nextInt(POOL_SIZE);
        Session session = SESSION_POOL.getSession(index);
        try {
            id = AgentUtil.getInstance().createAsString(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
//        LOGGER.info("add, processing "+(System.currentTimeMillis() - timestamp));
        return id;
    }

    public static void update(TbAgent record) {

        long timestamp = System.currentTimeMillis();
        int index = Math.abs(record.getId().hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            AgentUtil.getInstance().update(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
//        LOGGER.info("update, processing "+(System.currentTimeMillis() - timestamp));
    }

    public static void delete(TbAgent record) {

        long timestamp = System.currentTimeMillis();
        int index = Math.abs(record.getId().hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            AgentUtil.getInstance().delete(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        LOGGER.info("delete, processing "+(System.currentTimeMillis() - timestamp));
    }

    public static TbAgent findById(String id) {

        TbAgent account = null;
        int index = Math.abs(id.hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            account = AgentUtil.getInstance().findOne(session, id);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        return account;
    }

    public static List<TbAgent> findByInstanceId(String instanceId) {

        List<TbAgent> agents = null;
        int index = Math.abs(instanceId.hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            agents = AgentUtil.getInstance().findByInstanceId(session, instanceId);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        return agents;
    }

    public static List<TbAgent> findByComputeId(String computeId) {

        List<TbAgent> agents = null;
        int index = Math.abs(computeId.hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            agents = AgentUtil.getInstance().findByComputeId(session, computeId);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        return agents;
    }

    public static TbAgent findFirstByComputeIdAndInstanceId(String instanceId, String computeId){

        // get agent
        return null;
    }

    public static Account getAccount(String agentId) {

        if (agentId == null) {
            return null;
        }
        Account account = MAP_AGENT_ACCOUNTS.get(agentId);
        if (account == null) {
            TbAgent accountInfo = findById(agentId);
            if (accountInfo != null) {
                LOGGER.info("Cache not hit account with agentId " + agentId);
                account = new Account();
                account.setAgentVersion(accountInfo.getAgentVersion());
                account.setComputeId(accountInfo.getComputeId());
                account.setEncryptedKey(accountInfo.getEncryptedKey());
                account.setOrgId(accountInfo.getOrgId());
                account.setProjectId(accountInfo.getProjectId());
                account.setInstanceId(accountInfo.getInstanceId());
                account.setAgentFirmwareId(accountInfo.getAgentFirmwareId());
                account.setAgentId(accountInfo.getId());
                MAP_AGENT_ACCOUNTS.putIfAbsent(agentId, account);
            }
        }
        return account;
    }

    public static void removeAccountClient(String accountId, SocketIOClient socketClient) {

        Account account = getAccount(accountId);
        int index = account.getClients().indexOf(socketClient);
        if (index >= 0) {
            LOGGER.warn("");
            account.getClients().remove(index);
        }
        if (account.getClients().isEmpty()) {
            LOGGER.info("Remove account " + accountId);
            MAP_AGENT_ACCOUNTS.remove(accountId);
        }
    }

    public static Account addAccount(Account account) {
        MAP_AGENT_ACCOUNTS.putIfAbsent(account.getAgentId(), account);
        LOGGER.info("Add account " + account.getAgentId());
        return account;
    }

    public static String generateConfigurationAgent(String instanceId, String computeId, String datastoreCode, String datastoreVersion,
                                                    String encryptedKey, String agentId, String socketUrl, String dockerRegistry,
                                                    String backupUrl, String backupMode, String ipAddress, String monitorResourceType) {
        Map<String, String> vars = new HashMap<>();
        vars.put("instance_id", instanceId);
        vars.put("compute_id", computeId);
        vars.put("datastore_manager", datastoreCode);
        vars.put("datastore_version", datastoreVersion);
        vars.put("encrypted_key", encryptedKey);
        vars.put("agent_id", agentId);
        vars.put("socket_url", socketUrl);
        vars.put("docker_registry", dockerRegistry);
        vars.put("backup_url", backupUrl);
        vars.put("backup_mode", backupMode);
        vars.put("monitor_resource_type", monitorResourceType);
        vars.put("ip_address", ipAddress);

        return convertToSection(vars, "default");
    }

    private static String convertToSection(Map<String, String> hashMap, String sectionName) {
        StringBuilder sectionBuilder = new StringBuilder();
        sectionBuilder.append("[").append(sectionName).append("]\n");
        for (String key : hashMap.keySet()) {
            String value = hashMap.get(key);
            if (value == null) {
                value = "";
            }
            sectionBuilder.append(key).append("=").append(value).append("\n");
        }

        return sectionBuilder.toString();
    }
}
