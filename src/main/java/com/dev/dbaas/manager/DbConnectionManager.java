/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.manager;

import com.dev.dbaas.config.Config;
import com.dev.dbaas.utils.sessions.DbConnection;
import org.apache.log4j.Logger;

import java.security.SecureRandom;

/**
 * @author hieutrinh
 */
public class DbConnectionManager {

    private static final Logger LOGGER = Logger.getLogger(DbConnectionManager.class);
    private static SecureRandom RAND = new SecureRandom();
    private static DbConnection CONNECTION_MASTER;


    public static DbConnection getSessionMaster() {

        if (CONNECTION_MASTER == null) {
            CONNECTION_MASTER = new DbConnection.Builder().setHost(Config.database_host_master).setPort(Config.database_port_master).setUsername(Config.database_username_master).setPassword(Config.database_password_master).setDatabase(Config.database_default).setMaxConnectionPool(Config.max_mysql_connection_pool * 8).build();
            synchronized (CONNECTION_MASTER) {
                CONNECTION_MASTER.initConnection();
            }
        }
        if (CONNECTION_MASTER != null && !CONNECTION_MASTER.isOpen()) {
            LOGGER.warn("Try to connect to database master");
            synchronized (CONNECTION_MASTER) {
                CONNECTION_MASTER.initConnection();
            }
        }
        return CONNECTION_MASTER;
    }
}
