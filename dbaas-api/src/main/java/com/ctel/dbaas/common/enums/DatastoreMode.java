package com.ctel.dbaas.common.enums;

import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Arrays;

public class DatastoreMode {

    @Getter
    public enum Redis {
        STANDALONE("standalone", "Standalone"),
        MASTER_SLAVE("master_slave", "Master/Slave"),
        CLUSTER("cluster", "Cluster");

        private final String code;
        private final String name;

        Redis(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @SneakyThrows
        public static Redis get(String code) {
            return Arrays.stream(Redis.values())
                    .filter(value -> code.equals(value.getCode()))
                    .findFirst()
                    .orElseThrow(() -> new Exception("Code invalid " + code));
        }
    }

    @Getter
    public enum Mongodb {
        STANDALONE("standalone", "Standalone"),
        REPLICA_SET("replica_set", "Replica Set");

        private final String code;
        private final String name;

        Mongodb(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @SneakyThrows
        public static Mongodb get(String code) {
            return Arrays.stream(Mongodb.values())
                    .filter(value -> code.equals(value.getCode()))
                    .findFirst()
                    .orElseThrow(() -> new Exception("Code invalid " + code));
        }
    }

    @Getter
    public enum Kafka {
        SINGLE_NODE("single_node", "Single-node"),
        CLUSTER("cluster", "Cluster");

        private final String code;
        private final String name;

        Kafka(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @SneakyThrows
        public static Kafka get(String code) {
            return Arrays.stream(Kafka.values())
                    .filter(value -> code.equals(value.getCode()))
                    .findFirst()
                    .orElseThrow(() -> new Exception("Code invalid " + code));
        }
    }

    @Getter
    public enum ApiGateway {
        STANDALONE("standalone", "Standalone");

        private final String code;
        private final String name;

        ApiGateway(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @SneakyThrows
        public static ApiGateway get(String code) {
            return Arrays.stream(ApiGateway.values())
                    .filter(value -> code.equals(value.getCode()))
                    .findFirst()
                    .orElseThrow(() -> new Exception("Code invalid " + code));
        }
    }

    @Getter
    public enum Postgresql {
        STANDALONE("standalone", "Standalone");
//        MASTER_SLAVE("master_slave");

        private final String code;
        private final String name;

        Postgresql(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @SneakyThrows
        public static Postgresql get(String code) {
            return Arrays.stream(Postgresql.values())
                    .filter(value -> code.equals(value.getCode()))
                    .findFirst()
                    .orElseThrow(() -> new Exception("Code invalid " + code));
        }
    }

}
