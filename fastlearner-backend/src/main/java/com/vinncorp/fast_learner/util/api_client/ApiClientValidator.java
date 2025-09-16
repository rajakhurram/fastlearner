package com.vinncorp.fast_learner.util.api_client;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


public class ApiClientValidator {

    public static void main(String[] args) throws Exception {
        System.out.println(encrypt("Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuYXZlZWRAbWFpbGluYXRvci5jb20iLCJleHAiOjE3MzMzODk0MzMsImlhdCI6MTczMzM4MDQzMywiSU5TVFJVQ1RPUiI6IklOU1RSVUNUT1IifQ.OAChbOj0esLgi50H0MlOADS_EFl4Uo9A6pwwXjyvXxWbUKCy1q5G55Twvd_2PDCnTws2v3Kt1jCYtiSalBWUKw"
        , "1234567890abcdef1234567890abcdef"));
    }

    /**
     * @param data Data for encryption
     * @param encryptedData Encrypted data sent by the client
     * @param secretKey Our saved secret key by which encryption will apply.
     *
     * @return false will be return if the encrypted data doesn't match with the provided
     * data in the request header
     * */
    public static boolean encrypt(String data, String encryptedData, String secretKey) {
        try {
            SecretKey SECRET_KEY = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            String ENCRYPTED_DATA = Base64.getEncoder().encodeToString(encryptedBytes);
            if (ENCRYPTED_DATA.equals(encryptedData))
                return true;
        } catch (Exception e) {
            System.out.println("ERROR ==>> " + e.getLocalizedMessage());
        }
        return false;
    }

    public static String encrypt(String data, String secretKey) throws Exception {
        SecretKey SECRET_KEY = new SecretKeySpec(secretKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}
