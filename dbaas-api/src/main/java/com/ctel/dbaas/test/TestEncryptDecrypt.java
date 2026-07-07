package com.ctel.dbaas.test;

import com.ctel.dbaas.utils.CryptoUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.security.KeyPair;
import java.util.Base64;

public class TestEncryptDecrypt {

    @SneakyThrows
    public static void main(String[] args) {
        KeyPair keyPair = CryptoUtils.generateKeyPairRSA(1024);
        String keyEncrypt = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String keyDecrypt = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        System.out.println(keyEncrypt);
        System.out.println(keyDecrypt);

        String input = """
                {
                    "instanceId": "00e60d29-89c3-4397-8480-7cdd063b2141",
                    "action": "get_list_databases",
                    "requestData": {}
                }
                """;

        String encryptedData = CryptoUtils.encryptData(input, keyEncrypt);
        System.out.println("Encrypted Data: " + encryptedData);

        String decryptedData = CryptoUtils.decryptData(encryptedData, keyDecrypt);
        System.out.println("Decrypted Data: " + decryptedData);

        ObjectMapper objectMapper = new ObjectMapper();
//        ClientRequest.DataDecrypted data = objectMapper.readValue(decryptedData, ClientRequest.DataDecrypted.class);
//        System.out.println(data);
    }

}
