package com.ctel.dbaas.test;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.ctel.dbaas.utils.S3Utils;
import com.ctel.dbaas.utils.CryptoUtils;
import com.ctel.dbaas.dto.backup.S3StorageConfigDto;
import com.ctel.dbaas.utils.CommonUtils;
import lombok.SneakyThrows;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class TestGenerateS3Url {

    @SneakyThrows
    public static void main(String[] args) {
        String configuration = "4p1tJwyrrdwhe3jpgjXWe4v/y1GwwUhxbyo+kZFa/K9lUPqWaVuydOBuPTlh/ONPGYOk4bVfkFNuEKQP5U+4R9u8+dZZHUMOdJIz5avMAhMoEHaLz5c3/pre4cEiJehxar7BkjR6ftLLT1Jlm+f/m8/6gdNIB6e+Psvg8KlhbLMf1fJTlLaEFVcX17QvIGXXQXwCvKiZ9vWiVcfzyfukWg==";
        String configJsonDecode = CryptoUtils.decrypt(configuration, "mO7ljM8dvp1is0UkWZMFqcnrDiy6bH");
        S3StorageConfigDto storageConfigDto = CommonUtils.convert(new JSONObject(configJsonDecode), S3StorageConfigDto.class);

        String objectName = "v5xwnfab2hgt/mongodb_mongodb-standalon-backup-em-manhnk-create_20240424084506.tar.gz";

        AmazonS3 amazonS3 = S3Utils.buildAmazonS3(storageConfigDto);
        String result = S3Utils.genPresignedUrl(amazonS3, HttpMethod.GET, objectName);
        System.out.println(URLDecoder.decode(result, StandardCharsets.UTF_8));
    }

}
