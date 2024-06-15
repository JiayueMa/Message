package com.jiayuema;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final int KEY_LENGTH = 32;

    public static String encrypt(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(adjustKeyLength(key).getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String data, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(adjustKeyLength(key).getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(data);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, "UTF-8");
    }

    private static String adjustKeyLength(String key) {
        key = key + "asd5v968e521cv6523gh1s65a421h5dfa42621asd654v654dfa1da56"; // pad with additional characters
        return key.substring(0, KEY_LENGTH); // truncate to 32 bytes
    }
}
