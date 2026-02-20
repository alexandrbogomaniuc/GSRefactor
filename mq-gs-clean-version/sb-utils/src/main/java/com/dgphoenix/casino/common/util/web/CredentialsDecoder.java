package com.dgphoenix.casino.common.util.web;

import com.dgphoenix.casino.common.exception.CommonException;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class CredentialsDecoder {
    private static final String DES_KEY = "#7Sboy*-";
    public static final String DELIMETER = ",";
    public static final String PREFIX = "PANORA_ENC_CHECKSUM";
    private Cipher ecipher;
    private Cipher dcipher;

    private static final CredentialsDecoder instance = new CredentialsDecoder();

    public static CredentialsDecoder getInstance() {
        return instance;
    }

    /**
     * Constructor used to create this object.  Responsible for setting
     * and initializing this object's encrypter and decrypter Chipher instances
     * given a Pass Phrase and algorithm.
     */

    public CredentialsDecoder() {
        try {
            KeySpec keySpec = new DESKeySpec(DES_KEY.getBytes());
            SecretKey key = SecretKeyFactory.getInstance("DES").generateSecret(keySpec);

            ecipher = Cipher.getInstance("DES/ECB/NoPadding");
            dcipher = Cipher.getInstance("DES/ECB/NoPadding");

            ecipher.init(Cipher.ENCRYPT_MODE, key);
            dcipher.init(Cipher.DECRYPT_MODE, key);

        } catch (InvalidKeySpecException e) {
            System.out.println("EXCEPTION: InvalidKeySpecException");
        } catch (NoSuchPaddingException e) {
            System.out.println("EXCEPTION: NoSuchPaddingException");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("EXCEPTION: NoSuchAlgorithmException");
        } catch (InvalidKeyException e) {
            System.out.println("EXCEPTION: InvalidKeyException");
        }
    }


    /**
     * Takes a single String as an argument and returns an Encrypted version
     * of that String.
     *
     * @param str String to be encrypted
     * @return <code>EncoderResponce</code> Encrypted version of the provided String
     * @throws CommonException
     */    
    public EncoderResponce encrypt(String str) throws CommonException {
        try {
            // Encode the string into bytes using utf-8
            String sourceStr = PREFIX + DELIMETER + str;
            byte[] plaintext = sourceStr.getBytes("UTF8");

            byte[] source;
            if (plaintext.length % 8 != 0) {
                int len = plaintext.length / 8 * 8 + 8;
                source = new byte[len];
                System.arraycopy(plaintext, 0, source, 0, plaintext.length);
                for (int ii = plaintext.length; ii < len; ii++) {
                    source[ii] = "#".getBytes("UTF8")[0];
                }
            } else {
                source = new byte[plaintext.length];
                System.arraycopy(plaintext, 0, source, 0, source.length);
            }


            // Encrypt
            byte[] enc = ecipher.doFinal(source);

            // Encode bytes to base64 to get a string
            String result = new String(Base64.encodeBase64(enc));
            //System.out.println(URLEncoder.encode(result, "UTF-8"));
//            result = enc.length + DELIMETER + result.length() + DELIMETER + result;
            return new EncoderResponce(result.length(), enc.length, result);
        } catch (Exception e) {
            throw new CommonException("Can't to encrypt string " + str, e);
        }
    }


    /**
     * Takes a encrypted String as an argument, decrypts and returns the
     * decrypted String.
     *
     * @param str Encrypted String to be decrypted
     * @return {@linkplain DecoderResponce} Decrypted version of the provided String
     * @throws CommonException
     */
    public DecoderResponce decrypt(String str) throws CommonException {
        try {
            byte[] dec = Base64.decodeBase64(str);
            // Decrypt
            byte[] utf8 = dcipher.doFinal(dec);
            // Decode using utf-8
            String result = new String(utf8, "UTF8");

            result = result.replaceFirst((PREFIX + DELIMETER), "");
            result = result.substring(0, result.indexOf("#"));

            return new DecoderResponce(result);
        } catch (Exception e) {
            throw new CommonException("Can't to decrypt string " + str, e);
        }
    }

    public static void main(String[] args) {
        try {

            String source = "10,UB1022,711107031";
            System.out.println("source = " + source);
            CredentialsDecoder cd = new CredentialsDecoder();

            EncoderResponce responce = cd.encrypt(source);
            System.out.println("enctipted:" + responce.getEncString());

            String s = "cdROzWH6scNOxFotXvpfHUFrNVhIzD6ZB/ZZ8aog8yLmb4pa5YvO+g==";

            DecoderResponce decripted = cd.decrypt(/*responce.getEncString()*/s);
            System.out.println("decripted:" + decripted.getDecriptedStr());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
