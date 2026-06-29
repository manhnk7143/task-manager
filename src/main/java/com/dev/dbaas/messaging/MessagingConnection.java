/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.messaging;

import com.dev.dbaas.config.Config;
import com.dev.dbaas.pool.MessagingPool;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author hieutrinh
 */
public class MessagingConnection {

    private static final Logger LOGGER = Logger.getLogger(MessagingConnection.class);
    private static final ConcurrentMap<Integer, MessagingPool> MAP_SEND_POOLS = new ConcurrentHashMap<>();
    //private static final ConcurrentMap<String, IMessageConnection> MAP_LISTEN_CONNECTIONS = new ConcurrentHashMap<>();
    //private static final ConcurrentMap<String, ConnectionFactory> MAP_RABBIT_CONNECTIONS_FACTORY = new ConcurrentHashMap<>();

    private IMessageConnection connection;

    public static int POOL_SIZE = 1;
    private static int QUEUE_SIZE = 1;

    private static class MessagingConnectionHolder {
        private static final MessagingConnection INSTANCE = new MessagingConnection();
    }

    public MessagingConnection(){

    }

    public static MessagingConnection getInstance() {
        return MessagingConnectionHolder.INSTANCE;
    }


    public IMessageConnection getIMessageConnection(String queue) {

        LOGGER.info("Get instance of connection, " + queue);
        return connection;
    }


    public IMessageConnection listenQueue(EngineType eventType, String exchange, String queue, MessagingSubscriber subscriber, String rabbitConnection, int rabbitPort, String rabbitUsername, String rabbitPassword) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, IOException, CertificateException, KeyManagementException {
        LOGGER.info("Listen queue, " + queue + "; " + rabbitConnection + "; " + rabbitUsername + ";" + rabbitPassword);
        if (connection == null || !connection.isConnected()) {
            if (eventType == EngineType.RABBIT_MQ) {

                //LOGGER.info(Config.rabbit_client_key_password+" - "+Config.rabbit_client_key_file);
                char[] keyPassphrase = Config.rabbit_client_key_password.toCharArray();
                KeyStore ks = KeyStore.getInstance("PKCS12");
                ks.load(Files.newInputStream(Paths.get(Config.rabbit_client_key_file)), keyPassphrase);

                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, keyPassphrase);

                char[] trustPassphrase = Config.rabbit_trust_key_password.toCharArray();
                KeyStore tks = KeyStore.getInstance("JKS");
                tks.load(Files.newInputStream(Paths.get(Config.rabbit_trust_key_file)), trustPassphrase);

                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(tks);

                SSLContext c = SSLContext.getInstance("TLSv1.2");
                c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(rabbitConnection);
                factory.setPort(rabbitPort);
                factory.setUsername(rabbitUsername);
                factory.setPassword(rabbitPassword);
                factory.setVirtualHost("/");
                factory.useSslProtocol(c);
                //factory.enableHostnameVerification();

                connection = new RabbitConnection(factory);
                connection.setTag(queue);
                connection.registerQueueExchange(queue, exchange);
                connection.setConnected(true);
            } else {
                LOGGER.warn("Cannot find engineType, " + eventType);
            }
        }
        connection.addMessagingSubscriber(subscriber);
        return connection;
    }

    public IMessageConnection emmitQueue(EngineType eventType, String rabbitConnection, int rabbitPort, String rabbitUsername, String rabbitPassword, String exchange, String queue, JSONObject data) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {

        MessagingPool pool = MAP_SEND_POOLS.get(eventType.getValue());
        if (pool == null) {
            char[] keyPassphrase = Config.rabbit_client_key_password.toCharArray();
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(Files.newInputStream(Paths.get(Config.rabbit_client_key_file)), keyPassphrase);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, keyPassphrase);

            char[] trustPassphrase = Config.rabbit_trust_key_password.toCharArray();
            KeyStore tks = KeyStore.getInstance("JKS");
            tks.load(Files.newInputStream(Paths.get(Config.rabbit_trust_key_file)), trustPassphrase);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(tks);

            SSLContext c = SSLContext.getInstance("TLSv1.2");
            c.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitConnection);
            factory.setPort(rabbitPort);
            factory.setUsername(rabbitUsername);
            factory.setPassword(rabbitPassword);
            factory.setVirtualHost("/");
            factory.useSslProtocol(c);

            pool = new MessagingPool.Builder().setPoolSize(POOL_SIZE).setQueueSize(QUEUE_SIZE).setConnectionFactory(factory).setEngineType(eventType).build();
            MAP_SEND_POOLS.put(eventType.getValue(), pool);
        }
        int indexPool = Math.abs(queue.hashCode()) % POOL_SIZE;
        IMessageConnection connection = pool.getSession(indexPool);
        try {
            connection.emmitQueueDurable(exchange, queue, data);
        } catch (Exception e) {
            LOGGER.error(e, e);
        } finally {
            pool.releaseSession(indexPool, connection);
        }
        return connection;
    }
}
