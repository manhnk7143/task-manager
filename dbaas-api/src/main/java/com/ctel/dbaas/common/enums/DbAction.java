package com.ctel.dbaas.common.enums;

import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Arrays;

public class DbAction {

    @Getter
    public enum Mongodb {
        GET_LIST_DATABASE("get_list_database", "getListDatabase"),
        GET_LIST_COLLECTION("get_list_collection", "getListCollection"),
        CREATE_DATABASE("create_database", "createDatabase"),
        DROP_DATABASE("drop_database", "dropDatabase"),
        DROP_COLLECTION("drop_collection", "dropCollection"),
        GET_LIST_USER("get_list_user", "getListUser"),
        CREATE_USER("create_user", "createUser"),
        DROP_USER("drop_user", "dropUser"),
        UPDATE_PERMISSION("update_permission", "updatePermission");

        private final String serviceId;
        private final String methodName;

        Mongodb(String serviceId, String methodName) {
            this.serviceId = serviceId;
            this.methodName = methodName;
        }

        @SneakyThrows
        public static Mongodb get(String serviceId) {
            if (serviceId == null) {
                return null;
            }
            return Arrays.stream(Mongodb.values())
                    .filter(value -> serviceId.equals(value.getServiceId()))
                    .findFirst()
                    .orElse(null);
        }
    }

    @Getter
    public enum Kafka {
        GET_LIST_TOPIC("get_list_topic", "getListTopic"),
        CREATE_TOPIC("create_topic", "createTopic"),
        DELETE_TOPIC("delete_topic", "deleteTopic"),
        GET_LIST_CONSUMER_GROUP("get_list_consumer_group", "getListConsumerGroup"),
        GET_MESSAGE_BY_OPTIONS("get_message_by_options", "getMessageByOptions"),
        PARTITION_REASSIGNMENT("partition_reassignment", "partitionReassignment"),
        EDIT_TOPIC_CONFIG("edit_topic_config", "editTopicConfig"),
//        INCREASE_PARTITION("increase_partition", "increasePartition"),
        PUBLIC_MESSAGE("publish_message", "publishMessage"),
        GET_CONSUMER_LAG("get_consumer_lag", "getConsumerLag"),
        GET_CLUSTER_CONFIG("get_cluster_config", "getClusterConfig"),
        EDIT_CLUSTER_CONFIG("edit_cluster_config", "editClusterConfig");

        private final String serviceId;
        private final String methodName;

        Kafka(String serviceId, String methodName) {
            this.serviceId = serviceId;
            this.methodName = methodName;
        }

        @SneakyThrows
        public static Kafka get(String serviceId) {
            if (serviceId == null) {
                return null;
            }
            return Arrays.stream(Kafka.values())
                    .filter(value -> serviceId.equals(value.getServiceId()))
                    .findFirst()
                    .orElse(null);
        }
    }

}
