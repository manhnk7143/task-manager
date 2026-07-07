package com.ctel.dbaas.config;

import lombok.extern.log4j.Log4j2;

import java.util.Optional;

@Log4j2
public class EnvConfig {

    public static final String APIKEY_ADD_AGENT_FIRMWARE;
    public static final String KEY_DECRYPT_CONFIG_INSTANCE;
    public static final Long DB_CONNECTION_TIMEOUT;
    public static final Integer DB_MAX_POOL_SIZE;
    public static final Integer DB_MIN_IDLE;
    public static final String DB_PASSWORD;
    public static final String DB_DBAAS_URL;
    public static final String DB_AUTO_SCALE_URL;
    public static final String DB_USERNAME;
    public static final String GRPC_MONITOR_URL;
    public static final Integer GRPC_SERVER_PORT;
    public static final String HIBERNATE_DDL_AUTO;
    public static final Boolean HIBERNATE_FORMAT_SQL;
    public static final Boolean JPA_SHOW_SQL;
    public static final Boolean JPA_GENERATE_DDL;
    public static final String KEY_DECRYPT_CONFIG_BACKUP;

    public static final String OS_REDIS_KEY_PAIR;
    public static final String OS_REDIS_HN1_NETWORK;
    public static final String OS_REDIS_HN1_SECURITY_GROUP;
    public static final String OS_REDIS_HN1_SUBNET;

    public static final String OS_REDIS_HCM1_NETWORK;
    public static final String OS_REDIS_HCM1_SECURITY_GROUP;
    public static final String OS_REDIS_HCM1_SUBNET;

    public static final String OS_MONGODB_KEY_PAIR;
    public static final String OS_MONGODB_HN1_NETWORK;
    public static final String OS_MONGODB_HN1_SECURITY_GROUP;
    public static final String OS_MONGODB_HN1_SUBNET;

    public static final String OS_MONGODB_HCM1_NETWORK;
    public static final String OS_MONGODB_HCM1_SECURITY_GROUP;
    public static final String OS_MONGODB_HCM1_SUBNET;

    public static final String OS_FLAVOR_INIT_MONGODB_ARBITER;

    public static final String OS_KAFKA_KEY_PAIR;
    public static final String OS_KAFKA_HN1_NETWORK;
    public static final String OS_KAFKA_HN1_SECURITY_GROUP;
    public static final String OS_KAFKA_HN1_SUBNET;
    public static final String OS_KAFKA_HCM1_NETWORK;
    public static final String OS_KAFKA_HCM1_SECURITY_GROUP;
    public static final String OS_KAFKA_HCM1_SUBNET;

    public static final String DOCKER_REGISTRY_HN;
    public static final String DOCKER_REGISTRY_HCM;

    public static final String RABBIT_HOST;
    public static final Integer RABBIT_PORT;
    public static final String RABBIT_USERNAME;
    public static final String RABBIT_PASSWORD;
    public static final String RABBIT_SSL_KEYSTORE;
    public static final String RABBIT_SSL_KEYSTORE_TYPE;
    public static final String RABBIT_SSL_KEYSTORE_PASSWORD;
    public static final String RABBIT_SSL_TRUSTSTORE;
    public static final String RABBIT_SSL_TRUSTSTORE_PASSWORD;
    public static final Boolean RABBIT_SSL_VALIDATE_SERVER_CERT;
    public static final Boolean RABBIT_SSL_VERIFY_HOST_NAME;

    public static final String S3_ACCESS_KEY;
    public static final String S3_BUCKET_NAME;
    public static final String S3_ENDPOINT;
    public static final Long S3_PRESINGED_EXPIRE_HOUR;
    public static final String S3_REGION;
    public static final String S3_SECRET_KEY;

    public static final String SOCKET_SERVER_HOST;
    public static final Integer SOCKET_SERVER_PORT;

    public static final String REDIS_STANDALONE_HOST;
    public static final Integer REDIS_STANDALONE_PORT;
    public static final String REDIS_STANDALONE_PASSWORD;
    public static final Integer REDIS_STANDALONE_DATABASE;

    public static final Integer DBAAS_APIG_CONF_DB_PORT;
    public static final String DBAAS_APIG_CONF_DB_NAME;
    public static final String DBAAS_APIG_CONF_DB_USERNAME;

    public static final String CNTT_API_KEY;
    public static final String LOG_BILLING_API_KEY;

    public static final String TEST_OS_KEY_PAIR;
    public static final String TEST_OS_NETWORK;
    public static final String TEST_OS_SECURITY_GROUP;
    public static final String TEST_OS_SUBNET;
    public static final String TEST_REGION_DEV;
    public static final String TEST_ZONE_DEV;

    // validate
    public static final Integer BK_MIN_KEEP_RECORD;
    public static final Integer BK_MAX_KEEP_RECORD;

    static {
        APIKEY_ADD_AGENT_FIRMWARE = getValue("APIKEY_ADD_AGENT_FIRMWARE");
        KEY_DECRYPT_CONFIG_INSTANCE = getValue("KEY_DECRYPT_CONFIG_INSTANCE", "mySecret");
        DB_CONNECTION_TIMEOUT = Long.valueOf(getValue("DB_CONNECTION_TIMEOUT", "100000"));
        DB_MAX_POOL_SIZE = Integer.valueOf(getValue("DB_MAX_POOL_SIZE", "30"));
        DB_MIN_IDLE = Integer.valueOf(getValue("DB_MIN_IDLE", "15"));
        DB_DBAAS_URL = getValue("DB_DBAAS_URL");
        DB_PASSWORD = getValue("DB_PASSWORD");
        DB_USERNAME = getValue("DB_USERNAME");
        DB_AUTO_SCALE_URL = getValue("DB_AUTO_SCALE_URL");
        GRPC_MONITOR_URL = getValue("GRPC_MONITOR_URL");
        GRPC_SERVER_PORT = Integer.valueOf(getValue("GRPC_SERVER_PORT", "9090"));

        HIBERNATE_DDL_AUTO = getValue("HIBERNATE_DDL_AUTO", "update");
        HIBERNATE_FORMAT_SQL = Boolean.valueOf(getValue("HIBERNATE_FORMAT_SQL", "true"));

        JPA_SHOW_SQL = Boolean.valueOf(getValue("JPA_SHOW_SQL", "false"));
        JPA_GENERATE_DDL = Boolean.valueOf(getValue("JPA_GENERATE_DDL", "true"));

        KEY_DECRYPT_CONFIG_BACKUP = getValue("KEY_DECRYPT_CONFIG_BACKUP");

        OS_FLAVOR_INIT_MONGODB_ARBITER = getValue("OS_FLAVOR_INIT_MONGODB_ARBITER", "d2");
        OS_REDIS_KEY_PAIR = getValue("OS_REDIS_KEY_PAIR", "");
        OS_MONGODB_KEY_PAIR = getValue("OS_MONGODB_KEY_PAIR", "");
        OS_KAFKA_KEY_PAIR = getValue("OS_KAFKA_KEY_PAIR", "");

        // openstack Redis HN
        OS_REDIS_HN1_NETWORK = getValue("OS_REDIS_HN1_NETWORK", "");
        OS_REDIS_HN1_SECURITY_GROUP = getValue("OS_REDIS_HN1_SECURITY_GROUP", "");
        OS_REDIS_HN1_SUBNET = getValue("OS_REDIS_HN1_SUBNET", "");

        // openstack Redis HCM
        OS_REDIS_HCM1_NETWORK = getValue("OS_REDIS_HCM1_NETWORK", "");
        OS_REDIS_HCM1_SECURITY_GROUP = getValue("OS_REDIS_HCM1_SECURITY_GROUP", "");
        OS_REDIS_HCM1_SUBNET = getValue("OS_REDIS_HCM1_SUBNET", "");

        // openstack MongoDB HN
        OS_MONGODB_HN1_NETWORK = getValue("OS_MONGODB_HN1_NETWORK", "");
        OS_MONGODB_HN1_SECURITY_GROUP = getValue("OS_MONGODB_HN1_SECURITY_GROUP", "");
        OS_MONGODB_HN1_SUBNET = getValue("OS_MONGODB_HN1_SUBNET", "");

        // openstack MongoDB HCM
        OS_MONGODB_HCM1_NETWORK = getValue("OS_MONGODB_HCM1_NETWORK", "");
        OS_MONGODB_HCM1_SECURITY_GROUP = getValue("OS_MONGODB_HCM1_SECURITY_GROUP", "");
        OS_MONGODB_HCM1_SUBNET = getValue("OS_MONGODB_HCM1_SUBNET", "");

        // openstack kafka HN
        OS_KAFKA_HN1_NETWORK = getValue("OS_KAFKA_HN1_NETWORK", "");
        OS_KAFKA_HN1_SECURITY_GROUP = getValue("OS_KAFKA_HN1_SECURITY_GROUP", "");
        OS_KAFKA_HN1_SUBNET = getValue("OS_KAFKA_HN1_SUBNET", "");

        // openstack kafka HCM
        OS_KAFKA_HCM1_NETWORK = getValue("OS_KAFKA_HCM1_NETWORK", "");
        OS_KAFKA_HCM1_SECURITY_GROUP = getValue("OS_KAFKA_HCM1_SECURITY_GROUP", "");
        OS_KAFKA_HCM1_SUBNET = getValue("OS_KAFKA_HCM1_SUBNET", "");

        DOCKER_REGISTRY_HN = getValue("DOCKER_REGISTRY_HN", "registry1.cloud.cmctelecom.vn/rnd_db");
        DOCKER_REGISTRY_HCM = getValue("DOCKER_REGISTRY_HCM", "registry2.cloud.cmctelecom.vn/rnd_db");

        RABBIT_HOST = getValue("RABBIT_HOST");
        RABBIT_PORT = Integer.valueOf(getValue("RABBIT_PORT"));
        RABBIT_USERNAME = getValue("RABBIT_USERNAME");
        RABBIT_PASSWORD = getValue("RABBIT_PASSWORD");
        RABBIT_SSL_KEYSTORE = getValue("RABBIT_SSL.KEYSTORE", "file:C:\\Users\\CMCTELECOM\\Downloads\\key_hoangtv\\client.keystore.p12");
        RABBIT_SSL_KEYSTORE_TYPE = getValue("RABBIT_SSL.KEYSTORE.TYPE");
        RABBIT_SSL_KEYSTORE_PASSWORD = getValue("RABBIT_SSL.KEYSTORE_PASSWORD");
        RABBIT_SSL_TRUSTSTORE = getValue("RABBIT_SSL.TRUSTSTORE", "file:C:\\Users\\CMCTELECOM\\Downloads\\key_hoangtv\\client.truststore.p12");
        RABBIT_SSL_TRUSTSTORE_PASSWORD = getValue("RABBIT_SSL.TRUSTSTORE_PASSWORD");
        RABBIT_SSL_VALIDATE_SERVER_CERT = Boolean.valueOf(getValue("RABBIT_SSL.VALIDATE_SERVER_CERT", "false"));
        RABBIT_SSL_VERIFY_HOST_NAME = Boolean.valueOf(getValue("RABBIT_SSL.VERIFY_HOST_NAME", "false"));

        S3_ACCESS_KEY = getValue("S3_ACCESS_KEY");
        S3_BUCKET_NAME = getValue("S3_BUCKET_NAME", "redis-dev");
        S3_ENDPOINT = getValue("S3_ENDPOINT", "https://s3.cloud.cmctelecom.vn");
        S3_PRESINGED_EXPIRE_HOUR = Long.valueOf(getValue("S3_PRESINGED_EXPIRE_HOUR", "24"));
        S3_REGION = getValue("S3_REGION", "us-east-1");
        S3_SECRET_KEY = getValue("S3_SECRET_KEY");

        SOCKET_SERVER_HOST = getValue("SOCKET_SERVER_HOST", "localhost");
        SOCKET_SERVER_PORT = Integer.valueOf(getValue("SOCKET_SERVER_PORT", "9091"));

        REDIS_STANDALONE_HOST = getValue("REDIS_STANDALONE_HOST", "localhost");
        REDIS_STANDALONE_PORT = Integer.valueOf(getValue("REDIS_STANDALONE_PORT", "6379"));
        REDIS_STANDALONE_PASSWORD = getValue("REDIS_STANDALONE_PASSWORD", "");
        REDIS_STANDALONE_DATABASE = Integer.valueOf(getValue("REDIS_STANDALONE_DATABASE", "0"));

        DBAAS_APIG_CONF_DB_PORT = Integer.valueOf(getValue("DBAAS_APIG_CONF_DB_PORT", "5432"));
        DBAAS_APIG_CONF_DB_NAME = getValue("DBAAS_APIG_CONF_DB_NAME", "kong");
        DBAAS_APIG_CONF_DB_USERNAME = getValue("DBAAS_APIG_CONF_DB_USERNAME", "kong");
//        DBAAS_APIG_CONF_DB_PASSWORD = getValue("DBAAS_APIG_CONF_DB_PASSWORD", "kongpass");

        CNTT_API_KEY = getValue("CNTT_API_KEY");
        LOG_BILLING_API_KEY = getValue("LOG_BILLING_API_KEY");

        TEST_OS_KEY_PAIR = getValue("TEST_OS_KEY_PAIR");
        TEST_OS_NETWORK = getValue("TEST_OS_NETWORK");
        TEST_OS_SECURITY_GROUP = getValue("TEST_OS_SECURITY_GROUP");
        TEST_OS_SUBNET = getValue("TEST_OS_SUBNET");
        TEST_REGION_DEV = getValue("TEST_REGION_DEV");
        TEST_ZONE_DEV = getValue("TEST_ZONE_DEV");

        BK_MIN_KEEP_RECORD = Integer.valueOf(getValue("BK_MIN_KEEP_RECORD", "1"));
        BK_MAX_KEEP_RECORD = Integer.valueOf(getValue("BK_MAX_KEEP_RECORD", "30"));
    }

    private static String getValue(String key, String defaultValue) {
        return System.getenv().getOrDefault(key, defaultValue);
    }

    private static String getValue(String key) {
        String value = getValue(key, null);
        if (value == null) {
            log.error("env {} has not been assigned a value", key);
            System.out.println("env " + key + " has not been assigned a value");
        }

        return value;
    }

}
