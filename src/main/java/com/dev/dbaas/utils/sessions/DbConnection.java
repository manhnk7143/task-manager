package com.dev.dbaas.utils.sessions;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DbConnection {

    private static final Logger LOGGER = Logger.getLogger(DbConnection.class);

    @Getter
    @Setter
    private String host;
    @Getter
    @Setter
    private int port;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private String username;
    @Getter
    @Setter
    private String database;
    @Getter
    @Setter
    private int maxConnectionPool;
    @Getter
    @Setter
    private SessionFactory sessionFactory;
    @Getter
    @Setter
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public DbConnection(String mHost, int mPort, String mUsername, String mPassword, String mDatabase, int mMaxConnectionPool) {

        this.host = mHost;
        this.port = mPort;
        if (this.port == 0) {
            this.port = 3306;
        }
        this.username = mUsername;
        this.password = mPassword;
        this.database = mDatabase;
        this.maxConnectionPool = mMaxConnectionPool;
    }

    public void initConnection() {

        if (sessionFactory == null || sessionFactory.isClosed() || !sessionFactory.isOpen()) {

            Configuration configuration = new Configuration();
            configuration.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            configuration.setProperty("hibernate.connection.url", "jdbc:mysql://" + host + ":" + port + "/" + database + "?useUnicode=true&autoReconnect=true&zeroDateTimeBehavior=convertToNull&characterEncoding=UTF-8");
            configuration.setProperty("hibernate.connection.username", username);
            configuration.setProperty("hibernate.connection.password", password);

            configuration.setProperty("hibernate.connection.useUnicode", "true");
            configuration.setProperty("hibernate.connection.autoReconnect", "true");
            configuration.setProperty("hibernate.hikari.connectionTimeout", "20000");
            configuration.setProperty("hibernate.hikari.minimumIdle", "10");
            configuration.setProperty("hibernate.hikari.maximumPoolSize", "" + maxConnectionPool);
            configuration.setProperty("hibernate.hikari.idleTimeout", "300000");

            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties()).build();

            MetadataSources metadataSources = new MetadataSources(standardRegistry);
            metadataSources.addResource("com/dev/dbaas/database/enties/TbAgent.hbm.xml");
            metadataSources.addResource("com/dev/dbaas/database/enties/TbAgentFirmware.hbm.xml");
            metadataSources.addResource("com/dev/dbaas/database/enties/TbAgentHeartbeat.hbm.xml");
            metadataSources.addResource("com/dev/dbaas/database/enties/TbBackup.hbm.xml");
            metadataSources.addResource("com/dev/dbaas/database/enties/TbBackupStrategy.hbm.xml");
            metadataSources.addResource("com/dev/dbaas/database/enties/TbCompute.hbm.xml");
            metadataSources.addResource("com/dev/dbaas/database/enties/TbConfiguration.hbm.xml");
            metadataSources.addResource("com/dev/dbaas/database/enties/TbDatastore.hbm.xml");
            metadataSources.addResource("com/dev/dbaas/database/enties/TbDatastoreConfiguration.hbm.xml");
            metadataSources.addResource("com/dev/dbaas/database/enties/TbInstance.hbm.xml");
            metadataSources.addResource("com/dev/dbaas/database/enties/TbNetwork.hbm.xml");
            metadataSources.addResource("com/dev/dbaas/database/enties/TbVolume.hbm.xml");
            metadataSources.addResource("com/dev/dbaas/database/enties/TbFlavor.hbm.xml");

            Future future = executor.submit(new Runnable() {

                @Override
                public void run() {
                    Metadata metaData = metadataSources.getMetadataBuilder().build();
                    sessionFactory = metaData.getSessionFactoryBuilder().build();
                }
            });
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                LOGGER.error(e, e);
                if (sessionFactory != null) {
                    sessionFactory.close();
                }
            }
        }
        LOGGER.warn("Create factory session, " + sessionFactory);
    }

    public boolean isOpen() {
        return sessionFactory != null && sessionFactory.isOpen() && !sessionFactory.isClosed();
    }

    public static class Builder {

        private String host;
        private int port;
        private String password;
        private String username;
        private String database;
        private int maxConnectionPool;

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setDatabase(String database) {
            this.database = database;
            return this;
        }

        public Builder setMaxConnectionPool(int maxConnectionPool) {
            this.maxConnectionPool = maxConnectionPool;
            return this;
        }

        public DbConnection build() {
            LOGGER.info("[BUILDER] " + host + ", " + port + ", " + username + ", " + password + ", " + database + ", " + maxConnectionPool);
            DbConnection session = new DbConnection(host, port, username, password, database, maxConnectionPool);
            return session;
        }
    }
}
