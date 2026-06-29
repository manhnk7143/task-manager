/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.messaging;

import com.rabbitmq.client.*;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author hieutrinh
 */
public class RabbitConnection extends IMessageConnection {

    private static final Logger LOGGER = Logger.getLogger(RabbitConnection.class);
    private static final HashedWheelTimer TIMER = new HashedWheelTimer();
    private ConnectionFactory factory = null;
    private List<ConnectionInfo> connectionInfos = new ArrayList<>();
    private Connection connection;
    private SubmissionPublisher publisher;
    private Map<String, Channel> mapChannelReceive;
    private Map<String, Channel> mapChannelSend;

    public RabbitConnection(ConnectionFactory mFactory) {
        try {
            factory = mFactory;
            publisher = new SubmissionPublisher();
            mapChannelReceive = new HashMap<>();
            mapChannelSend = new HashMap<>();
            LOGGER.info("Factory: " + factory);
            initConnection();
            checkConnection();
        } catch (Exception ex) {
            LOGGER.info(ex, ex);
        }
    }

    private void checkConnection() {

//        LOGGER.info("Start checking rabbit connection ... " + getTag());
        TIMER.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                if (!connection.isOpen()) {
                    LOGGER.info("Connection is lost connection, renew connection ");
                    initConnection();
                    reRegisterExchange();
                }
                checkConnection();
            }
        }, 30, TimeUnit.SECONDS);
    }

    private void initConnection() throws Exception {

//        LOGGER.info("Init new rabbit connection " + getTag());
        if (connection != null) {
            connection.close();
        }
        connection = factory.newConnection();
    }

    private void reRegisterExchange(){
//        LOGGER.info("reRegisterExchange - autoReconnection : "+connectionInfos.size());
        for(ConnectionInfo connectionInfo : connectionInfos){
            registerQueueExchange(connectionInfo.getQueue(), connectionInfo.getExchange());
        }
    }

    @Override
    public boolean emmitQueueDurable(String exchange, String queue, JSONObject data) {
//        LOGGER.info("send - "+ exchange +" - "+queue+", Factory " + factory);
        if (connection == null) {
            try {
                initConnection();
            } catch (Exception ex) {
                LOGGER.error(ex, ex);
            }
        }
        try {
            Channel channel = mapChannelSend.get(queue);
            if (channel != null && !channel.isOpen()) {
                try {
                    channel.close();
                } catch (TimeoutException e) {
                    LOGGER.error(e, e);
                }
            }
            if (channel == null) {
                channel = connection.createChannel();
                mapChannelSend.put(queue, channel);
            }
            // check channel
            channel.exchangeDeclare(exchange, "direct", true);
            channel.queueDeclare(queue, true, false, false, null);
            try {
                AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().build();
                channel.basicPublish(exchange, queue, properties, data.toString().getBytes("UTF-8"));
            } catch (IOException ex) {
                LOGGER.error(ex, ex);
            }
        } catch (IOException ex) {
            LOGGER.error(ex, ex);
        }
        return true;
    }

    @Override
    public boolean registerQueueExchange(String queue, String exchange) {

//        LOGGER.info("Register queue " + queue + ", Factory " + factory);
        if (connection == null) {
            try {
                initConnection();
            } catch (Exception ex) {
                LOGGER.error(ex, ex);
            }
        }

        boolean result = false;
        try {
            Channel channel = mapChannelReceive.get(queue);
            if (channel != null && !channel.isOpen()) {
                try {
                    channel.close();
                } catch (TimeoutException e) {
                    LOGGER.error(e, e);
                }
            }
            if (channel == null) {

                boolean isAdded = true;
                for(ConnectionInfo connectionInfo : connectionInfos){
                    if(connectionInfo.toString().equalsIgnoreCase(exchange+queue)){
                        isAdded = false;
                    }
                }
                if(isAdded){
                    ConnectionInfo connectionInfo = new ConnectionInfo();
                    connectionInfo.setExchange(exchange);
                    connectionInfo.setQueue(queue);
                    connectionInfos.add(connectionInfo);
                }

                channel = connection.createChannel();
                channel.exchangeDeclare(exchange, "direct", true);
                channel.queueDeclare(queue, true, false, false, null);
                //String queueName = channel.queueDeclare().getQueue();
                LOGGER.info("QueueName "+queue);
                channel.queueBind(queue, exchange, "");
                mapChannelReceive.put(queue, channel);

                LOGGER.info("Register new queue name " + queue);
                DeliverCallback deliverCallback = new DeliverCallback() {
                    @Override
                    public void handle(String consumerTag, Delivery delivery) throws IOException {
                        try {
                            String content = new String(delivery.getBody(), "UTF-8");
                            //LOGGER.info("Tag: " + consumerTag + "; correlationId: " + delivery.getProperties().getCorrelationId());
                            JSONObject dataItem = new JSONObject(content);
                            if (dataItem.toString().contains("started")) {
                                //LOGGER.info("DEBUG "+dataItem.toString());
                            }

                            String transactionId = dataItem.optString("transaction", "");
                            String janus = dataItem.optString("janus", "");
                            if (!janus.equalsIgnoreCase("ack")) {
                                //LOGGER.info("receive - "+dataItem.toString());
                            }

                            Message message = new Message();
                            message.setData(dataItem);
                            message.setTransactionId(transactionId);
                            publisher.submit(message);
                            //LOGGER.info("Tag: "+publisher.getNumberOfSubscribers());
                        } catch (Exception e) {
                            LOGGER.error(e, e);
                        }
                    }
                };

                channel.basicConsume(queue, true, deliverCallback, new CancelCallback() {
                    @Override
                    public void handle(String string) throws IOException {
                        LOGGER.info("canncel callback");
                    }
                });
            }
            result = true;
        } catch (IOException e) {
            LOGGER.error(e, e);
        }
        return result;
    }

    @Override
    public void addMessagingSubscriber(MessagingSubscriber subscriber) {
        if (subscriber != null) {
            publisher.subscribe(subscriber);
        }
    }

    @Override
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                LOGGER.error(e, e);
            }
        }
    }
}
