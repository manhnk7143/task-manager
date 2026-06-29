package com.dev.dbaas.config;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private static final Logger LOGGER = Logger.getLogger("Config");

    public static final String APP_CONF_FILE_PATH = System
            .getProperty("user.dir")
            + File.separator
            + "config"
            + File.separator + "app.conf";
    public static final String LOG_CONF_FILE_PATH = System
            .getProperty("user.dir")
            + File.separator
            + "config"
            + File.separator + "log.conf";

    public static String app_listener_ip;
    public static int app_listener_port;
    public static int number_app_worker;
    public static int number_callback_worker;
    public static int number_auth_app_worker;
    public static int number_control_worker;

    public static String cache_centers;
    public static String file_certificate;
    public static String pass_certificate;

    public static String config_hibernate_path;
    public static String config_hibernate_h2_path;
    public static int size_connection_pool;
    public static String rabbit_connection;
    public static int rabbit_port;
    public static String rabbit_username;
    public static String rabbit_password;
    public static String mysql_username;
    public static String mysql_password;
    public static String redis_cluster_password;
    public static String default_queue_config;
    public static String default_exchange_config;

    public static String docker_registry_hn1;
    public static String docker_registry_hcm1;

    // env openstack
    public static String openstack_hn_auth_endpoint;
    public static String openstack_hn_domain_identify;
    public static String openstack_hn_username;
    public static String openstack_hn_password;
    public static String openstack_hn_project_name;
    public static String openstack_hn_project_id;
    public static String openstack_hn_keypair_name;
    public static String openstack_hn_security_group_id_manager;
    public static String openstack_hn_network_id_manager;
    public static String openstack_hn_subnet_id_manager;

    public static String openstack_hcm_auth_endpoint;
    public static String openstack_hcm_domain_identify;
    public static String openstack_hcm_username;
    public static String openstack_hcm_password;
    public static String openstack_hcm_project_name;
    public static String openstack_hcm_project_id;
    public static String openstack_hcm_keypair_name;
    public static String openstack_hcm_security_group_id_manager;
    public static String openstack_hcm_network_id_manager;
    public static String openstack_hcm_subnet_id_manager;

    // Mysql
    public static String openstack_hn1_mysql_auth_endpoint;
    public static String openstack_hn1_mysql_domain_identify;
    public static String openstack_hn1_mysql_username;
    public static String openstack_hn1_mysql_password;
    public static String openstack_hn1_mysql_project_name;
    public static String openstack_hn1_mysql_project_id;
    public static String openstack_hn1_mysql_keypair_name;
    public static String openstack_hn1_mysql_security_group_id_manager;
    public static String openstack_hn1_mysql_network_id_manager;
    public static String openstack_hn1_mysql_subnet_id_manager;
    public static String openstack_hn1_mysql_app_id;
    public static String openstack_hn1_mysql_secret;

    public static String openstack_hcm1_mysql_auth_endpoint;
    public static String openstack_hcm1_mysql_domain_identify;
    public static String openstack_hcm1_mysql_username;
    public static String openstack_hcm1_mysql_password;
    public static String openstack_hcm1_mysql_project_name;
    public static String openstack_hcm1_mysql_project_id;
    public static String openstack_hcm1_mysql_keypair_name;
    public static String openstack_hcm1_mysql_security_group_id_manager;
    public static String openstack_hcm1_mysql_network_id_manager;
    public static String openstack_hcm1_mysql_subnet_id_manager;
    public static String openstack_hcm1_mysql_app_id;
    public static String openstack_hcm1_mysql_secret;

    // Kafka
    public static String openstack_hn1_kafka_auth_endpoint;
    public static String openstack_hn1_kafka_domain_identify;
    public static String openstack_hn1_kafka_username;
    public static String openstack_hn1_kafka_password;
    public static String openstack_hn1_kafka_project_name;
    public static String openstack_hn1_kafka_project_id;
    public static String openstack_hn1_kafka_keypair_name;
    public static String openstack_hn1_kafka_security_group_id_manager;
    public static String openstack_hn1_kafka_network_id_manager;
    public static String openstack_hn1_kafka_subnet_id_manager;

    public static String openstack_hcm1_kafka_auth_endpoint;
    public static String openstack_hcm1_kafka_domain_identify;
    public static String openstack_hcm1_kafka_username;
    public static String openstack_hcm1_kafka_password;
    public static String openstack_hcm1_kafka_project_name;
    public static String openstack_hcm1_kafka_project_id;
    public static String openstack_hcm1_kafka_keypair_name;
    public static String openstack_hcm1_kafka_security_group_id_manager;
    public static String openstack_hcm1_kafka_network_id_manager;
    public static String openstack_hcm1_kafka_subnet_id_manager;
    //

    public static String rabbit_api_queue;
    public static String rabbit_api_exchange;
    public static String rabbit_resource_instance_queue;
    public static String rabbit_resource_instance_exchange;

    public static String socket_url;
    public static String secretkey_configuration_agent_firmware;

    public static String database_default;
    public static String database_host_master;
    public static int database_port_master;
    public static String database_username_master;
    public static String database_password_master;
    public static int max_mysql_connection_pool;
    public static String rabbit_client_key_password;
    public static String rabbit_trust_key_password;
    public static String rabbit_trust_key_file;
    public static String rabbit_client_key_file;

    public static boolean loadConfig() {

        PropertyConfigurator.configure(LOG_CONF_FILE_PATH);
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(APP_CONF_FILE_PATH));

            // load configuration for application
            number_app_worker = Integer.parseInt(properties.getProperty(
                    "number_app_worker", "4"));

            number_auth_app_worker = Integer.parseInt(properties.getProperty(
                    "number_auth_app_worker", "4"));

            number_control_worker = Integer.parseInt(properties.getProperty(
                    "number_control_worker", "4"));


            number_callback_worker = Integer.parseInt(properties.getProperty(
                    "number_callback_worker", "4"));

            redis_cluster_password = properties.getProperty("redis_cluster_password", "7dvLVW8B9peTThjGFzq95at6UBzhkbzUfGHPGYnpQY3K4h34").trim();

            app_listener_ip = properties.getProperty("app_listener_ip", "0.0.0.0").trim();
            app_listener_port = Integer.parseInt(properties.getProperty(
                    "app_listener_port", "9090"));

            cache_centers = properties.getProperty("cache_centers", "1,127.0.0.1:11211").trim();

            file_certificate = properties.getProperty("file_certificate", "config/btomcat.jks").trim();
            pass_certificate = properties.getProperty("pass_certificate", "123456").trim();

            config_hibernate_path = properties.getProperty("config_hibernate_path", "hibernate.cfg.xml").trim();

            size_connection_pool = Integer.parseInt(properties.getProperty(
                    "size_connection_pool", "10"));

            rabbit_connection = properties.getProperty("rabbit_connection", "203.205.9.195").trim();
            rabbit_port = Integer.parseInt(properties.getProperty(
                    "rabbit_port", "5674"));
            rabbit_username = properties.getProperty("rabbit_username", "rabbitdev").trim();
            rabbit_password = properties.getProperty("rabbit_password", "cmFiYml0ZGV2Cg==").trim();
            rabbit_trust_key_password = properties.getProperty("rabbit_trust_key_password", "sdjfhw87JHKJG4").trim();
            rabbit_client_key_password = properties.getProperty("rabbit_client_key_password", "sdjfhw87JHKJG4").trim();

            rabbit_client_key_file = properties.getProperty("rabbit_client_key_file", "config/keystore.p12").trim();
            rabbit_trust_key_file = properties.getProperty("rabbit_trust_key_file", "config/truststore.p12").trim();

            mysql_username = properties.getProperty("mysql_username", "root").trim();
            mysql_password = properties.getProperty("mysql_password", "123456aA@").trim();

            default_queue_config = properties.getProperty("default_queue_config",
                    "dbaas.taskmanager");

            default_exchange_config = properties.getProperty("default_exchange_config",
                    "dbaas.taskmanager");

            rabbit_api_queue = properties.getProperty("rabbit_api_queue",
                    "dbaas.api");
            rabbit_api_exchange = properties.getProperty("rabbit_api_exchange",
                    "dbaas.api");
            rabbit_resource_instance_queue = properties.getProperty("rabbit_resource_instance_queue",
                    "dbaas.resource_instance");
            rabbit_resource_instance_exchange = properties.getProperty("rabbit_resource_instance_exchange",
                    "dbaas.resource_instance");

            socket_url = properties.getProperty("socket_url",
                    "http://192.168.0.117:9091");

            docker_registry_hn1 = properties.getProperty("docker_registry_hn1", "");
            docker_registry_hcm1 = properties.getProperty("docker_registry_hcm1", "");

            openstack_hn_auth_endpoint = properties.getProperty("openstack_hn_auth_endpoint", "http://testv2.gocloud.vn:35357/v3");
            openstack_hn_domain_identify = properties.getProperty("openstack_hn_domain_identify", "default");
            openstack_hn_username = properties.getProperty("openstack_hn_username", "admin");
            openstack_hn_password = properties.getProperty("openstack_hn_password", "21vdsawniOCl6AyyG13fuqrEJGjw58iwupbeJYEp");
            openstack_hn_project_name = properties.getProperty("openstack_hn_project_name", "admin");
            openstack_hn_project_id = properties.getProperty("openstack_hn_project_id", "7651c5d2360844fe827214218a3c4ba4");
            openstack_hn_keypair_name = properties.getProperty("openstack_hn_keypair_name", "");
            openstack_hn_security_group_id_manager = properties.getProperty("openstack_hn_security_group_id_manager", "");
            openstack_hn_network_id_manager = properties.getProperty("openstack_hn_network_id_manager", "");
            openstack_hn_subnet_id_manager = properties.getProperty("openstack_hn_subnet_id_manager", "");

            openstack_hcm_auth_endpoint = properties.getProperty("openstack_hcm_auth_endpoint", "http://testv2.gocloud.vn:35357/v3");
            openstack_hcm_domain_identify = properties.getProperty("openstack_hcm_domain_identify", "default");
            openstack_hcm_username = properties.getProperty("openstack_hcm_username", "admin");
            openstack_hcm_password = properties.getProperty("openstack_hcm_password", "21vdsawniOCl6AyyG13fuqrEJGjw58iwupbeJYEp");
            openstack_hcm_project_name = properties.getProperty("openstack_hcm_project_name", "admin");
            openstack_hcm_project_id = properties.getProperty("openstack_hcm_project_id", "7651c5d2360844fe827214218a3c4ba4");
            openstack_hcm_keypair_name = properties.getProperty("openstack_hcm_keypair_name", "");
            openstack_hcm_security_group_id_manager = properties.getProperty("openstack_hcm_security_group_id_manager", "");
            openstack_hcm_network_id_manager = properties.getProperty("openstack_hcm_network_id_manager", "");
            openstack_hcm_subnet_id_manager = properties.getProperty("openstack_hcm_subnet_id_manager", "");

            openstack_hn1_mysql_auth_endpoint = properties.getProperty("openstack_hn1_mysql_auth_endpoint", "");
            openstack_hn1_mysql_domain_identify = properties.getProperty("openstack_hn1_mysql_domain_identify", "");
            openstack_hn1_mysql_username = properties.getProperty("openstack_hn1_mysql_username", "");
            openstack_hn1_mysql_password = properties.getProperty("openstack_hn1_mysql_password", "");
            openstack_hn1_mysql_project_name = properties.getProperty("openstack_hn1_mysql_project_name", "");
            openstack_hn1_mysql_project_id = properties.getProperty("openstack_hn1_mysql_project_id", "");
            openstack_hn1_mysql_keypair_name = properties.getProperty("openstack_hn1_mysql_keypair_name", "");
            openstack_hn1_mysql_security_group_id_manager = properties.getProperty("openstack_hn1_mysql_security_group_id_manager", "");
            openstack_hn1_mysql_network_id_manager = properties.getProperty("openstack_hn1_mysql_network_id_manager", "");
            openstack_hn1_mysql_subnet_id_manager = properties.getProperty("openstack_hn1_mysql_subnet_id_manager", "");
            openstack_hn1_mysql_app_id = properties.getProperty("openstack_hn1_mysql_app_id", "");
            openstack_hn1_mysql_secret = properties.getProperty("openstack_hn1_mysql_secret", "");

            openstack_hcm1_mysql_auth_endpoint = properties.getProperty("openstack_hcm1_mysql_auth_endpoint", "");
            openstack_hcm1_mysql_domain_identify = properties.getProperty("openstack_hcm1_mysql_domain_identify", "");
            openstack_hcm1_mysql_username = properties.getProperty("openstack_hcm1_mysql_username", "");
            openstack_hcm1_mysql_password = properties.getProperty("openstack_hcm1_mysql_password", "");
            openstack_hcm1_mysql_project_name = properties.getProperty("openstack_hcm1_mysql_project_name", "");
            openstack_hcm1_mysql_project_id = properties.getProperty("openstack_hcm1_mysql_project_id", "");
            openstack_hcm1_mysql_keypair_name = properties.getProperty("openstack_hcm1_mysql_keypair_name", "");
            openstack_hcm1_mysql_security_group_id_manager = properties.getProperty("openstack_hcm1_mysql_security_group_id_manager", "");
            openstack_hcm1_mysql_network_id_manager = properties.getProperty("openstack_hcm1_mysql_network_id_manager", "");
            openstack_hcm1_mysql_subnet_id_manager = properties.getProperty("openstack_hcm1_mysql_subnet_id_manager", "");
            openstack_hcm1_mysql_app_id = properties.getProperty("openstack_hcm1_mysql_app_id", "");
            openstack_hcm1_mysql_secret = properties.getProperty("openstack_hcm1_mysql_secret", "");

            openstack_hn1_kafka_auth_endpoint = properties.getProperty("openstack_hn1_kafka_auth_endpoint", "");
            openstack_hn1_kafka_domain_identify = properties.getProperty("openstack_hn1_kafka_domain_identify", "");
            openstack_hn1_kafka_username = properties.getProperty("openstack_hn1_kafka_username", "");
            openstack_hn1_kafka_password = properties.getProperty("openstack_hn1_kafka_password", "");
            openstack_hn1_kafka_project_name = properties.getProperty("openstack_hn1_kafka_project_name", "");
            openstack_hn1_kafka_project_id = properties.getProperty("openstack_hn1_kafka_project_id", "");
            openstack_hn1_kafka_keypair_name = properties.getProperty("openstack_hn1_kafka_keypair_name", "");
            openstack_hn1_kafka_security_group_id_manager = properties.getProperty("openstack_hn1_kafka_security_group_id_manager", "");
            openstack_hn1_kafka_network_id_manager = properties.getProperty("openstack_hn1_kafka_network_id_manager", "");
            openstack_hn1_kafka_subnet_id_manager = properties.getProperty("openstack_hn1_kafka_subnet_id_manager", "");

            openstack_hcm1_kafka_auth_endpoint = properties.getProperty("openstack_hcm1_kafka_auth_endpoint", "");
            openstack_hcm1_kafka_domain_identify = properties.getProperty("openstack_hcm1_kafka_domain_identify", "");
            openstack_hcm1_kafka_username = properties.getProperty("openstack_hcm1_kafka_username", "");
            openstack_hcm1_kafka_password = properties.getProperty("openstack_hcm1_kafka_password", "");
            openstack_hcm1_kafka_project_name = properties.getProperty("openstack_hcm1_kafka_project_name", "");
            openstack_hcm1_kafka_project_id = properties.getProperty("openstack_hcm1_kafka_project_id", "");
            openstack_hcm1_kafka_keypair_name = properties.getProperty("openstack_hcm1_kafka_keypair_name", "");
            openstack_hcm1_kafka_security_group_id_manager = properties.getProperty("openstack_hcm1_kafka_security_group_id_manager", "");
            openstack_hcm1_kafka_network_id_manager = properties.getProperty("openstack_hcm1_kafka_network_id_manager", "");
            openstack_hcm1_kafka_subnet_id_manager = properties.getProperty("openstack_hcm1_kafka_subnet_id_manager", "");

            secretkey_configuration_agent_firmware = properties.getProperty("secretkey_configuration_agent_firmware", "DIlaYY3T3kHgGLtUizo3JyjgCfz1Qumu");

            database_host_master = properties.getProperty("database_host_master", "203.205.9.195").trim();
            database_port_master = Integer.parseInt(properties.getProperty("database_port_master", "3306"));
            database_username_master = properties.getProperty("database_username_master", "dbaas").trim();
            database_password_master = properties.getProperty("database_password_master", "AoUvbbdmg7").trim();
            max_mysql_connection_pool = Integer.parseInt(properties.getProperty(
                    "max_mysql_connection_pool", "10"));
            database_default = properties.getProperty("database_default", "cloudops_dbaas").trim();

            if (app_listener_ip.isEmpty()) {
                LOGGER.info("Require specific app_listener_ip");
                return false;
            }

        } catch (IOException e) {
            LOGGER.error("Error load file configuration" + e);
        }
        return true;
    }

}
