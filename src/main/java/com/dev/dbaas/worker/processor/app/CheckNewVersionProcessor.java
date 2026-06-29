package com.dev.dbaas.worker.processor.app;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.dev.dbaas.common.ProcessorBase;
import com.dev.dbaas.config.Config;
import com.dev.dbaas.database.enties.*;
import com.dev.dbaas.database.enties.TbAgent;
import com.dev.dbaas.database.enties.TbAgentFirmware;
import com.dev.dbaas.entity.Account;
import com.dev.dbaas.manager.*;
import com.dev.dbaas.manager.AgentFirmwareManager;
import com.dev.dbaas.manager.AgentManager;
import com.dev.dbaas.packet.AppServiceType;
import com.dev.dbaas.protocol.AppPacket;
import com.dev.dbaas.protocol.ResponsePacket;
import com.dev.dbaas.utils.security.AESAuth;
import com.dev.dbaas.worker.job.AppJob;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CheckNewVersionProcessor implements ProcessorBase<AppJob> {

    private static final Logger LOGGER = Logger.getLogger(CheckNewVersionProcessor.class);

    private static final int SERVER_INTERNAL_ERROR = -100;
    private static final int SUCCESSFUL = 1;
    private static final int REQUIRE_NAMESPACE = -5;
    private static final int REQUIRE_BUILD_NUMBER = -6;
    private static final int NOT_FOUND_GROUP = -7;
    private static final int REQUIRE_ACCOUNT_ID = -8;
    private static final int NOT_FOUND_ACCOUNT_MEMBER = -9;
    private static final int PERMISSION_DENIED = -99;

    @Override
    public boolean process(AppJob job) throws Exception {

//        LOGGER.info("[4CMS] " + job.getPacket().getData());

        String jobId = "JobId : " + job.getId();
//        LOGGER.info(jobId);

        Account account = job.getAccount();
        AppPacket packet = job.getPacket();

        ResponsePacket responsePacket = new ResponsePacket();
        responsePacket.setServiceId(AppServiceType.CHECK_NEW_VERSION);

        int result = SERVER_INTERNAL_ERROR;

        packet.decodePacket();
        int buildNumber = packet.optIntField("buildNumber", 0);

        boolean isPass = true;
        if (buildNumber < 0) {
            result = REQUIRE_BUILD_NUMBER;
            isPass = false;
        }
        if (isPass) {

//            LOGGER.info("InstanceId : "+account.getInstanceId()+" - agentId : "+account.getAgentId()+" - "+buildNumber+" - "+account.getAgentFirmwareId());

            TbAgent agent = AgentManager.findById(account.getAgentId());
            TbAgentFirmware agentFirmware = AgentFirmwareManager.findById(agent.getAgentFirmwareId());
            if(agentFirmware.getBuildNumber() > buildNumber){
                LOGGER.info(agentFirmware.getBuildNumber() +" - "+buildNumber+" - "+agentFirmware.getConfiguration()+" - "+Config.secretkey_configuration_agent_firmware);

                String configuration = AESAuth.decryptAesCbc(agentFirmware.getConfiguration(), Config.secretkey_configuration_agent_firmware);
                JSONObject configurationJson = new JSONObject(configuration);
                String endpoint = configurationJson.optString("endPoint");
                String region = configurationJson.optString("region");
                String accessKey = configurationJson.optString("accessKey");
                String secretKey = configurationJson.optString("secretKey");
                String bucketName = configurationJson.optString("bucketName");
                String type = configurationJson.optString("type");
                LOGGER.info(configuration);

                AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                        .withPathStyleAccessEnabled(true)
                        .build();

                try {
                    Date expiration = new Date();
                    long expTimeMillis = Instant.now().toEpochMilli();
                    expTimeMillis += 6000000;
                    expiration.setTime(expTimeMillis);

                    GeneratePresignedUrlRequest generatePresignedUrlRequest =
                            new GeneratePresignedUrlRequest(bucketName, agentFirmware.getFilename())
                                    .withMethod(HttpMethod.GET)
                                    .withExpiration(expiration);

                    URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
//                    LOGGER.info("Pre-Signed URL: " + url.toString());

                    JSONObject dataResponse = new JSONObject();
                    dataResponse.put("url", url.toString());
                    dataResponse.put("buildNumber", agentFirmware.getBuildNumber());
                    responsePacket.setData(dataResponse.toString());

                } catch (Exception e) {
                    LOGGER.error(e, e);
                } finally {
                    s3Client.shutdown();
                }
            }
            result = SUCCESSFUL;
        }

//        LOGGER.info("Result : " + result);
        responsePacket.setResult(result);
        responsePacket.setMessage("");
        responsePacket.setMessageId(packet.getMessageId());

//        LOGGER.info("Clients: "+account.getClients().size());
        account.receivePacket(responsePacket);
        return true;
    }
}
