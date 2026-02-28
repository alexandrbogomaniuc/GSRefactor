package com.abs.casino.common.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * User: flsh
 * Date: 17.04.2009
 */
public class CookieUtils {
   public static String getCookieValue(HttpServletRequest request,
                                        String name) {
        Cookie cookies[] = request.getCookies();
        if (null == cookies) {
            return null;
        }
        for (int i = 0; i < cookies.length; ++i) {
            if (cookies[i].getName().equals(name)) {
                try {
                    return URLDecoder.decode(cookies[i].getValue(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    return cookies[i].getValue();
                }
            }
        }
        return null;
    }

    public static void setCookie(HttpServletResponse response, String name,
                                 String value) {
        setCookie(response, name, value, "/", -1);
    }

    public static void setCookie(HttpServletResponse response, String name,
                                 String value, String path, int maxAge) {
        Cookie cookie;
        try {
            cookie = new Cookie(name, URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            cookie = new Cookie(name, value);
        }
        cookie.setPath(path);
        if (maxAge != -1) {
            cookie.setMaxAge(maxAge);
        }
        response.addCookie(cookie);
    }

}
