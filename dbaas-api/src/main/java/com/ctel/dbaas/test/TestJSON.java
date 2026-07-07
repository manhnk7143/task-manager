package com.ctel.dbaas.test;

import com.ctel.dbaas.common.context.OSContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.openstack.internal.OSClientSession;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Log4j2
public class TestJSON {

    @SneakyThrows
    public static void main(String[] args) {
//        String jsonData = """
//                {"data":"{\\"instanceId\\":\\"fdcea368-3898-4cbe-88a4-fd4ff759a71f\\",\\"userRequest\\":\\"v5xwnfab2hgt_manhnk\\",\\"regionId\\":\\"dev\\",\\"securityGroupIds\\":[\\"12d7fe5c-3a54-4278-84ef-6cb04eb71998\\"],\\"datastore\\":\\"mongodb\\",\\"datastoreModeCode\\":\\"standalone\\",\\"projectId\\":\\"f79a94006fd24746b48f87af304b394d\\",\\"orgId\\":\\"v5xwnfab2hgt\\"}","messageId":"c2f2656b-9000-4161-982c-0fa6087557f2","time":1715052924754,"serviceId":"attach_security_group"}
//                """;
//        String jsonData = """
//                {"data":"{\\"instanceId\\":\\"fdcea368-3898-4cbe-88a4-fd4ff759a71f\\",\\"userRequest\\":\\"v5xwnfab2hgt_manhnk\\",\\"regionId\\":\\"dev\\",\\"securityGroupIds\\":[],\\"datastore\\":\\"mongodb\\",\\"datastoreModeCode\\":\\"standalone\\",\\"projectId\\":\\"f79a94006fd24746b48f87af304b394d\\",\\"orgId\\":\\"v5xwnfab2hgt\\"}","messageId":"2b8adcfa-5f42-4ac3-b0f5-66853205fb7e","time":1715058299450,"serviceId":"attach_security_group"}
//                """;
//
//        JSONObject json = new JSONObject(jsonData);
//        JSONObject data =  new JSONObject(json.getString("data"));
//        JSONArray lstSg = data.getJSONArray("securityGroupIds");
//
//        List<String> xxx = new ObjectMapper().readValue(lstSg.toString(), new TypeReference<>() {});
//
//        System.out.println(lstSg);


        List<String> lstIds = new ArrayList<>();
        lstIds.add("4");
        lstIds.add("5");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put("1");
        jsonArray.put("2");
        jsonArray.put("3");
        jsonArray.put("3");

        Set<String> securityGroupIds = new ObjectMapper().readValue(jsonArray.toString(), new TypeReference<>() {});
        securityGroupIds.remove("3");
        System.out.println("set : " + securityGroupIds);


//        List<String> lstSecurityGroupIds = new ObjectMapper().readValue(jsonArray.toString(), new TypeReference<>() {});
//        System.out.println("list" + lstSecurityGroupIds);

//        System.out.println(jsonArray.put);

//        jsonArray..put(new JSONArray(lstIds));


//        List<String> ids = List.of(
//                "12d7fe5c-3a54-4278-84ef-6cb04eb70a89",
//                "4a744ce7-1187-4fef-8a73-d0d0f6891599",
//                "8d700c3b-ca6d-4af3-b18a-86d0012292f1"
//        );
//        JSONObject securityGroups = new JSONObject();
//        securityGroups.put("security_groups", new JSONArray(ids));
//
//        JSONObject portReq = new JSONObject();
//        portReq.put("port", securityGroups);
//
//        System.out.println(portReq);
//
//
//        OSClient.OSClientV3 os = OSContext.getInstance().getClient();
//        String url = ((OSClientSession.OSClientSessionV3) os).getEndpoint(ServiceType.NETWORK) + "/v2.0/ports/{portId}";
//
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
//        httpHeaders.set("X-Auth-Token", os.getToken().getId());
//
//        RestTemplate restTemplate = new RestTemplate();
//        HttpEntity<?> httpEntity = new HttpEntity<>(portReq.toString(), httpHeaders);
//
//        log.info("POST =====> REQUEST[{}] => data[{}]", url, httpEntity);
//        ResponseEntity<?> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, Object.class, Collections.singletonMap("portId", "edd58ca9-174c-4f85-8516-8caa4a21d75d"));
//        log.info("POST =====> RESPONSE[{}] => data[{}]", url, response);
    }

}
