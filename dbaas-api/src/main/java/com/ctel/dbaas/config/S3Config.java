package com.ctel.dbaas.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class S3Config {

    @Bean
    public AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        EnvConfig.S3_ENDPOINT, EnvConfig.S3_REGION))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                        Objects.requireNonNull(EnvConfig.S3_ACCESS_KEY),
                        Objects.requireNonNull(EnvConfig.S3_SECRET_KEY)
                )))
                .withPathStyleAccessEnabled(true)
                .build();
    }

}
