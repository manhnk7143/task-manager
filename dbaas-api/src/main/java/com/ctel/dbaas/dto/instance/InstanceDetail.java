package com.ctel.dbaas.dto.instance;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class InstanceDetail {

    private String id;

    private String instanceName;

    private String datastoreName;

    private String datastoreVersion;

    private String datastoreMode;

    private String groupConfigId = "";

    private String securityClientIds;

    private String vpcId;

    private String subnetId;

    private String flavorId;

    private String vpcName = "";

    private String subnetName = "";

    private String flavorName = "";

    private int vCpu = 0;

    private int ram = 0;

    private int disk = 0;

    private Integer volumeSize = 0;

    private String dataDetail = "";

    private String status;

    private String created;

    private String updated;

    @Data
    public static class ComputeInstanceInfo {

        private String id;

        private String osServerId;

        private String role;

        private String ipAddress;

        private Integer vCpus;

        private Integer ram;

        private Integer disk;

        private Integer volumeSize;

        private String zoneName;

        private String status;

        private String monitorResourceId;
    }

    @Data
    @AllArgsConstructor
    public static class RedisStandalone {
        private ComputeInstanceInfo masterInfo;
    }

    @Data
    @AllArgsConstructor
    public static class RedisMasterSlave {
        private ComputeInstanceInfo masterInfo;
        private List<ComputeInstanceInfo> slavesInfo;
    }

    @Data
    @AllArgsConstructor
    public static class RedisCluster {
        private List<ComputeInstanceInfo> mastersInfo;
        private List<ComputeInstanceInfo> slavesInfo;
    }

    @Data
    @AllArgsConstructor
    public static class Mongodb {
        private List<ComputeInstanceInfo> serversInfo;
        private String statusArbiter;
    }

    @Data
    @AllArgsConstructor
    public static class Kafka {
        private List<ComputeInstanceInfo> serversInfo;
    }

}
