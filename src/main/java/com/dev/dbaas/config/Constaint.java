package com.dev.dbaas.config;

public class Constaint {

    public static final String PREFIX_CACHE_USER = "user_info_";
    public static final int ENABLE = 1;
    public static final int DISABLE = 0;

    public static final String EXTERNAL_CONTEXT = "external";
    public static final String INTERNAL_CONTEXT = "internal";
    public static final String INBOUND = "inbound";
    public static final String OUTBOUND = "outbound";
    public static final String INTERNAL = "internal";
    public static final String STATE_INCALL = "incall";
    public static final String STATE_HANGUP = "hangup";
    public static final String FROM_PHONENUMBER = "phonenumber";
    public static final String FROM_EXTENSION = "extension";
    public static final String FROM_API = "api";
    public static final String FROM_REGISTRAR = "registrar";
    public static final String SYSTEM_CONTEXT = "system";
    public static final String FROM_AUTO = "auto";
    public static final String PRIVATE_CALL = "private_call";
    public static final String STATE_RINGING = "ringing";
    public static final String STATE_ANSWERED = "answered";

    public static final String STATUS_ERROR = "ERROR";
    public static final String TEMPLATE_AGENT_INFO = "[default]\n" +
            "socket_url=${socket_url}\n" +
            "instance_id=${instance_id}\n" +
            "compute_id=${compute_id}\n" +
            "monitor_resource_type=${monitor_resource_type}\n" +
            "datastore_manager=${datastore_manager}\n" +
            "datastore_version=${datastore_version}\n" +
            "encrypted_key=${encrypted_key}\n" +
            "agent_id=${agent_id}\n" +
            "docker_registry=${docker_registry}\n" +
            "backup_url=${backup_url}\n" +
            "backup_mode=${backup_mode}\n" +
            "ip_address=${ip_address}\n" +
            "\n";
    public static final String TEMPLATE_REDIS_STANDALONE = "[redis]\n" +
            "port=${port}\n" +
            "is_master=${is_master}\n" +
            "mode=${mode}\n" +
            "password=${password}\n";
    public static final String TEMPLATE_MONGO_DB_STANDALONE = "[mongodb]\n" +
            "root_user=${root_user}\n" +
            "mode=${mode}\n" +
            "root_password=${root_password}\n";

    public static final String TEMPLATE_MONGO_DB_ARBITER_REPLICASET = "[mongodb]\n" +
            "mode=${mode}\n" +
            "mongo_role=${mongo_role}\n" +
            "replicaset_key=${replicaset_key}\n" +
            "replicaset_name=${replicaset_name}\n" +
            "ip_primary=${ip_primary}\n" +
            "root_user=${root_user}\n" +
            "ip_address=${ip_address}\n" +
            "root_password=${root_password}\n";

    public static final String TEMPLATE_MONGO_DB_PRIMARY_REPLICASET = "[mongodb]\n" +
            "mode=${mode}\n" +
            "mongo_role=${mongo_role}\n" +
            "replicaset_key=${replicaset_key}\n" +
            "replicaset_name=${replicaset_name}\n" +
            "ip_primary=${ip_primary}\n" +
            "root_user=${root_user}\n" +
            "ip_address=${ip_address}\n" +
            "root_password=${root_password}\n";

    public static final String TEMPLATE_MONGO_DB_SECONDARY_REPLICASET = "[mongodb]\n" +
            "mode=${mode}\n" +
            "mongo_role=${mongo_role}\n" +
            "replicaset_key=${replicaset_key}\n" +
            "replicaset_name=${replicaset_name}\n" +
            "ip_primary=${ip_primary}\n" +
            "root_user=${root_user}\n" + "ip_address=${ip_address}\n" +
            "root_password=${root_password}\n";

    public static final String TEMPLATE_REDIS_CLUSTER = "[redis]\n" +
            "mode=${mode}\n" +
            "role=${role}\n" +
            "password=${password}\n" +
            "cluster_nodes=${cluster_nodes}\n" +
            "port=${port}\n" +
            "bus_port=${bus_port}\n" +
            "replicas=${replicas}\n";

    public static final String TEMPLATE_REDIS_MASTER_CLUSTER = "[redis]\n" +
            "port=${port}\n" +
            "mode=${mode}\n" +
            "is_master=${is_master}\n" +
            "role=${role}\n" +
            "password=${password}\n";
    public static final String TEMPLATE_REDIS_SLAVE_CLUSTER = "[redis]\n" +
            "port=${port}\n" +
            "mode=${mode}\n" +
            "is_master=${is_master}\n" +
            "role=${role}\n" +
            "master_password=${master_password}\n" +
            "password=${password}\n" +
            "master_host=${master_host}\n";

    public static final String TEMPLATE_KAFKA_CLUSTER = "[kafka]\n" +
            "mode=${mode}\n" +
            "port_default=${port_default}\n" +
            "cluster_id=${cluster_id}\n" +
            "broker_id=${broker_id}\n" +
            "node_id=${node_id}\n" +
            "cluster_nodes=${cluster_nodes}\n";

    public static final String PATH_FILE_CONFIG_GUEST = "/etc/dev/conf.d/guest_info.conf";
    public static final String SLAVE_ROLE = "slave";
    public static final String MASTER_ROLE = "master";
    public static final String PRIMARY_ROLE = "primary";
    public static final String SECONDARY_ROLE = "secondary";
    public static final String ARBITER_ROLE = "arbiter";
    public static final String DELETED = "deleted";
    public static final String DELETING = "deleting";
    public static final String MODE_STANDALONE = "standalone";
    public static final String MODE_MASTER_SLAVE = "master_slave";
    public static final String DATASTORE_REDIS = "redis";
    public static final String DATASTORE_MONGO_DB = "mongodb";
    public static final String DATASTORE_KAFKA = "kafka";
    public static final String DATASTORE_MYSQL = "mysql";
    public static final String RUNNING = "running";
    public static final String MODE_SYSTEM = "system";
    public static final String MODE_USER = "user";
    public static final String MODE_REPLICA_SET = "replica_set";
    public static final String POLICY_SERVER_GROUP = "soft-anti-affinity";

    public static String TEMPLATE_CLOUD_CONFIG = "- encoding: b64\n" +
            "  owner: ubuntu:root\n" +
            "  path: ${path_file}\n" +
            "  content: ${content}\n";
    public static final String DONE = "done";
    public static final String FAILURE = "failure";
    public static String EVENT_STATUS = "status";
    public static String EVENT_PHONE_REGISTERED = "user_registered";
    public static String EVENT_USERID = "userId";
    public static String EVENT_FROM = "from";
    public static String EVENT_TO = "to";
    public static String EVENT_URL = "url";
    public static String EVENT_CALLID = "callId";
    public static String EVENT_RECORD_TIME = "recordTime";
    public static String EVENT_CHANNEL_NAME = "channelName";
    public static String EVENT_UUID = "uuid";
    public static String EVENT_AZUSER_ID = "azUserId";
    public static String EVENT_KEY_SESSION = "keySession";
    public static String EVENT_CALL_TYPE = "callType";
    public static String EVENT_FULL_PATH = "fullPath";
    public static String EVENT_TAGS = "tags";
    public static String EVENT_DURATION_CALL = "duration";
    public static String HANGUP_CALL = "hangup";
    public static String ANSWERED_CALL = "answered";
    public static String RINGING_CALL = "ringing";
    public static String REJECTED_CALL = "rejected";
    public static String CANCELED_CALL = "canceled";
    public static int CALLBACK_FAILURE = 0;
    public static int CALLBACK_SUCCESS = 1;

    public static String USER_ROLE = "user";
    public static String ADMIN_ROLE = "admin";
    public static String GROUP_TYPE = "group";
    public static String IVR_TYPE = "ivr";
    public static String EXTENSION_TYPE = "extension";
    public static String VOICEMAIL_TYPE = "voicemail";
    public static String CUSTOM_GROUP_TYPE = "custom-group";
    public static String PHONENUMBER_TYPE = "phonenumber";

    public static int STATE_ON = 1;
    public static int STATE_OFF = 0;
    public static int DEFAULT_DIGIT_CUSTOMER = 1;
    public static String INTERNAL_CALL = "internal";
    public static String EXTERNAL_CALL = "external";
    public static String IP_GATEWAY_SIP = "115.165.166.63";
}
