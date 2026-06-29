package com.dev.dbaas.manager;

import com.dev.dbaas.messaging.EngineType;
import com.dev.dbaas.messaging.Message;
import com.dev.dbaas.messaging.MessagingConnection;
import com.dev.dbaas.messaging.MessagingSubscriber;
import com.dev.dbaas.worker.job.ControlJob;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class ClusterManager {

    private static final Logger LOGGER = Logger.getLogger(ClusterManager.class);

    public static void listenTopic(String connection, int port, String exchange, String queue, String username, String password) {
//        LOGGER.info("Listen " + connection + ", " + exchange + ", " + queue + ", " + username + ", " + password);
        MessagingSubscriber messagingSubscriber = new MessagingSubscriber() {
            @Override
            public void onProcess(Message message) {

                JSONObject eventData = message.getData();
//                LOGGER.info("eventData : " + eventData.toString());

                ControlJob job = new ControlJob();
                job.setData(eventData.optString("data"));
                job.setTime(eventData.optLong("time"));
                job.setServiceId(eventData.optString("serviceId"));
                WorkerManager.putControlJob(job);
            }
        };
        try {
            MessagingConnection.getInstance().listenQueue(EngineType.RABBIT_MQ, exchange, queue, messagingSubscriber, connection, port, username, password);
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
    }

    public static void sendTopic(String rabbitConnection, int rabbitPort, String rabbitUsername, String rabbitPassword, String exchange, String queue, JSONObject data) {
//        LOGGER.info("Send " + rabbitConnection + ", " + queue + ", " + data);
        try {
            MessagingConnection.getInstance().emmitQueue(EngineType.RABBIT_MQ, rabbitConnection, rabbitPort, rabbitUsername, rabbitPassword, exchange, queue, data);
        } catch (Exception e) {
            LOGGER.error(e, e);
        }
    }
}
