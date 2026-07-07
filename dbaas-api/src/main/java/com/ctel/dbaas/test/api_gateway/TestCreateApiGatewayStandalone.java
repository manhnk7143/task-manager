package com.ctel.dbaas.test.api_gateway;

import com.ctel.dbaas.common.context.OSContext;
import com.ctel.dbaas.common.enums.ComputeStatus;
import com.ctel.dbaas.common.enums.Datastore;
import com.ctel.dbaas.common.enums.InstanceStatus;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.dto.openstack.CreatePortReq;
import com.ctel.dbaas.dto.openstack.CreatePortRes;
import com.ctel.dbaas.entity.dbaas.*;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.datastore.api_gateway.model.ApiGatewayRole;
import com.ctel.dbaas.repository.dbaas.*;
import com.ctel.dbaas.utils.CommonUtils;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.BDMDestType;
import org.openstack4j.model.compute.BDMSourceType;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.builder.ServerCreateBuilder;
import org.openstack4j.model.image.v2.Image;
import org.openstack4j.model.network.IP;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.network.SecurityGroup;
import org.openstack4j.model.network.builder.PortBuilder;
import org.openstack4j.model.storage.block.Volume;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Log4j2
@RestController
@RequestMapping("/test-api-gateway")
public class TestCreateApiGatewayStandalone {

    private static final int volumeSize = 10;
    private static final String flavorId = "d3";
    private static final String vpcClientId = "86f98d4c-4a4c-4859-af9c-d5b4e8ada9b6";
    private static final String vpcManagerId = "d26a7c74-bfa4-4723-898a-108526e0adcf";
    private static final String securityManagerId = "93d9ba7a-6a62-47b8-91e0-52616aa38aed";
    private static final String TEMPLATE_CONFIG_DB_API_GATEWAY = """
            [default]
            socket_url=https://ws-cloudops-taskmanager.api-connect.io:32286/
            datastore_manager=api_gateway
            datastore_version=3.6.1.1
            encrypted_key=${encrypted_key}
            agent_id=${agent_id}
                        
            [api_gateway]
            role=database
            mode=standalone
            db_port=${db_port}
            db_name=${db_name}
            db_username=${db_username}
            db_password=${db_password}
            """;

    @Autowired
    private DatastoreRepository datastoreRepository;
    @Autowired
    private DatastoreVersionRepository datastoreVersionRepository;
    @Autowired
    private DatastoreModeRepository datastoreModeRepository;
    @Autowired
    private InstanceRepository instanceRepository;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private ComputeRepository computeRepository;

    @SneakyThrows
    @PostMapping("/standalone")
    public Object createRedisStandalone(@RequestBody StandaloneCreate req) {
        log.info("TEST::Request create api gateway standalone => {}", req);

        OSClient.OSClientV3 os = OSContext.getInstance().getClient();
        List<? extends Image> images = os.imagesV2().list(Map.of("tag", "cmc-dbaas-agent"));
        if (images == null || images.isEmpty()) {
            throw new AppException(new ErrorResponse("Not found image with tag cmc-dbaas-agent"));
        }
        String imageId = images.get(0).getId();
        InstanceEntity instance = this.createInstanceStandalone(req);

        // init VM database
        ComputeDbInfo dbInfo = this.createComputeDatabase(instance.getId(), imageId, req);

        // create vip ip
        PortBuilder portVipBuilder = Builders.port()
                .name("portVip_api_gateway_" + req.getInstanceName())
                .networkId(vpcManagerId)
                .securityGroup(securityManagerId);
        Port portVip = os.networking().port().create(portVipBuilder.build());
        String ipV4Vip = getIpV4(portVip.getFixedIps());

        String cloudInitGateway = """
                [default]
                socket_url=https://ws-cloudops-taskmanager.api-connect.io:32286/
                datastore_manager=api_gateway
                datastore_version=3.6.1.1
                encrypted_key=${encrypted_key}
                agent_id=${agent_id}
                ip_address=${ip_address}
                                
                [api_gateway]
                role=api_gateway
                mode=standalone
                db_host=${db_host}
                db_port=${db_port}
                db_name=${db_name}
                db_username=${db_username}
                
                
                db_password=${db_password}
                """
                .replace("${db_host}", dbInfo.getHost())
                .replace("${db_port}", dbInfo.getPort())
                .replace("${db_name}", dbInfo.getDbName())
                .replace("${db_username}", dbInfo.getUsername())
                .replace("${db_password}", dbInfo.getPassword());

        String keepalivedConfig1 = """
                vrrp_instance VI_1 {
                    state MASTER
                    interface ens3   # Giao diện mạng kết nối với mạng LAN
                    virtual_router_id 1
                    priority 100       # Ưu tiên của server B, thấp hơn server A
                    advert_int 1      # Khoảng thời gian giữa các gói tin VRRP multicast
                                
                    virtual_ipaddress {
                        ${vip_ip}    # Địa chỉ IP ảo
                    }
                }
                """.replace("${vip_ip}", ipV4Vip);
        this.createComputeGateway(instance.getId(), imageId, req, ipV4Vip, portVip.getMacAddress(), cloudInitGateway, keepalivedConfig1);

        String keepalivedConfig2 = """
                vrrp_instance VI_1 {
                    state BACKUP
                    interface ens3   # Giao diện mạng kết nối với mạng LAN
                    virtual_router_id 1
                    priority 90       # Ưu tiên của server B, thấp hơn server A
                    advert_int 1      # Khoảng thời gian giữa các gói tin VRRP multicast
                                
                    virtual_ipaddress {
                        ${vip_ip}    # Địa chỉ IP ảo
                    }
                }
                """.replace("${vip_ip}", ipV4Vip);
        this.createComputeGateway(instance.getId(), imageId, req, ipV4Vip, portVip.getMacAddress(), cloudInitGateway, keepalivedConfig2);

        return "OK";
    }

    private InstanceEntity createInstanceStandalone(StandaloneCreate req) {
        DatastoreEntity datastore = datastoreRepository.findFirstByName(Datastore.API_GATEWAY.getDatastoreName()).orElse(null);
        if (datastore == null) {
            throw new AppException(new ErrorResponse("datastore api_gateway not exist"));
        }

        DatastoreVersionEntity datastoreVersion = datastoreVersionRepository.findFirstByDatastoreIdAndVersion(datastore.getId(), "1.0").orElse(null);
        if (datastoreVersion == null) {
            throw new AppException(new ErrorResponse("version 1.0 of api_gateway not exist"));
        }

        DatastoreModeEntity datastoreMode = datastoreModeRepository.findFirstByNameAndDatastoreVersionId(
                "Standalone", datastoreVersion.getId()).orElse(null);
        if (datastoreMode == null) {
            throw new AppException(new ErrorResponse("mode standalone of api_gateway not exist"));
        }

        InstanceEntity instance = new InstanceEntity();
        instance.setName(req.getInstanceName());
        instance.setDatastoreId(datastore.getId());
        instance.setDatastoreVersionId(datastoreVersion.getId());
        instance.setDatastoreModeId(datastoreMode.getId());
        instance.setGroupConfigurationId(null);
        instance.setResourcePackage("{}");
        instance.setStatus(InstanceStatus.WAITING.getStatus());
        instance.setMessage("");
//        instance.setZoneName("nova");
        instance.setProjectId(req.getProjectId());
        instance.setOrgId(req.getOrgId());
        instance.setRegionId(req.getRegionId());
        instanceRepository.save(instance);

        return instance;
    }

    @SneakyThrows
    private ComputeDbInfo createComputeDatabase(String instanceId, String imageId, StandaloneCreate req) {
        String vmName = "DB_APIG_" + req.getInstanceName();

        ComputeEntity compute = new ComputeEntity();
        compute.setFlavorId(flavorId);
        compute.setRole(ApiGatewayRole.DATABASE.getName());
        compute.setStatus(ComputeStatus.BUILDING.getName());
        compute.setInstanceId(instanceId);
        compute.setProjectId(req.getProjectId());
        compute.setOrgId(req.getOrgId());
        compute.setRegionId(req.getRegionId());
//        compute.setStatusAgentMonitor("NONE");
        computeRepository.save(compute);

        AgentEntity agent = new AgentEntity();
        agent.setName("compute-db-" + compute.getId());
        agent.setEncryptedKey(CommonUtils.generateString(32));
        agent.setAgentFirmwareId(null);
        agent.setAgentVersion(String.valueOf(0));
        agent.setInstanceId(instanceId);
        agent.setComputeId(compute.getId());
        agent.setProjectId(req.getProjectId());
        agent.setOrgId(req.getOrgId());
        agent.setStatus("NONE");
        agentRepository.save(agent);

        OSClient.OSClientV3 os = OSContext.getInstance().getClient();
        Volume volume = os.blockStorage().volumes()
                .create(Builders.volume()
                        .name("volume_" + req.getInstanceName())
                        .size(volumeSize)
                        .description("datastore volume for " + vmName)
                        .build()
                );
        TimeUnit.SECONDS.sleep(2);
        String securityGroupId = this.createSecurityGroup(os, vmName + "_sg_db_gateway", instanceId, List.of(5432));

        CreatePortRes portNetworkPrivate = this.createPort(os, CreatePortReq.builder()
                .name("port_APIG_user_db_" + vmName)
                .securityGroupId(List.of(securityGroupId))
                .vpcId(vpcClientId)
                .build());

        CreatePortRes portTroveMgmt = this.createPort(os, CreatePortReq.builder()
                .name("port_APIG_mgmt_db_" + vmName)
                .securityGroupId(List.of(securityManagerId, securityGroupId)) // sg_id for trove-mgmt
                .vpcId(vpcManagerId) // network trove-mgmt
                .build());

        String content = TEMPLATE_CONFIG_DB_API_GATEWAY
                .replace("${agent_id}", agent.getId())
                .replace("${encrypted_key}", agent.getEncryptedKey())

                .replace("${db_port}", "5432")
                .replace("${db_name}", "kong")
                .replace("${db_username}", "kong")
                .replace("${db_password}", "kongpass");

        String contentBase64 = Base64.getEncoder().encodeToString(content.getBytes());
        String cloudInit = """
                #cloud-config
                write_files:
                - encoding: b64
                  owner: ubuntu:root
                  path: /etc/cmc/conf.d/guest_info.conf
                  content: ${content}
                """.replace("${content}", contentBase64);

        ServerCreateBuilder sc = Builders.server()
                .name(vmName)
                .image(imageId)
                .flavor(flavorId)
                .userData(Base64.getEncoder().encodeToString(cloudInit.getBytes()))
                .keypairName("trove-mgmt")
                .availabilityZone("nova")
                .blockDevice(Builders.blockDeviceMapping()
                        .uuid(imageId)
                        .sourceType(BDMSourceType.IMAGE)
                        .destinationType(BDMDestType.LOCAL)
                        .bootIndex(0)
                        .deleteOnTermination(true)
                        .build()
                )
                .blockDevice(Builders.blockDeviceMapping()
                        .uuid(volume.getId())
                        .sourceType(BDMSourceType.VOLUME)
                        .destinationType(BDMDestType.VOLUME)
                        .deviceName("vdb")
                        .volumeSize(volumeSize)
                        .deleteOnTermination(true)
                        .build()
                );

        sc.addNetworkPort(portTroveMgmt.getPortId());
        sc.addNetworkPort(portNetworkPrivate.getPortId());

        Server server = os.compute().servers().boot(sc.build());
        System.out.println(server);
        System.out.println("====> Created " + vmName);

        ComputeDbInfo computeDbInfo = new ComputeDbInfo();
        computeDbInfo.setComputeId(compute.getId());
        computeDbInfo.setHost(portTroveMgmt.getIpAddress());
        computeDbInfo.setPort("5432");
        computeDbInfo.setDbName("postgres");
        computeDbInfo.setUsername("kong");
        computeDbInfo.setPassword("kongpass");

        return computeDbInfo;
    }

    @SneakyThrows
    private void createComputeGateway(String instanceId, String imageId, StandaloneCreate req, String ipVip, String macAddressVip, String agentConfig, String keepalivedConfig) {
        String vmName = "APIG_" + req.getInstanceName();

        ComputeEntity compute = new ComputeEntity();
        compute.setFlavorId(flavorId);
        compute.setRole(ApiGatewayRole.API_GATEWAY.getName());
        compute.setStatus(ComputeStatus.BUILDING.getName());
        compute.setInstanceId(instanceId);
        compute.setProjectId(req.getProjectId());
        compute.setOrgId(req.getOrgId());
        compute.setRegionId(req.getRegionId());
//        compute.setStatusAgentMonitor("NONE");
        computeRepository.save(compute);

        AgentEntity agent = new AgentEntity();
        agent.setName("compute-" + compute.getId());
        agent.setEncryptedKey(CommonUtils.generateString(32));
        agent.setAgentFirmwareId(null);
        agent.setAgentVersion(String.valueOf(0));
        agent.setInstanceId(instanceId);
        agent.setComputeId(compute.getId());
        agent.setProjectId(req.getProjectId());
        agent.setOrgId(req.getOrgId());
        agent.setStatus("NONE");
        agentRepository.save(agent);

        OSClient.OSClientV3 os = OSContext.getInstance().getClient();
        Volume volume = os.blockStorage().volumes()
                .create(Builders.volume()
                        .name("volume_" + req.getInstanceName())
                        .size(volumeSize)
                        .description("datastore volume for " + vmName)
                        .build()
                );
        TimeUnit.SECONDS.sleep(2);
        String securityGroupId = this.createSecurityGroup(os, vmName + "_sg_api_gateway", instanceId,
                List.of(8000, 8443, 8001, 8444, 8002, 8445, 8003, 8004));
//        String securityGroupId = this.createSecurityGroup(os, vmName + "_sg_api_gateway", instanceId,
//                List.of(8001));

        CreatePortRes portNetworkPrivate = this.createPort(os, CreatePortReq.builder()
                .name("port_user_APIG_" + vmName)
                .securityGroupId(List.of(securityGroupId))
                .vpcId(vpcClientId)
                .allowAddressPair(List.of(new CreatePortReq.AllowAddressPair(ipVip, macAddressVip)))
                .build());

        CreatePortRes portTroveMgmt = this.createPort(os, CreatePortReq.builder()
                .name("port_mgmt_APIG_" + vmName)
                .securityGroupId(List.of(securityManagerId, securityGroupId)) // sg_id for trove-mgmt
                .vpcId(vpcManagerId) // network trove-mgmt
                .allowAddressPair(List.of(new CreatePortReq.AllowAddressPair(ipVip, macAddressVip)))
                .build());

        String content = agentConfig
                .replace("${agent_id}", agent.getId())
                .replace("${encrypted_key}", agent.getEncryptedKey())
                .replace("${ip_address}", portTroveMgmt.getIpAddress());
        log.info("CLOUD INIT GATEWAY: {}", content);

        String cloudInit = """
                #cloud-config
                write_files:
                - encoding: b64
                  owner: ubuntu:root
                  path: /etc/cmc/conf.d/guest_info.conf
                  content: ${content}
                - encoding: b64
                  owner: root:root
                  path: /etc/keepalived/keepalived.conf
                  content: ${keepalived_config}
                """
                .replace("${content}", Base64.getEncoder().encodeToString(content.getBytes()))
                .replace("${keepalived_config}", Base64.getEncoder().encodeToString(keepalivedConfig.getBytes()));

        ServerCreateBuilder sc = Builders.server()
                .name(vmName)
                .image(imageId)
                .flavor(flavorId)
                .userData(Base64.getEncoder().encodeToString(cloudInit.getBytes()))
                .keypairName("trove-mgmt")
                .availabilityZone("nova")
                .blockDevice(Builders.blockDeviceMapping()
                        .uuid(imageId)
                        .sourceType(BDMSourceType.IMAGE)
                        .destinationType(BDMDestType.LOCAL)
                        .bootIndex(0)
                        .deleteOnTermination(true)
                        .build()
                )
                .blockDevice(Builders.blockDeviceMapping()
                        .uuid(volume.getId())
                        .sourceType(BDMSourceType.VOLUME)
                        .destinationType(BDMDestType.VOLUME)
                        .deviceName("vdb")
                        .volumeSize(volumeSize)
                        .deleteOnTermination(true)
                        .build()
                );

        sc.addNetworkPort(portTroveMgmt.getPortId());
        sc.addNetworkPort(portNetworkPrivate.getPortId());

        Server server = os.compute().servers().boot(sc.build());
        System.out.println(server);
        System.out.println("====> Created Gateway: " + vmName);
    }

    private String getIpV4(Set<? extends IP> fixedIps) {
        String ipv4Address = null;
        for (IP ip : fixedIps) {
            String ipAddress = ip.getIpAddress();
            if (InetAddressUtils.isIPv4Address(ipAddress)) {
                ipv4Address = ipAddress;
                break;
            }
        }

        return ipv4Address;
    }

    private CreatePortRes createPort(OSClient.OSClientV3 os, CreatePortReq portReq) {
        PortBuilder portBuilder = Builders.port()
                .name(portReq.getName())
                .networkId(portReq.getVpcId());

        for (String sgId : portReq.getSecurityGroupId()) {
            portBuilder.securityGroup(sgId);
        }

        if (portReq.getAllowAddressPair() != null && !portReq.getAllowAddressPair().isEmpty()) {
            for (CreatePortReq.AllowAddressPair vipInfo : portReq.getAllowAddressPair()) {
                portBuilder.allowedAddressPair(vipInfo.getIpAddress(), vipInfo.getMacAddress());
            }
        }

        if (StringUtils.isNotBlank(portReq.getSubnetId())) {
            portBuilder = portBuilder.fixedIp(StringUtils.isBlank(portReq.getIpAddress()) ? null :
                    Objects.requireNonNull(portReq.getIpAddress()), portReq.getSubnetId());
        }
        Port port = os.networking().port().create(portBuilder.build());
        Set<? extends IP> fixedIps = port.getFixedIps();
        String ipv4Address = null;
        for (IP ip : fixedIps) {
            String ipAddress = ip.getIpAddress();
            if (InetAddressUtils.isIPv4Address(ipAddress)) {
                ipv4Address = ipAddress;
                break;
            }
        }

        if (port.getId() == null || ipv4Address == null) {
            throw new AppException(new ErrorResponse("Create port failed"));
        }

        return new CreatePortRes(port.getId(), ipv4Address, port.getMacAddress());
    }

    public String createSecurityGroup(OSClient.OSClientV3 os, String name, String instanceId, List<Integer> ports) {
        SecurityGroup sg = os.networking().securitygroup().create(
                Builders.securityGroup()
                        .name(name)
                        .description("Security group for instance " + instanceId)
                        .build()
        );

        // create rule for security group
        for (int port : ports) {
            os.networking().securityrule().create(
                    Builders.securityGroupRule()
                            .direction("ingress")
                            .ethertype("IPv4")
                            .protocol("tcp")
                            .securityGroupId(sg.getId())
                            .portRangeMin(port)
                            .portRangeMax(port)
                            .remoteIpPrefix("0.0.0.0/0")
                            .build()
            );
        }

        return sg.getId();
    }

    @Getter
    @Setter
    public static class StandaloneCreate {
        private String instanceName;
        private String projectId;
        private String orgId;
        private String regionId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ComputeDbInfo {
        private String computeId;
        private String host;
        private String port;
        private String dbName;
        private String username;
        private String password;
    }

}
