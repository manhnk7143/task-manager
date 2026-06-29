package com.dev.dbaas.utils;

public class ResourceNameUtil {

    public static String buildComputeName(String orgId, String datastoreCode, String role, String instanceId) {
        return String.format("%s_%s_%s_instance_%s", orgId, datastoreCode, role, instanceId);
    }

    public static String buildVolumeName(String orgId, String datastoreCode, String role, String instanceId) {
        return String.format("%s_%s_%s_instance_%s", orgId, datastoreCode, role, instanceId);
    }

}
