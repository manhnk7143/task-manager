package com.ctel.dbaas.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class RestfulUtils {

    public static ResponseEntity<?> post(String url, String bodyJson, String tokenAuth) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.isNotBlank(tokenAuth)) {
            httpHeaders.set("Authorization", tokenAuth);
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<?> httpEntity = new HttpEntity<>(bodyJson, httpHeaders);

        log.info("POST =====> REQUEST[{}] => data[{}]", url, httpEntity);
        ResponseEntity<?> response = restTemplate.postForEntity(url, httpEntity, HashMap.class);
        log.info("POST =====> RESPONSE[{}] => data[{}]", url, response);

        return response;
    }

    public static <T> ResponseEntity<T> post(String url, Map<String, String> mapHeaders, String bodyJson, Class<T> zClass) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        for (Map.Entry<String, String> entry : mapHeaders.entrySet()) {
            httpHeaders.add(entry.getKey(), entry.getValue());
        }
        HttpEntity<?> httpEntity = new HttpEntity<>(bodyJson, httpHeaders);

        log.info("POST =====> REQUEST[{}] => data[{}]", url, httpEntity);
        ResponseEntity<T> response = new RestTemplate().postForEntity(url, httpEntity, zClass);
        log.info("POST =====> RESPONSE[{}] => data[{}]", url, response);

        return response;
    }

    public static <T> ResponseEntity<T> get(String url, String tokenAuth, Class<T> zClass) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (tokenAuth != null) {
            headers.set("Authorization", tokenAuth);
        }
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        log.info("GET =====> REQUEST[{}] => httpEntity[{}]", url, httpEntity);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, zClass);
        log.info("GET =====> RESPONSE[{}] => [{}]", url, response);

        return response;
    }

    public static <T> ResponseEntity<T> get(String url, String tokenAuth, Map<String, String> mapHeaders, Class<T> zClass) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (tokenAuth != null) {
            headers.set("Authorization", tokenAuth);
        }
        for (Map.Entry<String, String> entry : mapHeaders.entrySet()) {
            headers.add(entry.getKey(), entry.getValue());
        }
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

//        log.info("GET =====> REQUEST[{}] => httpEntity[{}]", url, httpEntity);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, zClass);
//        log.info("GET =====> RESPONSE[{}] => [{}]", url, response);

        return response;
    }

    public static <T> ResponseEntity<T> get(String url, Map<String, String> mapHeaders, Class<T> zClass) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        for (Map.Entry<String, String> entry : mapHeaders.entrySet()) {
            headers.add(entry.getKey(), entry.getValue());
        }
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        log.info("GET =====> REQUEST[{}] => httpEntity[{}]", url, httpEntity);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, zClass);
        log.info("GET =====> RESPONSE[{}] => [{}]", url, response);

        return restTemplate.exchange(url, HttpMethod.GET, httpEntity, zClass);
    }

}
