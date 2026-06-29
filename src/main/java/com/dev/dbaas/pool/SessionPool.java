/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.pool;

import com.dev.dbaas.manager.DbConnectionManager;
import com.dev.dbaas.utils.sessions.DbConnection;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author hieutrinh
 */
public class SessionPool {
    private static final Logger LOGGER = Logger.getLogger(SessionPool.class);
    private ConcurrentMap<Integer, BlockingQueue<Session>> MAP_QUEUE = new ConcurrentHashMap<>();
    private ConcurrentMap<Integer, Integer> MAP_COUNTER = new ConcurrentHashMap<>();
    private String poolName;
    private String namespace;
    private boolean isReadOnly = false;
    private int queueSize = 4;
    private int poolSize = 4;

    public SessionPool(int poolSize, int queueSize, String poolName, String namespace, boolean isReadOnly) {

        this.poolSize = poolSize;
        this.queueSize = queueSize;
        this.poolName = poolName;
        this.namespace = namespace;
        this.isReadOnly = isReadOnly;
    }

    private Session createSession() {

//        LOGGER.warn(namespace + " create new session, " + poolName);
        DbConnection connection = DbConnectionManager.getSessionMaster();
        if (connection == null || connection.getSessionFactory() == null) {
            LOGGER.warn(namespace + ", session is null, cannot create session");
            return null;
        }
        return connection.getSessionFactory().openSession();
    }

    public void releaseSession(Integer index, Session session) {

        //LOGGER.info("Release session, "+poolName);
        session.clear();
        MAP_QUEUE.get(index).add(session);
    }

    public Session getSession(Integer index) {

        if (index >= poolSize) {
            LOGGER.info("Over poolsize , " + poolSize);
            return null;
        }
        BlockingQueue<Session> queue = MAP_QUEUE.get(index);
        Integer counter = MAP_COUNTER.get(index);
        if (queue == null) {
            queue = new LinkedBlockingQueue<>();
            MAP_QUEUE.put(index, queue);
        }
        if (counter == null) {
            counter = new Integer(0);
        }
        if (counter < queueSize) {
            Session newSession = createSession();
            if (newSession == null) {
                return null;
            }
            queue.add(newSession);
            counter = counter + 1;
            MAP_COUNTER.put(index, counter);
        }
        try {
            //LOGGER.info("Request a session, "+poolName +", index = "+index+", isEmpty = "+queue.isEmpty());
            Session session = queue.take();
            Transaction transaction = session.getTransaction();

            if (!session.isConnected() || !session.isOpen() || (transaction != null && transaction.getStatus() == TransactionStatus.FAILED_COMMIT)) {
                session.close();
                session = createSession();
            }
            //LOGGER.info("Take session is successful,"+poolName+", isEmpty = "+queue.isEmpty());
            return session;
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
        return null;
    }

    public void clearSessions() {

        LOGGER.info("Clear all session");
        MAP_QUEUE.forEach((id, queue) -> {
            queue.forEach((session) -> {
                session.clear();
                session.close();
            });
        });
    }

    public static class Builder {

        private int mQueueSize;
        private int mPoolSize;
        private String mPoolName;
        private String mNamespace;
        private boolean mIsReadOnly;

        public Builder setQueueSize(int queueSize) {
            mQueueSize = queueSize;
            return this;
        }

        public Builder setPoolSize(int queueSize) {
            mPoolSize = queueSize;
            return this;
        }

        public Builder setPoolName(String poolName) {
            mPoolName = poolName;
            return this;
        }

        public Builder setNamespace(String namespace) {
            mNamespace = namespace;
            return this;
        }

        public Builder setIsReadOnly(boolean isReadOnly) {
            mIsReadOnly = isReadOnly;
            return this;
        }

        public SessionPool build() {
            LOGGER.info("[BUILDER] " + mQueueSize + ", " + mPoolSize + ", " + mPoolName);
            SessionPool pool = new SessionPool(mPoolSize, mQueueSize, mPoolName, mNamespace, mIsReadOnly);
            return pool;
        }
    }
}

