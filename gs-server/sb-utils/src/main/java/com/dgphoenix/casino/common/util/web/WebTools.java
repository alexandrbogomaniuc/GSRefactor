package com.dgphoenix.casino.common.util.web;

import com.dgphoenix.casino.common.util.KryoHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by ANGeL
 * Date: Sep 16, 2008
 * Time: 7:31:15 PM
 */
public class WebTools {

    private static final String UTF_8 = "UTF-8";

    private WebTools() {
    }

    public static String encodeHashTableToURL(Map<String, String> ht)
            throws UnsupportedEncodingException {

        final StringBuilder res = new StringBuilder(ht != null && !ht.isEmpty() ? ht.size() * 6 : 1);

        encodeHashTableToURL(ht, res);

        return res.toString();
    }

    public static void encodeHashTableToURL(Map<String, String> ht, StringBuilder res)
            throws UnsupportedEncodingException {
        if (ht != null && !ht.isEmpty()) {
            for (Entry<String, String> e : ht.entrySet()) {
                final String strKey = e.getKey();
                final String strValue = e.getValue();

                if (res.length() != 0) {
                    res.append('&');
                }
                res.append(URLEncoder.encode(strKey, UTF_8))
                        .append('=')
                        .append(strValue!=null ?URLEncoder.encode(strValue, UTF_8):"");
            }
        }
    }

    public static String encodeObject(Object o) {
        byte[] bytes = KryoHelper.serializeToBytes(o);
        try {
            String string = Base64.encodeBase64URLSafeString(bytes);
            return URLEncoder.encode(string, UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T decodeObject(String s, Class<T> tClass) {
        try {
            String decode = URLDecoder.decode(s, UTF_8);
            byte[] bytes = Base64.decodeBase64(decode);
            return KryoHelper.deserializeFrom(bytes, tClass);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String debugRequest(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder(100);
        sb.append("query=").append(req.getQueryString()).append("; params=");
        Enumeration parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = (String) parameterNames.nextElement();
            sb.append(name).append("=").append(req.getParameter(name)).append("&");
        }
/*        Enumeration attributeNames = req.getAttributeNames();
        sb.append("; attributes=");
        while (attributeNames.hasMoreElements()) {
            String name = (String) attributeNames.nextElement();
            sb.append(name + "=" + req.getParameter(name) + "&");
        }*/
        return sb.toString();
    }

    public static String getHttpRequestParameterValue(String request, String parameterName) {
        for (NameValuePair nameValuePair : URLEncodedUtils.parse(request, Charsets.UTF_8)) {
            if (nameValuePair.getName().equals(parameterName)) {
                return nameValuePair.getValue();
            }
        }
        return null;
    }

    public static long toMoney(double value) {
        return (long) (value*100);
    }

    public static void setResponseStandardHeader(final HttpServletRequest request, final HttpServletResponse response) {
        if (request.getHeader("Origin") != null) {
            response.setHeader("Access-Control-Allow-Origin", request
                    .getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Credentials",
                    "true");
        }
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
    }
}
