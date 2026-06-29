/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.utils;

import lombok.SneakyThrows;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hieutrinh
 */
public class NetworkUtil {

    private static final Logger LOGGER = Logger.getLogger(NetworkUtil.class);
    private static final ConcurrentHashMap<Integer, CloseableHttpClient> MAP_HTTP_CLIENTS = new ConcurrentHashMap<>();

    public static String httpPost(String url, JSONObject json) throws Exception {

        int index = 0;
        if (url != null) {
            index = Math.abs(url.hashCode()) % 10;
        }
        CloseableHttpClient httpClient = MAP_HTTP_CLIENTS.get(index);
        if (httpClient == null) {
            LOGGER.info("Create httpclient, index = " + index);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(1000)
                    .setConnectTimeout(1000)
                    .setSocketTimeout(1000)
                    .build();
            httpClient = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).setDefaultRequestConfig(requestConfig).build();
            MAP_HTTP_CLIENTS.put(index, httpClient);
        }
        String content = "";
        HttpPost postRequest = new HttpPost(url);
        postRequest.setEntity(new StringEntity(json.toString()));
        postRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        CloseableHttpResponse httpResponse = httpClient.execute(postRequest);
        content = EntityUtils.toString(httpResponse.getEntity());
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        System.out.println("statusCode = " + statusCode);
        System.out.println("content = " + content);
        httpResponse.close();
        return content;
    }

    @SneakyThrows
    public static ResponseEntity<String> httpPost(String url, Map<String, String> mapHeaders, String bodyRequest) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(1).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofSeconds(6).toMillis());
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        for (Map.Entry<String, String> entry : mapHeaders.entrySet()) {
            httpHeaders.add(entry.getKey(), entry.getValue());
        }
        HttpEntity<?> httpEntity = new HttpEntity<>(bodyRequest, httpHeaders);

//        log.info("POST =====> REQUEST[{}] => data[{}]", url, httpEntity);
        ResponseEntity<String> response = restTemplate.postForEntity(url, httpEntity, String.class);
//        log.info("POST =====> RESPONSE[{}] => data[{}]", url, response);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.warn("CALL " + url + " error - response: {}" + response);
        }

        return response;
    }

    public static String getCurrentPublicIp(String url) throws IOException, NoSuchAlgorithmException, KeyManagementException {

        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        URL checkIpUrl = new URL(url);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                checkIpUrl.openStream()));
        String ip = in.readLine(); //you get the IP as a String
        return ip;
    }

    public static String getListAsteriskServer(String url) throws IOException, NoSuchAlgorithmException, KeyManagementException {

        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        URL checkIpUrl = new URL(url);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                checkIpUrl.openStream()));
        String ip = in.readLine(); //you get the IP as a String
        return ip;
    }

    public static boolean advanceRouteNoWait(String url, JSONObject json) throws Exception {

        int index = 0;
        if (url != null) {
            index = Math.abs(url.hashCode()) % 10;
        }
        CloseableHttpClient httpClient = MAP_HTTP_CLIENTS.get(index);
        if (httpClient == null) {
            LOGGER.info("Create httpclient, index = " + index);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(1000)
                    .setConnectTimeout(1000)
                    .setSocketTimeout(1000)
                    .build();
            httpClient = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).setDefaultRequestConfig(requestConfig).build();
        }
        HttpPost postRequest = new HttpPost(url);
        postRequest.setEntity(new StringEntity(json.toString()));
        postRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        httpClient.execute(postRequest, new ResponseHandler<Object>() {
            @Override
            public Object handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {

                String content = EntityUtils.toString(httpResponse.getEntity());
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                System.out.println("statusCode = " + statusCode);
                System.out.println("content = " + content);
                return null;
            }
        });
        return true;
    }

    private boolean isValidEndpoint(String endpoint) {

        if (endpoint.length() < 1 || endpoint.length() > 253) {
            return false;
        }
        for (String label : endpoint.split("\\.")) {
            if (label.length() < 1 || label.length() > 63) {
                return false;
            }
            if (!(label.matches("^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?$"))) {
                return false;
            }
        }
        return true;
    }

    public static String parseIpAddress(String strIpAddress) {

        String ipAddress = "";
        String[] str = strIpAddress.substring(1).split(":");
        if (str.length > 1) {
            ipAddress = str[0];
        }
        return ipAddress;
    }
}
