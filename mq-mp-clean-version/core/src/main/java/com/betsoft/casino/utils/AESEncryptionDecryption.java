package com.betsoft.casino.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.*;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AESEncryptionDecryption {

    private static final Logger LOG = LogManager.getLogger(AESEncryptionDecryption.class);
    private static SecretKey secretKey;
    private static final String ALGORITHM = "AES";

    // Static block for initializing the secret key
    static {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256); // You can use 128, 192, or 256 bits
            secretKey = keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing secret key", e);
        }
    }

    // Method to encrypt a string value
    public static String encrypt(String dataToEncrypt) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(dataToEncrypt.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Method to decrypt a string value
    public static String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }

    public static byte[] encryptTimestamp(long timestamp) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(timestamp).array());
        } catch (IllegalBlockSizeException e) {
            LOG.error("encryptTimestamp: IllegalBlockSizeException: {}", e.getMessage());
        } catch (NoSuchPaddingException e) {
            LOG.error("encryptTimestamp: NoSuchPaddingException: {}", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            LOG.error("encryptTimestamp: NoSuchAlgorithmException: {}", e.getMessage());
        } catch (InvalidKeyException e) {
            LOG.error("encryptTimestamp: InvalidKeyException: {}", e.getMessage());
        } catch (BadPaddingException e) {
            LOG.error("encryptTimestamp: BadPaddingException: {}", e.getMessage());
        }
        return longToBytes(timestamp);

    }

    // Method to decrypt a string value
    public static long decryptTimestamp(byte[] encryptedTimestamp) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(encryptedTimestamp);
            return ByteBuffer.wrap(decryptedBytes).getLong();
        } catch (NoSuchPaddingException e) {
            LOG.error("decryptTimestamp: NoSuchPaddingException: {}", e.getMessage());
        } catch (IllegalBlockSizeException e) {
            LOG.error("decryptTimestamp: IllegalBlockSizeException: {}", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            LOG.error("decryptTimestamp: NoSuchAlgorithmException: {}", e.getMessage());
        } catch (BadPaddingException e) {
            LOG.error("decryptTimestamp: BadPaddingException: {}", e.getMessage());
        } catch (InvalidKeyException e) {
            LOG.error("decryptTimestamp: InvalidKeyException: {}", e.getMessage());
        }
        return bytesToLong(encryptedTimestamp);
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }
}
