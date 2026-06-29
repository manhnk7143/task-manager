package com.dev.dbaas.manager;

import com.dev.dbaas.config.Config;
import com.dev.dbaas.entity.ZoneInfo;
import org.apache.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ZoneManager {

    private static final Logger LOGGER = Logger.getLogger(ZoneManager.class);
    private static final ConcurrentMap<String, ZoneInfo> MAP_ZONE = new ConcurrentHashMap<>();

    static {
        ZoneInfo redisMongodbHn1 = ZoneInfo.builder()
                .endpoint(Config.openstack_hn_auth_endpoint)
                .username(Config.openstack_hn_username)
                .password(Config.openstack_hn_password)
                .projectId(Config.openstack_hn_project_id)
                .projectName(Config.openstack_hn_project_name)
                .domain(Config.openstack_hn_domain_identify)
                .securityGroupIdManager(Config.openstack_hn_security_group_id_manager)
                .networkIdManager(Config.openstack_hn_network_id_manager)
                .subnetIdManager(Config.openstack_hn_subnet_id_manager)
                .build();

        ZoneInfo redisMongodbHcm1 = ZoneInfo.builder()
                .endpoint(Config.openstack_hcm_auth_endpoint)
                .username(Config.openstack_hcm_username)
                .password(Config.openstack_hcm_password)
                .projectId(Config.openstack_hcm_project_id)
                .projectName(Config.openstack_hcm_project_name)
                .domain(Config.openstack_hcm_domain_identify)
                .securityGroupIdManager(Config.openstack_hcm_security_group_id_manager)
                .networkIdManager(Config.openstack_hcm_network_id_manager)
                .subnetIdManager(Config.openstack_hcm_subnet_id_manager)
                .build();

        ZoneInfo kafkaHn1 = ZoneInfo.builder()
                .endpoint(Config.openstack_hn1_kafka_auth_endpoint)
                .username(Config.openstack_hn1_kafka_username)
                .password(Config.openstack_hn1_kafka_password)
                .projectId(Config.openstack_hn1_kafka_project_id)
                .projectName(Config.openstack_hn1_kafka_project_name)
                .domain(Config.openstack_hn1_kafka_domain_identify)
                .securityGroupIdManager(Config.openstack_hn1_kafka_security_group_id_manager)
                .networkIdManager(Config.openstack_hn1_kafka_network_id_manager)
                .subnetIdManager(Config.openstack_hn1_kafka_subnet_id_manager)
                .build();

        ZoneInfo kafkaHcm1 = ZoneInfo.builder()
                .endpoint(Config.openstack_hcm1_kafka_auth_endpoint)
                .username(Config.openstack_hcm1_kafka_username)
                .password(Config.openstack_hcm1_kafka_password)
                .projectId(Config.openstack_hcm1_kafka_project_id)
                .projectName(Config.openstack_hcm1_kafka_project_name)
                .domain(Config.openstack_hcm1_kafka_domain_identify)
                .securityGroupIdManager(Config.openstack_hcm1_kafka_security_group_id_manager)
                .networkIdManager(Config.openstack_hcm1_kafka_network_id_manager)
                .subnetIdManager(Config.openstack_hcm1_kafka_subnet_id_manager)
                .build();

        ZoneInfo mysqlHn1 = ZoneInfo.builder()
                .endpoint(Config.openstack_hn1_mysql_auth_endpoint)
                .username(Config.openstack_hn1_mysql_username)
                .password(Config.openstack_hn1_mysql_password)
                .projectId(Config.openstack_hn1_mysql_project_id)
                .projectName(Config.openstack_hn1_mysql_project_name)
                .domain(Config.openstack_hn1_mysql_domain_identify)
                .securityGroupIdManager(Config.openstack_hn1_mysql_security_group_id_manager)
                .networkIdManager(Config.openstack_hn1_mysql_network_id_manager)
                .subnetIdManager(Config.openstack_hn1_mysql_subnet_id_manager)
                .applicationCredentialId(Config.openstack_hn1_mysql_app_id)
                .applicationCredentialSecret(Config.openstack_hn1_mysql_secret)
                .build();

        ZoneInfo mysqlHcm1 = ZoneInfo.builder()
                .endpoint(Config.openstack_hcm1_mysql_auth_endpoint)
                .username(Config.openstack_hcm1_mysql_username)
                .password(Config.openstack_hcm1_mysql_password)
                .projectId(Config.openstack_hcm1_mysql_project_id)
                .projectName(Config.openstack_hcm1_mysql_project_name)
                .domain(Config.openstack_hcm1_mysql_domain_identify)
                .securityGroupIdManager(Config.openstack_hcm1_mysql_security_group_id_manager)
                .networkIdManager(Config.openstack_hcm1_mysql_network_id_manager)
                .subnetIdManager(Config.openstack_hcm1_mysql_subnet_id_manager)
                .applicationCredentialId(Config.openstack_hcm1_mysql_app_id)
                .applicationCredentialSecret(Config.openstack_hcm1_mysql_secret)
                .build();

        MAP_ZONE.put("redis-hn-1", redisMongodbHn1);
        MAP_ZONE.put("redis-hcm-1", redisMongodbHcm1);

        MAP_ZONE.put("mongodb-hn-1", redisMongodbHn1);
        MAP_ZONE.put("mongodb-hcm-1", redisMongodbHcm1);

        MAP_ZONE.put("kafka-hn-1", kafkaHn1);
        MAP_ZONE.put("kafka-hcm-1", kafkaHcm1);

        MAP_ZONE.put("mysql-hn-1", mysqlHn1);
        MAP_ZONE.put("mysql-hcm-1", mysqlHcm1);
    }

    public static ZoneInfo getByZoneIdAndDatastore(String zoneId, String datastoreCode) {
        return MAP_ZONE.get(datastoreCode + "-" + zoneId);
    }
}