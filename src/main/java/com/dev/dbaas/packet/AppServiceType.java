package com.dev.dbaas.packet;

public enum AppServiceType {

    REQUIRE_LOGIN("require_login"),
    NOT_FOUND("not_found"),
    NONE("none"),
    AUTHENTICATION("authentication_agent"),
    UPDATE_STATUS_INSTANCE("update_status_instance"),
    UPDATE_STATUS_BACKUP("update_status_backup"),
    UPDATE_STATUS_COMPUTE("update_status_compute"),
    CHANGE_GROUP_CONFIG("change_group_config"),
    START_INSTANCE("start_instance"),
    STOP_INSTANCE("stop_instance"),
    GET_AGENT_INFO("get_agent_info"),
    RESTART_INSTANCE("restart_instance"),
    RESTORE_BACKUP("restore_backup"),
    SET_PASSWORD("set_password"),
    CHECK_NEW_VERSION("check_new_version"),
    PROMOTE_SLAVE_MASTER("promote_slave_master"),
    UPDATE_MONITOR_SERVICE("update_monitor_service"),
    CREATE_BACKUP("create_backup"),
    REQUEST_DB_ACTION("request_db_action"),
    RESPONSE_DB_ACTION("response_db_action"),
    UPDATE_STATUS_CHANGE_CONFIG("update_status_change_config"),
    TEST_COMMAND("test_command"),
    RESIZE_FILESYSTEM("resize_filesystem"),;


    private String value;

    private AppServiceType(String mValue) {
        value = mValue;
    }

    public static AppServiceType getAppServiceType(String value) {

        for (AppServiceType temp : AppServiceType.values()) {
            if (temp.getValue().equalsIgnoreCase(value)) {
                return temp;
            }
        }
        return AppServiceType.NONE;
    }

    public String getValue() {
        return value;
    }
}
