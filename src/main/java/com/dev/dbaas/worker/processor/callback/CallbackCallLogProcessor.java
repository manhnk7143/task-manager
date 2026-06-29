/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dev.dbaas.worker.processor.callback;

import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.worker.job.CallbackJob;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;

/**
 * @author hieutrinh
 */
public class CallbackCallLogProcessor implements ProcessorBase<CallbackJob> {

    private static final Logger LOGGER = Logger.getLogger(CallbackCallLogProcessor.class);
    private static final ConcurrentHashMap<String, CloseableHttpClient> MAP_HTTP_CLIENTS = new ConcurrentHashMap<>();

    @Override
    public boolean process(CallbackJob job) {

        String requestId = "request_"+System.currentTimeMillis();

        LOGGER.info(requestId+" - send report to " + job.getCallbackUrl());
        LOGGER.info("Body : " + job.getData().toString());
        int index = 0;

        if(job.getCallbackUrl().startsWith("http")){

            CloseableHttpClient httpClient = MAP_HTTP_CLIENTS.get(job.getCallbackUrl());
            if (httpClient == null) {
                LOGGER.info("Create httpclient, index = " + job.getCallbackUrl());
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectionRequestTimeout(2000)
                        .setConnectTimeout(2000)
                        .setSocketTimeout(2000)
                        .build();
                try {
                    httpClient = HttpClients.custom().
                            setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).
                            setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy()
                            {
                                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
                                {
                                    return true;
                                }
                            }).build()).setDefaultRequestConfig(requestConfig).build();
                } catch (Exception e) {
                    LOGGER.error(e,e);
                }
                MAP_HTTP_CLIENTS.put(job.getCallbackUrl(), httpClient);
            }

            try {
                HttpPost postRequest = new HttpPost(job.getCallbackUrl());
                postRequest.setEntity(new StringEntity(job.getData().toString()));
                postRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

                try{
                    httpClient.execute(postRequest, new ResponseHandler<Object>() {
                        @Override
                        public Object handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {

                            LOGGER.info(requestId+" - Response request");
                            String content = "";
                            if(httpResponse.getEntity() != null){
                                content = EntityUtils.toString(httpResponse.getEntity());
                            }
                            int statusCode = httpResponse.getStatusLine().getStatusCode();
                            LOGGER.info("statusCode = " + statusCode);
                            LOGGER.info("content = " + content);

                            if (content != null && content.length() > 300) {
                                content = content.substring(0, 300);
                            }
                            return null;
                        }
                    });
                }
                catch (Exception e){
                    LOGGER.error(e, e);
                    LOGGER.info(requestId+" - exception");
                }
                finally{
                    postRequest.releaseConnection();
                }
            } catch (Exception e) {
                LOGGER.error(e, e);
                LOGGER.info(requestId+" - exception");
            }
        }
        else if(job.getCallbackUrl().startsWith("rabbitmq")){

            String[] params = job.getCallbackUrl().split("/");
            if(params.length == 7){
                String connection = params[1];
                String port = params[2];
                String username = params[3];
                String password = params[4];
                String exchange = params[5];
                String routingKey = params[6];
                LOGGER.info("Send to rabbit routing, "+connection+" - "+port+" - "+username+" - "+password+" - "+exchange+" - "+routingKey);
                //MessagingConnection.getInstance(EngineType.RABBIT_MQ, connection, username, password, Integer.valueOf(port)).emmitRouting(exchange, routingKey,job.getData());
            }
        }
        return true;
    }
}
