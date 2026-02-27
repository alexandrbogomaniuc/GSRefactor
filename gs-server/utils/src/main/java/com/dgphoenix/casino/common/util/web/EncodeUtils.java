package com.abs.casino.common.util.web;

import com.dgphoenix.casino.common.util.FastKryoHelper;
import com.esotericsoftware.kryo.util.UnsafeUtil;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;

public final class EncodeUtils {

    private static final String UTF_8 = "UTF-8";

    private EncodeUtils() {
    }


    public static String encodeObject(Object o) {
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = FastKryoHelper.serializeToBytes(o);
            byte[] bytes = getBytesArrayFromDirectBuffer(byteBuffer);
            String string = Base64.encodeBase64URLSafeString(bytes);
            return URLEncoder.encode(string, UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    public static <T> T decodeObject(String s, Class<T> tClass) {
        ByteBuffer byteBuffer = null;
        try {
            String decode = URLDecoder.decode(s, UTF_8);
            byte[] bytes = Base64.decodeBase64(decode);
            byteBuffer = ByteBuffer.wrap(bytes);
            return FastKryoHelper.deserializeFrom(byteBuffer, tClass);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    private static byte[] getBytesArrayFromDirectBuffer(ByteBuffer byteBuffer) {
        byte[] b = new byte[byteBuffer.remaining()];
        byteBuffer.get(b);
        return b;
    }

    private static void releaseBuffer(ByteBuffer buffer) {
        try {
            UnsafeUtil.releaseBuffer(buffer);
        } catch (Throwable ignore) {
        }
    }
}
