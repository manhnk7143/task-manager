package com.dev.dbaas.common;

import com.dev.dbaas.config.Config;
import lombok.Getter;

import java.util.List;

@Getter
public class RegionResource {
    private String keypair;
    private String networkMng;
    private String securityGroupMng;
    private String subnetMng;
    private String dockerRegistry;
    private List<Integer> portDefault;
    private final String monitorResourceType;

    public RegionResource(String region, String datastoreCode) {
        DatastoreSupport datastoreSupport = DatastoreSupport.getOrThrow(datastoreCode);
        this.monitorResourceType = datastoreSupport.getMonitorResourceTypeName();
        switch (datastoreSupport) {
            case REDIS: {
                this.portDefault = List.of(6379);
                break;
            }
            case MONGODB: {
                this.portDefault = List.of(27017);
                break;
            }
            case POSTGRESQL: {
                this.portDefault = List.of(5432);
                break;
            }
            case KAFKA: {
                this.portDefault = List.of(9092);
                break;
            }
            case MYSQL: {
                this.portDefault = List.of(3306);
                break;
            }
            case API_GATEWAY: {
                this.portDefault = List.of(8000);
                break;
            }
        }

        if (Region.HN1.getName().equals(region)) {
            this.dockerRegistry = Config.docker_registry_hn1;
            switch (datastoreSupport) {
                case REDIS:
                case MONGODB: {
                    this.keypair = Config.openstack_hn_keypair_name;
                    this.networkMng = Config.openstack_hn_network_id_manager;
                    this.securityGroupMng = Config.openstack_hn_security_group_id_manager;
                    this.subnetMng = Config.openstack_hn_subnet_id_manager;
                    break;
                }
                case KAFKA: {
                    this.keypair = Config.openstack_hn1_kafka_keypair_name;
                    this.networkMng = Config.openstack_hn1_kafka_network_id_manager;
                    this.securityGroupMng = Config.openstack_hn1_kafka_security_group_id_manager;
                    this.subnetMng = Config.openstack_hn1_kafka_subnet_id_manager;
                    break;
                }
                case MYSQL: {
                    this.keypair = Config.openstack_hn1_mysql_keypair_name;
                    this.networkMng = Config.openstack_hn1_mysql_network_id_manager;
                    this.securityGroupMng = Config.openstack_hn1_mysql_security_group_id_manager;
                    this.subnetMng = Config.openstack_hn1_mysql_subnet_id_manager;
                }
            }
        } else if (Region.HCM1.getName().equals(region)) {
            this.dockerRegistry = Config.docker_registry_hcm1;
            switch (datastoreSupport) {
                case REDIS:
                case MONGODB: {
                    this.keypair = Config.openstack_hcm_keypair_name;
                    this.networkMng = Config.openstack_hcm_network_id_manager;
                    this.securityGroupMng = Config.openstack_hcm_security_group_id_manager;
                    this.subnetMng = Config.openstack_hcm_subnet_id_manager;
                    break;
                }
                case KAFKA: {
                    this.keypair = Config.openstack_hcm1_kafka_keypair_name;
                    this.networkMng = Config.openstack_hcm1_kafka_network_id_manager;
                    this.securityGroupMng = Config.openstack_hcm1_kafka_security_group_id_manager;
                    this.subnetMng = Config.openstack_hcm1_kafka_subnet_id_manager;
                    break;
                }
                case MYSQL: {
                    this.keypair = Config.openstack_hcm1_mysql_keypair_name;
                    this.networkMng = Config.openstack_hcm1_mysql_network_id_manager;
                    this.securityGroupMng = Config.openstack_hcm1_mysql_security_group_id_manager;
                    this.subnetMng = Config.openstack_hcm1_mysql_subnet_id_manager;
                    break;
                }
            }
        }
    }
}
