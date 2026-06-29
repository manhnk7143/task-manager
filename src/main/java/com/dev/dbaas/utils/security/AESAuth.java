package com.dev.dbaas.utils.security;

import org.apache.log4j.Logger;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import static com.google.common.primitives.Bytes.concat;

public class AESAuth {

    private static final Logger LOGGER = Logger.getLogger(AESAuth.class);

    public static String encrypt(String data, String key) throws Exception {
        Key SECRET_KEY = new SecretKeySpec(key.getBytes(), "AES");
        Cipher c = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        c.init(Cipher.ENCRYPT_MODE, SECRET_KEY);
        byte[] encrypted = c.doFinal(data.getBytes());
        return Base64Utils.encodeToString(encrypted);
    }

    public static String decrypt(String encryptedData, String encryptedKey) throws Exception {

        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[] key = encryptedKey.getBytes(StandardCharsets.UTF_8);
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);

        Key SECRET_KEY = new SecretKeySpec(key, "AES");
        Cipher c = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        c.init(Cipher.DECRYPT_MODE, SECRET_KEY);
        byte[] base64Encoded = Base64Utils.decodeFromString(encryptedData);
        byte[] decrypted = c.doFinal(base64Encoded);
        return new String(decrypted);
    }

    public static String decryptAesCbc(String encrypt, String password) {
        try {
            byte[] secretKeyClear = password.getBytes();
            byte[] cipherBytes = Base64.getDecoder().decode(encrypt.replaceAll("\\n", ""));
            byte[] salt = Arrays.copyOfRange(cipherBytes, 8, 16);
            cipherBytes = Arrays.copyOfRange(cipherBytes, 16, cipherBytes.length);

            byte[] passAndSalt = concat(secretKeyClear, salt);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] key = md.digest(passAndSalt);
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

            md.reset();
            byte[] iv = Arrays.copyOfRange(md.digest(concat(key, passAndSalt)), 0, 16);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return new String(cipher.doFinal(cipherBytes));
        } catch (Exception e) {
            LOGGER.error(e,e);
        }
        return null;
    }
}