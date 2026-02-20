package com.dgphoenix.casino.common.util;


import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.HexStringConverter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class DESCrypter {
    private final Cipher ecipher;
    private final Cipher dcipher;

    public DESCrypter(SecretKey key) throws CommonException {
        try {
            ecipher = Cipher.getInstance("DES");
            dcipher = Cipher.getInstance("DES");
            ecipher.init(Cipher.ENCRYPT_MODE, key);
            dcipher.init(Cipher.DECRYPT_MODE, key);
        } catch (Exception e) {
            throw new CommonException("Cannot create Decryptper", e);
        }
    }

    public String encrypt(String str) throws CommonException {
        try {
            // Encode the string into bytes using utf-8
            byte[] utf8 = str.getBytes("UTF8");

            // Encrypt
            byte[] enc = ecipher.doFinal(utf8);
            // Encode bytes to base64 to get a string
            return HexStringConverter.byteArrayToHexString(enc);
        } catch (Exception e) {
            throw new CommonException("Cannot encrypt string", e);
        }
    }

    public String decrypt(String str) throws CommonException {
        try {
            // Decode base64 to get bytes
            byte[] dec = HexStringConverter.hexStringToByteArray(str);

            // Decrypt
            byte[] utf8 = dcipher.doFinal(dec);

            // Decode using utf-8
            return new String(utf8, "UTF8");
        } catch (Exception e) {
            throw new CommonException("Cannot decrypt string", e);
        }
    }
}
