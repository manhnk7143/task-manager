/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.manager;

import com.dev.dbaas.database.enties.TbNetwork;
import com.dev.dbaas.database.utils.NetworkUtil;
import com.dev.dbaas.pool.SessionPool;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.security.SecureRandom;
import java.util.List;

/**
 *
 * @author hieutrinh
 */
public class NetworkManager {

    private static final Logger LOGGER = Logger.getLogger(NetworkManager.class);
    private static final SessionPool SESSION_POOL;
    private static final int QUEUE_SIZE = 4;
    private static final int POOL_SIZE = 4;
    private static final SecureRandom RAND = new SecureRandom();


    static {
        SESSION_POOL = new SessionPool.Builder().setPoolSize(POOL_SIZE).setQueueSize(QUEUE_SIZE).setPoolName("NetworkManager").build();
    }

    public static String add(TbNetwork record) {

        long timestamp = System.currentTimeMillis();
        String id = null;
        int index = RAND.nextInt(POOL_SIZE);
        Session session = SESSION_POOL.getSession(index);
        try {
            id = NetworkUtil.getInstance().createAsString(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        LOGGER.info("add, processing "+(System.currentTimeMillis() - timestamp));
        return id;
    }

    public static void update(TbNetwork record) {

        long timestamp = System.currentTimeMillis();
        int index = Math.abs(record.getId().hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            NetworkUtil.getInstance().update(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        LOGGER.info("update, processing "+(System.currentTimeMillis() - timestamp));
    }

    public static void delete(TbNetwork record) {

        long timestamp = System.currentTimeMillis();
        int index = Math.abs(record.getId().hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            NetworkUtil.getInstance().delete(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        LOGGER.info("delete, processing "+(System.currentTimeMillis() - timestamp));
    }

    public static TbNetwork findById(String networkId) {

        TbNetwork record = null;
        int index = RAND.nextInt(POOL_SIZE);
        Session session = SESSION_POOL.getSession(index);
        try {
            record = NetworkUtil.getInstance().findOne(session, networkId);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        return record;
    }

    public static List<TbNetwork> findByComputeId(String computeId) {

        List<TbNetwork> records = null;
        int index = RAND.nextInt(POOL_SIZE);
        Session session = SESSION_POOL.getSession(index);
        try {
            records = NetworkUtil.getInstance().findByComputeId(session, computeId);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        return records;
    }

    public static TbNetwork findByComputeIdAndMode(String computeId, String mode) {

        TbNetwork record = null;
        int index = RAND.nextInt(POOL_SIZE);
        Session session = SESSION_POOL.getSession(index);
        try {
            record = NetworkUtil.getInstance().findByComputeIdAndMode(session, computeId, mode);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        return record;
    }
}
