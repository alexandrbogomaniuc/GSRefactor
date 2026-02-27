package com.abs.casino.gs.singlegames.tools.util;


import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public final class SocketUtils {

    public static final String encodeHashTableToURL(HashMap ht) {

        final StringBuilder res = new StringBuilder();

        encodeHashTableToURL(ht, res);

        return res.toString();
    }

    public static final String encodeHashTableToURL(HashMap ht, boolean encode) {

        final StringBuilder res = new StringBuilder();

        encodeHashTableToURL(ht, res, encode);

        return res.toString();
    }

    public static final void encodeHashTableToURL(
            HashMap ht,
            StringBuilder res) {
        encodeHashTableToURL(ht, res, true);
    }

    public static final void encodeHashTableToURL(
            HashMap ht,
            StringBuilder res,
            boolean encode) {
        if (ht != null && ht.size() > 0) {
            for (Iterator i = ht.entrySet().iterator(); i.hasNext(); ) {

                final Map.Entry e = (Map.Entry) i.next();
                final String strKey = (String) e.getKey();
                final String strValue = (String) e.getValue();

                if (res.length() != 0) {
                    res.append('&');
                }
                res.append(encode ? URLEncoder.encode(strKey) : strKey)
                        .append('=')
                        .append(encode ? URLEncoder.encode(strValue) : strValue);
            }
        }
    }

    private SocketUtils() {
    }

}
