package com.dev.dbaas.main;

import com.dev.dbaas.config.Config;
import com.dev.dbaas.manager.ClusterManager;

public class Test {
    public static void main(String[] args){

        try {
            Config.loadConfig();
            Config.rabbit_connection="redis.api-connect.io";
            Config.rabbit_port=5674;
            Config.rabbit_api_queue="dbaas.api";
            Config.rabbit_api_exchange="dbaas.api";
            Config.rabbit_client_key_password="aaaaaaaa";
            Config.rabbit_trust_key_password="aaaaaaaa";
            Config.rabbit_client_key_file="/projects/dev/task_manager/config/client.keystore.p12";
            Config.rabbit_trust_key_file="/projects/dev/task_manager/config/client.truststore.p12";

            ClusterManager.listenTopic(Config.rabbit_connection, Config.rabbit_port, Config.default_exchange_config, Config.default_queue_config, Config.rabbit_username, Config.rabbit_password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
