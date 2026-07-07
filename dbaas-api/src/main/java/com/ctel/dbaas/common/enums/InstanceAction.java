package com.ctel.dbaas.common.enums;

import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Arrays;

@Getter
public enum InstanceAction {
    START_INSTANCE("start_instance", "startInstance", false),
    STOP_INSTANCE("stop_instance", "stopInstance", false),
    RESTART_INSTANCE("restart_instance", "restartInstance", false),
    DELETE_INSTANCE("delete_instance", "deleteInstance", false),
    SET_PASSWORD("set_password", "setPassword", false),
    CREATE_BACKUP("create_backup", "createBackup", false),
    RESTORE_BACKUP("restore_backup", "restoreBackup", false),
    CHANGE_GROUP_CONFIG("change_group_config", "changeGroupConfig", false),
    PROMOTE_SLAVE_TO_MASTER("promote_slave_to_master", "promoteSlaveToMaster", false),
//    UPDATE_AGENT_MONITOR("update_monitor_service", "updateMonitorService", false),
    ATTACH_SECURITY_GROUP("attach_security_group", "attachSecurityGroup", false),
    DETACH_SECURITY_GROUP("detach_security_group", "detachSecurityGroup", false),
    RESIZE_INSTANCE("resize_instance", "resizeInstance", false),
    RESIZE_VOLUME("resize_volume", "resizeVolume", false),

    DB_ACTION("db_action", "dbAction", false),
    GET_LIST_USER("get_list_user", "getListUser", true),
    GET_LIST_DATABASE("get_list_database", "getListDatabase", true);

    private final String serviceId;

    private final String methodName;

    private final boolean returnValue;
 
    InstanceAction(String serviceId, String methodName, boolean returnValue) {
        this.serviceId = serviceId;
        this.methodName = methodName;
        this.returnValue = returnValue;
    }

    @SneakyThrows
    public static InstanceAction get(String serviceId) {
        return Arrays.stream(InstanceAction.values())
                .filter(value -> value.getServiceId().equals(serviceId))
                .findFirst()
                .orElse(null);
    }

    @SneakyThrows
    public static InstanceAction getOrThrow(String serviceId) {
        return Arrays.stream(InstanceAction.values())
                .filter(value -> serviceId.equals(value.getServiceId()))
                .findFirst()
                .orElseThrow(() -> new Exception("action " + serviceId + " not support"));
    }

}
