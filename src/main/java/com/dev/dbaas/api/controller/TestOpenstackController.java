package com.dev.dbaas.api.controller;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/test-openstack")
public class TestOpenstackController {

    @PostMapping
    public Object testOpenstack(@RequestBody Info info) {
        log.info("info : {}", info);
        OSClient.OSClientV3 instance = OSFactory.builderV3()
                .endpoint(info.getEndpoint())
                .credentials(info.getUsername(), info.getPassword(), Identifier.byName(info.getDomain()))
                .scopeToProject(Identifier.byId(info.getProjectId()), Identifier.byName(info.getDomain()))
                .authenticate();
        if (instance == null) {
            return "xxxx";
        }

        return instance.getToken();
    }

    @Data
    public static class Info {
        private String endpoint;
        private String username;
        private String password;
        private String domain;
        private String projectId;
    }

}
