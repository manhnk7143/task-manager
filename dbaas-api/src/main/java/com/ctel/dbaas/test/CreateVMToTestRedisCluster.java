package com.ctel.dbaas.test;

import com.ctel.dbaas.common.context.OSContext;
import org.apache.http.conn.util.InetAddressUtils;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.builder.ServerCreateBuilder;
import org.openstack4j.model.network.IP;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.network.builder.PortBuilder;

import java.util.*;

public class CreateVMToTestRedisCluster {

    private static int numberOfVMs = 3;
    private static LinkedHashMap<String, Map<String, Object>> mapPort = new LinkedHashMap<>();
    private static Map<String, String> mapNetwork = new LinkedHashMap<>();

    static {
        Map<String, Object> network1 = new HashMap<>();
        network1.put("ip", "192.168.254.81");
        network1.put("ports", List.of("7001", "7004"));

        Map<String, Object> network2 = new HashMap<>();
        network2.put("ip", "192.168.254.7");
        network2.put("ports", List.of("7002", "7005"));

        Map<String, Object> network3 = new HashMap<>();
        network3.put("ip", "192.168.254.63");
        network3.put("ports", List.of("7003", "7006"));

        mapPort.put("redis-cluster-1", network1);
        mapPort.put("redis-cluster-2", network2);
        mapPort.put("redis-cluster-3", network3);
    }

    public static void main(String[] args) {
        OSClient.OSClientV3 os = OSContext.getInstance().getClient();

        PortBuilder port81 = Builders.port()
                .name("port_trove_192.168.254.81")
                .networkId("b0e1f853-9b6c-4659-b9d1-e41fb9f398c4") // trove
                .securityGroup("4a744ce7-1187-4fef-8a73-d0d0f6891599") // default
                .securityGroup("a90f3be8-f4a2-426e-bba2-23cee0c6ed18") // trove
                .fixedIp("192.168.254.81", "8dd0dea8-5828-4b52-b63e-adf4b2863456");

        PortBuilder port7 = Builders.port()
                .name("port_trove_192.168.254.7")
                .networkId("b0e1f853-9b6c-4659-b9d1-e41fb9f398c4") // trove
                .securityGroup("4a744ce7-1187-4fef-8a73-d0d0f6891599") // default
                .securityGroup("a90f3be8-f4a2-426e-bba2-23cee0c6ed18") // trove
                .fixedIp("192.168.254.7", "8dd0dea8-5828-4b52-b63e-adf4b2863456");

        PortBuilder port63 = Builders.port()
                .name("port_trove_192.168.254.63")
                .networkId("b0e1f853-9b6c-4659-b9d1-e41fb9f398c4") // trove
                .securityGroup("4a744ce7-1187-4fef-8a73-d0d0f6891599") // default
                .securityGroup("a90f3be8-f4a2-426e-bba2-23cee0c6ed18") // trove
                .fixedIp("192.168.254.63", "8dd0dea8-5828-4b52-b63e-adf4b2863456");
        Port portCluster1 = OSContext.getInstance().getClient().networking().port().create(port81.build());
        Port portCluster2 = OSContext.getInstance().getClient().networking().port().create(port7.build());
        Port portCluster3 = OSContext.getInstance().getClient().networking().port().create(port63.build());
        System.out.println("DONE");

        mapNetwork.put("redis-cluster-1", portCluster1.getId());
        mapNetwork.put("redis-cluster-2", portCluster2.getId());
        mapNetwork.put("redis-cluster-3", portCluster3.getId());

        for (int i = 1; i <= numberOfVMs; i++) {
            String name = "redis-cluster-" + i;
            String portId = mapNetwork.get(name);

            PortBuilder portBuilder = Builders.port()
                    .name("private_" + name)
                    .securityGroup("4a744ce7-1187-4fef-8a73-d0d0f6891599")
                    .securityGroup("a90f3be8-f4a2-426e-bba2-23cee0c6ed18")
                    .networkId("affaa3e8-e127-42f6-be72-bc8aa0d79e39");
            Port port = os.networking().port().create(portBuilder.build());

            ServerCreateBuilder sc = Builders.server()
                    .name(name)
                    .image("f86b5771-9a72-4ddb-ac11-3fdfb7c29f23")
                    .flavor("d2")
                    .keypairName("trove-mgmt")
                    .availabilityZone("nova")
                    .addNetworkPort(portId)
                    .addNetworkPort(port.getId());
            Server server = os.compute().servers().boot(sc.build());
            System.out.println("created server: " + name);
        }

        Map<String, Object> network1 = new HashMap<>();
        network1.put("ip", "192.168.254.81");
        network1.put("ports", List.of("7001", "7004"));

        Map<String, Object> network2 = new HashMap<>();
        network2.put("ip", "192.168.254.7");
        network2.put("ports", List.of("7002", "7005"));

        Map<String, Object> network3 = new HashMap<>();
        network3.put("ip", "192.168.254.63");
        network3.put("ports", List.of("7003", "7006"));

        mapPort.put("redis-cluster-1", network1);
        mapPort.put("redis-cluster-2", network2);
        mapPort.put("redis-cluster-3", network3);

        System.out.println(buildDockerFile());

    }

    public static String buildDockerFile() {
        String template = """
                docker run -d --name database \\
                 -p ${HOST_PORT_1}:${CONTAINER_PORT_1} \\
                 -p ${HOST_PORT_2}:${CONTAINER_PORT_2} \\
                 -e ALLOW_EMPTY_PASSWORD=yes \\
                 -e REDIS_PORT_NUMBER=${REDIS_PORT_NUMBER} \\
                 -e REDIS_CLUSTER_ANNOUNCE_PORT=${REDIS_PORT_NUMBER} \\
                 -e REDIS_CLUSTER_ANNOUNCE_IP=${HOST_IP_ADDRESS} \\
                 -e REDIS_CLUSTER_ANNOUNCE_BUS_PORT=${REDIS_CLUSTER_ANNOUNCE_BUS_PORT} \\
                 -e REDIS_CLUSTER_DYNAMIC_IPS=no \\
                 -e REDIS_NODES=${REDIS_NODES} \\
                 bitnami/redis-cluster:latest
                """;
        String REDIS_NODES = buildRedisNode();
        for (int i = 1; i <= numberOfVMs; i++) {
            String name = "redis-cluster-" + i;
            Map<String, Object> hostPortInfo = mapPort.get(name);
            String HOST_IP_ADDRESS = (String) hostPortInfo.get("ip");
            List<String> ports = (List<String>) hostPortInfo.get("ports");

            for (String port : ports) {
                String dockerFile = template
                        .replace("${HOST_PORT_1}", "1" + port)
                        .replace("${CONTAINER_PORT_1}", "1" + port)

                        .replace("${HOST_PORT_2}", port)
                        .replace("${CONTAINER_PORT_2}", port)

                        .replace("${REDIS_PORT_NUMBER}", port)
                        .replace("${HOST_IP_ADDRESS}", HOST_IP_ADDRESS)
                        .replace("${REDIS_CLUSTER_ANNOUNCE_BUS_PORT}", "1" + port)
                        .replace("${REDIS_NODES}", REDIS_NODES);
                System.out.println(dockerFile);
            }

        }
        return null;
    }

    private static String getIP(Port port) {
        Set<? extends IP> fixedIps = port.getFixedIps();
        for (IP ip : fixedIps) {
            String ipAddress = ip.getIpAddress();
            if (InetAddressUtils.isIPv4Address(ipAddress)) {
                return ipAddress;
            }
        }
        return null;
    }

    private static String buildRedisNode() {
        StringBuilder redisNode = new StringBuilder();
        int i = 1;
        boolean first = true;
        for (Map.Entry<String, Map<String, Object>> item : mapPort.entrySet()) {
            String name = "redis-cluster-" + i;
            Map<String, Object> ips = mapPort.get(name);

            String ipAddr = (String) ips.get("ip");
            List<String> ports = (List<String>) ips.get("ports");


            for(String port : ports) {
                if (!first) {
                    redisNode.append(",");
                }
                redisNode.append(ipAddr).append(":").append(port);
                first = false;
            }
            i++;
        }

        return redisNode.toString();
    }

}
