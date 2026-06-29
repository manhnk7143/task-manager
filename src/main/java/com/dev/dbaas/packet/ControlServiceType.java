package com.dev.dbaas.packet;

public enum ControlServiceType {

    NONE("none"),
    CREATE_BACKUP("create_backup"),
    START_INSTANCE("start_instance"),
    STOP_INSTANCE("stop_instance"),
    RESTART_INSTANCE("restart_instance"),
    RESTORE_BACKUP("restore_backup"),
    DELETE_INSTANCE("delete_instance"),
    SET_PASSWORD("set_password"),
    CHANGE_GROUP_CONFIG("change_group_config"),
    CREATE_INSTANCE_STANDALONE("create_instance_standalone"),
    PROMOTE_SLAVE_MASTER("promote_slave_to_master"),
    UPDATE_MONITOR_SERVICE("update_monitor_service"),
    CREATE_INSTANCE_CLUSTER("create_instance_cluster"),
    TEST_COMMAND("test_command"),
    DB_ACTION("db_action"),
    CREATE_INSTANCE_REPLICASET("create_instance_replicaset"),
    ATTACH_SECURITY_GROUP("attach_security_group"),
    DETACH_SECURITY_GROUP("detach_security_group"),
    RESIZE_INSTANCE("resize_instance"),
    RESIZE_VOLUME("resize_volume"),

    CREATE_REDIS_STANDALONE("create_redis_standalone"),
    CREATE_REDIS_MASTER_SLAVE("create_redis_master_slave"),
    CREATE_REDIS_CLUSTER("create_redis_cluster"),
    CREATE_MONGODB_STANDALONE("create_mongodb_standalone"),
    CREATE_MONGODB_REPLICASET("create_mongodb_replicaset"),
    CREATE_KAFKA_CLUSTER("create_kafka_cluster"),
    CREATE_KAFKA_SINGLE_NODE("create_kafka_single_node"),
    CREATE_MYSQL_STANDALONE("create_mysql_standalone"),
    CREATE_MYSQL_REPLICASET("create_mysql_replicaset"),
    CREATE_API_GATEWAY_STANDALONE("create_api_gateway_standalone");

    private String value;

    private ControlServiceType(String mValue) {
        value = mValue;
    }

    public static ControlServiceType getAppServiceType(String value) {

        for (ControlServiceType temp : ControlServiceType.values()) {
            if (temp.getValue().equalsIgnoreCase(value)) {
                return temp;
            }
        }
        return ControlServiceType.NONE;
    }

    public String getValue() {
        return value;
    }
}
