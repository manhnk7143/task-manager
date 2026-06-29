/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.manager;

import com.dev.dbaas.config.Constaint;
import com.dev.dbaas.database.enties.TbDatastore;
import com.dev.dbaas.database.utils.DatastoreUtil;
import com.dev.dbaas.pool.SessionPool;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hieutrinh
 */
public class DatastoreManager {

    private static final Logger LOGGER = Logger.getLogger(DatastoreManager.class);
    private static final SessionPool SESSION_POOL;
    private static final int QUEUE_SIZE = 4;
    private static final int POOL_SIZE = 4;
    private static final SecureRandom RAND = new SecureRandom();

    static {
        SESSION_POOL = new SessionPool.Builder().setPoolSize(POOL_SIZE).setQueueSize(QUEUE_SIZE).setPoolName("DatastoreManager").build();
    }

    public static String add(TbDatastore record) {

        long timestamp = System.currentTimeMillis();
        String id = null;
        int index = RAND.nextInt(POOL_SIZE);
        Session session = SESSION_POOL.getSession(index);
        try {
            id = DatastoreUtil.getInstance().createAsString(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        LOGGER.info("add, processing " + (System.currentTimeMillis() - timestamp));
        return id;
    }

    public static void update(TbDatastore record) {

        long timestamp = System.currentTimeMillis();
        int index = Math.abs(record.getId().hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            DatastoreUtil.getInstance().update(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        LOGGER.info("update, processing " + (System.currentTimeMillis() - timestamp));
    }

    public static void delete(TbDatastore record) {

        long timestamp = System.currentTimeMillis();
        int index = Math.abs(record.getId().hashCode()) % POOL_SIZE;
        Session session = SESSION_POOL.getSession(index);
        try {
            DatastoreUtil.getInstance().delete(session, record);
        } catch (Exception ex) {
            LOGGER.error(ex, ex);
        } finally {
            SESSION_POOL.releaseSession(index, session);
        }
        LOGGER.info("delete, processing " + (System.currentTimeMillis() - timestamp));
    }

    public static String generateConfigurationRedisStandalone(String mode, String password, int port) {
        Map<String, String> vars = new HashMap<>();
        vars.put("mode", mode);
        vars.put("password", password);
        vars.put("is_master", "true");
        vars.put("port", String.valueOf(port));
        return convertToSection(vars, "redis");
    }

    public static String generateConfigurationMongoDbStandalone(String mode, String rootUser, String rootPassword) {
        Map<String, String> vars = new HashMap<>();
        vars.put("mode", mode);
        vars.put("root_user", rootUser);
        vars.put("root_password", rootPassword);
        return convertToSection(vars, "mongodb");
    }

    public static String generateConfigurationMongoDbArbiterReplicaset(
            String mode, String mongoRole, String rootUser, String rootPassword, String replicasetKey,
            String replicasetName, String ipPrimary, String ipAddress) {

        Map<String, String> vars = new HashMap<>();
        vars.put("mode", mode);
        vars.put("mongo_role", mongoRole);
        vars.put("root_user", rootUser);
        vars.put("root_password", rootPassword);
        vars.put("replicaset_key", replicasetKey);
        vars.put("replicaset_name", replicasetName);
        vars.put("ip_primary", ipPrimary);
        vars.put("ip_address", ipAddress);
        return convertToSection(vars, "mongodb");
    }

    public static String generateConfigurationMongoDbPrimaryReplicaset(
            String mode, String mongoRole, String rootUser, String rootPassword, String replicasetKey,
            String replicasetName, String ipPrimary, String ipAddress) {

        Map<String, String> vars = new HashMap<>();
        vars.put("mode", mode);
        vars.put("mongo_role", mongoRole);
        vars.put("root_user", rootUser);
        vars.put("root_password", rootPassword);
        vars.put("replicaset_key", replicasetKey);
        vars.put("replicaset_name", replicasetName);
        vars.put("ip_primary", ipPrimary);
        vars.put("ip_address", ipAddress);
        return convertToSection(vars, "mongodb");
    }

    public static String generateConfigurationMongoDbSecondaryReplicaset(
            String mode, String mongoRole, String rootUser, String rootPassword, String replicasetKey,
            String replicasetName, String ipPrimary, String ipAddress) {

        Map<String, String> vars = new HashMap<>();
        vars.put("mode", mode);
        vars.put("mongo_role", mongoRole);
        vars.put("root_user", rootUser);
        vars.put("root_password", rootPassword);
        vars.put("replicaset_key", replicasetKey);
        vars.put("replicaset_name", replicasetName);
        vars.put("ip_primary", ipPrimary);
        vars.put("ip_address", ipAddress);
        return convertToSection(vars, "mongodb");
    }

    public static String generateConfigurationRedisMaster(String mode, String password, int portDefault) {
        Map<String, String> vars = new HashMap<>();
        vars.put("mode", mode);
        vars.put("port", String.valueOf(portDefault));
        vars.put("password", password);
        vars.put("is_master", "true");
        vars.put("role", Constaint.MASTER_ROLE);
        return convertToSection(vars, "redis");
    }

    public static String generateConfigurationRedisCluster(String mode, String role, String password, String clusterNodes,
                                                           int port, int busPort, int replicas) {
        Map<String, String> vars = new HashMap<>();
        vars.put("mode", mode);
        vars.put("role", role);
        vars.put("password", password);
        vars.put("cluster_nodes", clusterNodes);
        vars.put("port", String.valueOf(port));
        vars.put("bus_port", String.valueOf(busPort));
        vars.put("replicas", String.valueOf(replicas));
        return convertToSection(vars, "redis");
    }

    public static String generateConfigurationKafkaCluster(String mode, String clusterId, Integer brokerId, Integer nodeId,
                                                           Integer portDefault, String clusterNodes, boolean enableBasicAuth, String authInfo) {
        Map<String, String> vars = new HashMap<>();
        vars.put("mode", mode);
        vars.put("port_default", String.valueOf(portDefault));
        vars.put("cluster_id", clusterId);
        vars.put("broker_id", String.valueOf(brokerId));
        vars.put("node_id", String.valueOf(nodeId));
        vars.put("cluster_nodes", clusterNodes);
        vars.put("basic_auth", String.valueOf(enableBasicAuth));
        vars.put("users", authInfo);
        return convertToSection(vars, "kafka");
    }

    public static String generateConfigurationKafkaSingleNode(String mode, int portDefault, boolean enableBasicAuth, String authInfo) {
        Map<String, String> vars = new HashMap<>();
        vars.put("mode", mode);
        vars.put("port_default", String.valueOf(portDefault));
        vars.put("basic_auth", String.valueOf(enableBasicAuth));
        vars.put("users", authInfo);
        return convertToSection(vars, "kafka");
    }

    public static String generateConfigurationRedisSlave(String mode, String password, String ipAddressMaster, int portDefault) {
        Map<String, String> vars = new HashMap<>();
        vars.put("is_master", "false");
        vars.put("port", String.valueOf(portDefault));
        vars.put("role", Constaint.SLAVE_ROLE);
        vars.put("mode", mode);
        vars.put("password", password);
        vars.put("master_password", password);
        vars.put("master_host", ipAddressMaster);
        return convertToSection(vars, "redis");
    }

    public static String generateConfigurationMysqlStandalone(String rootPassword, String mode, int portDefault) {
        Map<String, String> vars = new HashMap<>();
        vars.put("mode", mode);
        vars.put("role", "master");
        vars.put("root_password", rootPassword);
        vars.put("port", String.valueOf(portDefault));
        return convertToSection(vars, "mysql");
    }

    public static String generateConfigurationMysqlMasterReplicaset(String mode, String rootPassword, String role, String slaveIps,
                                                              int portDefault, String replicaUser, String replicaPwd, int serverId) {
        Map<String, String> vars = new HashMap<>();
        vars.put("mode", mode);
        vars.put("root_password", rootPassword);
        vars.put("role", role);
        vars.put("slave_ip", slaveIps);
        vars.put("port", String.valueOf(portDefault));
        vars.put("replica_user", replicaUser);
        vars.put("replica_password", replicaPwd);
        vars.put("server_id", String.valueOf(serverId));

        return convertToSection(vars, "mysql");
    }

    public static String generateConfigurationMysqlSlaveReplicaset(String mode, String rootPassword, String role, String primaryIp,
                                                                    int portDefault, String replicaUser, String replicaPwd, int serverId, int primaryPort) {
        Map<String, String> vars = new HashMap<>();
        vars.put("mode", mode);
        vars.put("root_password", rootPassword);
        vars.put("role", role);
        vars.put("primary_ip", primaryIp);
        vars.put("port", String.valueOf(portDefault));
        vars.put("replica_user", replicaUser);
        vars.put("replica_password", replicaPwd);
        vars.put("replica_readonly", "true");
        vars.put("server_id", String.valueOf(serverId));
        vars.put("primary_port", String.valueOf(primaryPort));

        return convertToSection(vars, "mysql");
    }

    public static String generateConfigAPIGStandalone(String role, String mode) {
        Map<String, String> vars = new HashMap<>();
        vars.put("role", role);
        vars.put("mode", mode);

        return convertToSection(vars, "api_gateway");
    }

    private static String convertToSection(Map<String, String> hashMap, String sectionName) {
        StringBuilder sectionBuilder = new StringBuilder();
        sectionBuilder.append("[").append(sectionName).append("]\n");
        for (String key : hashMap.keySet()) {
            String value = hashMap.get(key);
            if (value == null) {
                value = "";
            }
            sectionBuilder.append(key).append("=").append(value).append("\n");
        }

        return sectionBuilder.toString();
    }
}
