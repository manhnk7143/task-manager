package com.ctel.dbaas.common.enums;

import lombok.Getter;

public class Role {

    @Getter
    public enum Redis {
        MASTER("master"),
        SLAVE("slave");

        private final String name;

        Redis(String name) {
            this.name = name;
        }
    }

    @Getter
    public enum MongoDB {
        STANDALONE("standalone"),
        PRIMARY("primary"),
        SECONDARY("secondary"),
        ARBITER("arbiter");

        private final String name;

        MongoDB(String name) {
            this.name = name;
        }
    }

    @Getter
    public enum Kafka {
        BROKER("broker");

        private final String name;

        Kafka(String name) {
            this.name = name;
        }
    }

}
