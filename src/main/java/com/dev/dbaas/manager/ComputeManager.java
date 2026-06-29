/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.manager;

import com.dev.dbaas.config.APIServiceType;
import com.dev.dbaas.config.ComputeStatus;
import com.dev.dbaas.config.Config;
import com.dev.dbaas.database.enties.TbAgent;
import com.dev.dbaas.database.enties.TbCompute;
import com.dev.dbaas.database.utils.ComputeUtil;
import com.dev.dbaas.pool.SessionPool;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.List;

/**
 *
 * @author hieutrinh
 */
public class ComputeManager {

    private static final Logger LOGGER = Logger.getLogger(ComputeManager.class);
    private static final SessionPool SESSION_POOL;
    private static final int QUEUE_SIZE = 4;
    private static final int POOL_SIZE = 4;
    private static final SecureRandom RAND = new SecureRandom();


    static {
        SESSION_POOL = new SessionPool.Builder().setPoolSize(POOL_SIZE).setQueueSize(QUEUE_SIZE).setPoolName("ComputeManager").build();
    }

    public static String add(TbCompute record) {

        long timestamp = System.currentTimeMillis();
        String id = null;
        int index = RAND.nextInt(POOL_SIZE);
        Session session = SESSION_POOL.getSession(index);
        try {
            id = ComputeUtil.getInstance().createAsString(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        LOGGER.info("add, processing "+(System.currentTimeMillis() - timestamp));
        return id;
    }

    public static void update(TbCompute record) {

        long timestamp = System.currentTimeMillis();
        int index = Math.abs(record.getId().hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            ComputeUtil.getInstance().update(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        LOGGER.info("update, processing "+(System.currentTimeMillis() - timestamp));
    }

    public static void delete(TbCompute record) {

        long timestamp = System.currentTimeMillis();
        int index = Math.abs(record.getId().hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            ComputeUtil.getInstance().delete(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        LOGGER.info("delete, processing "+(System.currentTimeMillis() - timestamp));
    }

    public static TbCompute findById(String id) {

        TbCompute record = null;
        int index = Math.abs(id.hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            record = ComputeUtil.getInstance().findOne(session, id);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        return record;
    }

    public static List<TbCompute> findByInstanceId(String instanceId) {

        List<TbCompute> records = null;
        int index = Math.abs(instanceId.hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            records = ComputeUtil.getInstance().findByInstanceId(session, instanceId);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        return records;
    }

    public static List<TbCompute> findByInstanceIdAndRole(String instanceId, String role) {

        List<TbCompute> records = null;
        int index = Math.abs(instanceId.hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            records = ComputeUtil.getInstance().findByInstanceIdAndRole(session, instanceId, role);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        return records;
    }

    public static void handlerDisconnectAgent(String agentId) {
        TbAgent agent = AgentManager.findById(agentId);
        if (agent == null) {
            return;
        }

        TbCompute compute = ComputeManager.findById(agent.getComputeId());
        if (compute == null) {
            return;
        }

        JSONObject computeInfos = new JSONObject();
        computeInfos.put("mode", "");
        computeInfos.put("compute_role", compute.getRole());

        JSONObject data = new JSONObject();
        data.put("serviceId", APIServiceType.UPDATE_STATUS_COMPUTE);

        JSONObject bodyData = new JSONObject();
        bodyData.put("computeId", agent.getComputeId());
        bodyData.put("status", ComputeStatus.DISCONNECTED.getName());
        bodyData.put("computeInfos", computeInfos);

        data.put("data", bodyData.toString());
        data.put("messageId", "");
        data.put("time", System.currentTimeMillis());
        ClusterManager.sendTopic(Config.rabbit_connection, Config.rabbit_port, Config.rabbit_username, Config.rabbit_password, Config.rabbit_api_exchange, Config.rabbit_api_queue, data);
    }
}
