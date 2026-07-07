package com.ctel.dbaas.test;

import com.ctel.dbaas.utils.CryptoUtils;
import com.ctel.dbaas.datastore.mongodb.model.Config;
import com.ctel.dbaas.utils.CommonUtils;

public class TestEncryptDecryptConfigInstance {

    public static void main(String[] args) {
        String secretKey = "mySecretKey";

        String rootPwdMongo = CommonUtils.generateString(30);
        Config mongoConfig = new Config(rootPwdMongo);

        String input = CommonUtils.toJson(mongoConfig);
        String dataEncrypt = CryptoUtils.encrypt(input, secretKey);

        String jsonConfig = CryptoUtils.decrypt(dataEncrypt, secretKey);
        Config configMongodb = CommonUtils.toObject(jsonConfig, Config.class);

        System.out.println(configMongodb);
    }

}
