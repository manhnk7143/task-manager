package com.ctel.dbaas.utils;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.ctel.dbaas.config.EnvConfig;
import com.ctel.dbaas.dto.backup.S3StorageConfigDto;

import java.util.Date;

public class S3Utils {

    public static AmazonS3 buildAmazonS3(S3StorageConfigDto s3Config) {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        s3Config.getEndpoint(), s3Config.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                        s3Config.getAccessKey(), s3Config.getSecretKey())
                ))
                .withPathStyleAccessEnabled(true)
                .build();
    }

    public static String genPresignedUrl(AmazonS3 s3, HttpMethod method, String objectKey) {
        Date expiration = new Date(System.currentTimeMillis() + EnvConfig.S3_PRESINGED_EXPIRE_HOUR * 60 * 60 * 1000);
        GeneratePresignedUrlRequest putUrlRequest = new GeneratePresignedUrlRequest(EnvConfig.S3_BUCKET_NAME, objectKey)
                .withMethod(method)
                .withExpiration(expiration);
        return s3.generatePresignedUrl(putUrlRequest).toString();
    }

    public static String genPresignedUrl(S3StorageConfigDto s3Config, HttpMethod method, String orgId, String fileName) {
        AmazonS3 s3 = buildAmazonS3(s3Config);
        String bucketName = EnvConfig.S3_BUCKET_NAME + "/" + orgId;

        Date expiration = new Date(System.currentTimeMillis() + EnvConfig.S3_PRESINGED_EXPIRE_HOUR * 60 * 60 * 1000);
        GeneratePresignedUrlRequest putUrlRequest = new GeneratePresignedUrlRequest(bucketName, fileName)
                .withMethod(method)
                .withExpiration(expiration);
        return s3.generatePresignedUrl(putUrlRequest).toString();
    }

}
