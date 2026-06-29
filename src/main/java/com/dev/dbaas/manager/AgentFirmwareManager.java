/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.manager;

import com.dev.dbaas.database.enties.TbAgentFirmware;
import com.dev.dbaas.database.enties.TbAgentHeartbeat;
import com.dev.dbaas.database.utils.AgentFirmwareUtil;
import com.dev.dbaas.database.utils.AgentHeartbeatUtil;
import com.dev.dbaas.pool.SessionPool;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.security.SecureRandom;

/**
 *
 * @author hieutrinh
 */
public class AgentFirmwareManager {

    private static final Logger LOGGER = Logger.getLogger(AgentFirmwareManager.class);
    private static final SessionPool SESSION_POOL;
    private static final int QUEUE_SIZE = 4;
    private static final int POOL_SIZE = 4;
    private static final SecureRandom RAND = new SecureRandom();


    static {
        SESSION_POOL = new SessionPool.Builder().setPoolSize(POOL_SIZE).setQueueSize(QUEUE_SIZE).setPoolName("AgentFirmwareManager").build();
    }

    public static String add(TbAgentFirmware record) {

        long timestamp = System.currentTimeMillis();
        String id = null;
        int index = RAND.nextInt(POOL_SIZE);
        Session session = SESSION_POOL.getSession(index);
        try {
            id = AgentFirmwareUtil.getInstance().createAsString(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        LOGGER.info("add, processing "+(System.currentTimeMillis() - timestamp));
        return id;
    }

    public static void update(TbAgentFirmware record) {

        long timestamp = System.currentTimeMillis();
        int index = Math.abs(record.getId().hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            AgentFirmwareUtil.getInstance().update(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        LOGGER.info("update, processing "+(System.currentTimeMillis() - timestamp));
    }

    public static void delete(TbAgentFirmware record) {

        long timestamp = System.currentTimeMillis();
        int index = Math.abs(record.getId().hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            AgentFirmwareUtil.getInstance().delete(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        LOGGER.info("delete, processing "+(System.currentTimeMillis() - timestamp));
    }

    public static TbAgentFirmware findById(String id) {

        TbAgentFirmware record = null;
        int index = Math.abs(id.hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            record = AgentFirmwareUtil.getInstance().findOne(session, id);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        return record;
    }

    public static TbAgentHeartbeat findByAgentId(String agentId) {

        TbAgentHeartbeat record = null;
        int index = Math.abs(agentId.hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            record = AgentHeartbeatUtil.getInstance().getByAgentId(session, agentId);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        return record;
    }
}
