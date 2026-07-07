package com.ctel.dbaas.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.ctel.dbaas.entity.dbaas.AgentFirmwareEntity;
import com.ctel.dbaas.utils.S3Utils;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.utils.CryptoUtils;
import com.ctel.dbaas.dto.agent_firmware.AgentS3Config;
import com.ctel.dbaas.dto.agent_firmware.CreateAgentFirmwareReq;
import com.ctel.dbaas.dto.backup.S3StorageConfigDto;
import com.ctel.dbaas.dto.common.ErrorResponse;
import com.ctel.dbaas.exception.AppException;
import com.ctel.dbaas.repository.dbaas.AgentFirmwareRepository;
import com.ctel.dbaas.utils.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Log4j2
@Service
public class AgentFirmwareService {

    @Autowired
    private AgentFirmwareRepository agentFirmwareRepository;

    public void addAgentFirmware(CreateAgentFirmwareReq req) {
        AgentFirmwareEntity agentFirmware = new AgentFirmwareEntity();
        agentFirmware.setBuildNumber(Integer.valueOf(req.getAgentVersion()));
        agentFirmware.setOsSupport(req.getOsSupport());
        agentFirmware.setConfiguration(req.getConfiguration().replaceAll(" ", ""));
        agentFirmware.setState(req.getState()); // AgentS3Config
        agentFirmware.setFileName(req.getObjectKey());

        String jsonS3Config = CryptoUtils.decryptAesCbc(agentFirmware.getConfiguration(),
                "DIlaYY3T3kHgGLtUizo3JyjgCfz1Qumu");
        if (jsonS3Config == null) {
            throw new AppException(new ErrorResponse("decryptAesCbc error"));
        }

        Map<String, Object> mapS3Config = CommonUtils.toMap(jsonS3Config);
        AgentS3Config s3Config = new ObjectMapper().convertValue(mapS3Config, AgentS3Config.class);
        S3StorageConfigDto s3 = new S3StorageConfigDto(s3Config.getEndPoint(), s3Config.getRegion(),
                s3Config.getAccessKey(), s3Config.getSecretKey());

        agentFirmware.setLinkDownload(this.genPresignedUrl(s3, HttpMethod.GET, s3Config.getBucketName(), agentFirmware.getFileName()));
        agentFirmwareRepository.save(agentFirmware);
    }

    public String genPresignedUrl(S3StorageConfigDto s3Config, HttpMethod method, String bucketName, String objectKey) {
        AmazonS3 s3 = S3Utils.buildAmazonS3(s3Config);
        Date expiration = new Date(System.currentTimeMillis() + EnvConfig.S3_PRESINGED_EXPIRE_HOUR * 60 * 60 * 1000);
        GeneratePresignedUrlRequest putUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey)
                .withMethod(method)
                .withExpiration(expiration);
        return s3.generatePresignedUrl(putUrlRequest).toString();
    }

    public AgentFirmwareEntity getAgentFirmwareLatest() {
        return agentFirmwareRepository.findFirstByOrderByBuildNumberDesc();
    }

}
