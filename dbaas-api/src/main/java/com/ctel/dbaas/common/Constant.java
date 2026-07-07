package com.ctel.dbaas.common;

import jlibs.core.util.regex.TemplateMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constant {

    public static final TemplateMatcher MATCHER = new TemplateMatcher("${", "}");

    public static final String GLANCE_IMAGE_TAG = "cmc-dbaas-agent";
    public static final int BUS_PORT_DEFAULT_REDIS = 16379;

    public static String PATH_FILE_CONFIG_GUEST = "/etc/cmc/conf.d/guest_info.conf";

    public static Integer MAX_NANO_SECOND = 999999999;

    public static Integer RESOURCE_UNLIMITED = -1;

    public static final int KAFKA_MAX_TOPIC_NAME_LENGTH = 249;

    public static String LOCALHOST = "localhost";

    public static int PORT_DEFAULT_MONGODB = 27017;

    public static String PATTERN_URI_MONGODB_STAND = "mongodb://${username}:${password}@${host}:${port}/";

    public static String SYSTEM = "system";

    public static String ROOT_USER_MONGODB = "root";

    public static String NETWORK_USER = "user";

    public static String NETWORK_SYSTEM = "system";

    public static String PRIMARY_MONGODB = "primary";

    public static String SECONDARY_MONGODB = "secondary";

    public static String ARBITER_MONGODB = "arbiter";

    public static String REPLICASET_MONGODB = "replicaset";

    public static String REGEX_PASSWORD = "^[A-Za-z0-9]{8,32}$";

    public static int PORT_DEFAULT_REDIS = 6379;

    public static int MAX_VOLUME_SIZE = 32000;

    public static int MIN_VOLUME_SIZE = 20;

    public static String COMPLETED = "COMPLETED";

    public static String ERROR = "ERROR";

    public static final List<String> TIME_ZONE_SUPPORT = new ArrayList<>(Arrays.asList(
            "GMT-12:00","GMT-11:00","GMT-10:00","GMT-09:00","GMT-08:00","GMT-07:00","GMT-06:00","GMT-05:00","GMT-04:00","GMT-03:00","GMT-02:00","GMT-01:00","GMT+00:00","GMT+01:00","GMT+02:00","GMT+03:00","GMT+04:00","GMT+05:00","GMT+06:00","GMT+07:00","GMT+08:00","GMT+09:00","GMT+10:00","GMT+11:00","GMT+12:00"));

    public static String TEMPLATE_CLOUD_CONFIG = """
            - encoding: b64
              owner: ubuntu:root
              path: ${path_file}
              content: ${content}
            """;

    public static String TEMPLATE_AGENT_INFO = """
            [default]
            datastore_manager=${datastore_manager}
            datastore_version=${datastore_version}
            encrypted_key=${encrypted_key}
            agent_id=${agent_id}
                        
            """;

    public static class STATUS {
        public static final int SUCCESS = 1;
        public static final int ERROR = 0;
    }

    public static class ResourceTypeMonitor {
        public static final String REDIS = "redisdb";
        public static final String MONGODB = "mongo";
    }

    public static class Message {
        public static final String SUCCESS = "Success";

        public static final String ERROR = "Error";
    }

}
