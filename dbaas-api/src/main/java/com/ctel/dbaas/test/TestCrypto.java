package com.ctel.dbaas.test;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import static com.google.common.primitives.Bytes.concat;

public class TestCrypto {

    @SneakyThrows
    public static void main(String[] args) {
        // echo -n manhnk | openssl enc -aes-256-cbc -md sha256 -pass pass:mypass -a
        String encrypt = "U2FsdGVkX18w2tdfX6DvzLrbsg5PZit9XymK9ekS+PxcU8ApdwJOhw+Mfl61IIX2";
        String password = "mypass";

        byte[] secretKeyClear = password.getBytes();
        byte[] cipherBytes = Base64.getDecoder().decode(encrypt);
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
        String clearText = new String(cipher.doFinal(cipherBytes));

        System.out.println(clearText);

    }

}
