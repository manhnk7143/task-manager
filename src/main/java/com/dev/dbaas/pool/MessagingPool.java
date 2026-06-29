/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.pool;

import com.dev.dbaas.messaging.EngineType;
import com.dev.dbaas.messaging.IMessageConnection;
import com.dev.dbaas.messaging.RabbitConnection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author hieutrinh
 */
public class MessagingPool {

    private static final Logger LOGGER = Logger.getLogger(MessagingPool.class);
    private ConcurrentMap<Integer, BlockingQueue<IMessageConnection>> MAP_QUEUE = new ConcurrentHashMap<>();
    private ConcurrentMap<Integer, Integer> MAP_COUNTER = new ConcurrentHashMap<>();
    private EngineType engineType;
    private int queueSize = 2;
    private int poolSize = 2;
    private ConnectionFactory factory;

    public MessagingPool(int poolSize, int queueSize, EngineType engineType) {

        this.poolSize = poolSize;
        this.queueSize = queueSize;
        this.engineType = engineType;
    }


    public MessagingPool(int poolSize, int queueSize, EngineType engineType, ConnectionFactory factory) {

        this.poolSize = poolSize;
        this.queueSize = queueSize;
        this.engineType = engineType;
        this.factory = factory;
    }

    private IMessageConnection createSession() {

//        LOGGER.info("Create session ");
        IMessageConnection client = null;
        try {
            if (engineType == EngineType.RABBIT_MQ) {
                client = new RabbitConnection(this.factory);
                client.setConnected(true);
            }
//            LOGGER.info("Create session is successful");
            return client;
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
        return null;
    }

    public void releaseSession(Integer index, IMessageConnection session) {

//        LOGGER.info("Release session");
        try {
            MAP_QUEUE.get(index).put(session);
        } catch (InterruptedException e) {
            LOGGER.error(e, e);
        }
    }

    public IMessageConnection getSession(Integer index) {

//        LOGGER.info("Request a session, index = " + index + ", poolsize " + poolSize);
        if (index >= poolSize) {
            LOGGER.info("Over poolsize , " + poolSize);
            return null;
        }
        BlockingQueue<IMessageConnection> queue = MAP_QUEUE.get(index);
        Integer counter = MAP_COUNTER.get(index);
        if (queue == null) {
            queue = new LinkedBlockingQueue<>();
            MAP_QUEUE.put(index, queue);
        }
        if (counter == null) {
            counter = Integer.valueOf(0);
        }
        if (counter.intValue() < queueSize) {
            IMessageConnection newSession = createSession();
            if (newSession != null) {
                try {
                    queue.put(newSession);
                    counter = counter + 1;
                    MAP_COUNTER.put(index, counter);
                } catch (InterruptedException e) {
                    LOGGER.error(e, e);
                }
            }
        }
        try {
            IMessageConnection session = queue.take();
            if (session == null) {
                session = createSession();
            }
            return session;
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
        return null;
    }

    public void clearSessions() {

//        LOGGER.info("Clear all session");
        MAP_QUEUE.forEach((id, queue) -> {
            queue.forEach((session) -> {

            });
        });
    }

    public static class Builder {

        private int mQueueSize;
        private int mPoolSize;
        private EngineType mEngineType;
        private ConnectionFactory mFactory;

        public Builder setQueueSize(int queueSize) {
            mQueueSize = queueSize;
            return this;
        }

        public Builder setPoolSize(int queueSize) {
            mPoolSize = queueSize;
            return this;
        }

        public Builder setEngineType(EngineType engineType) {
            mEngineType = engineType;
            return this;
        }

        public Builder setConnectionFactory(ConnectionFactory factory) {
            mFactory = factory;
            return this;
        }

        public MessagingPool build() {
            LOGGER.info("[BUILDER] " + mQueueSize + ", " + mPoolSize);
            MessagingPool pool = new MessagingPool(mPoolSize, mQueueSize, mEngineType, mFactory);
            return pool;
        }
    }
}
