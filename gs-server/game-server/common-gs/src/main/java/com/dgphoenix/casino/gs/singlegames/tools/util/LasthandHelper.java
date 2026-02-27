package com.abs.casino.gs.singlegames.tools.util;

import com.dgphoenix.casino.common.exception.GameException;
import com.dgphoenix.casino.common.util.GameTools;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Created
 * Date: Mar 3, 2009
 * Time: 4:23:02 PM
 */
public class LasthandHelper {
    private static final Logger LOG = Logger.getLogger(LasthandHelper.class);
    public static final String LST = "LST";

    public static String pack(Map<String, String> publicLastHand, Map<String, String> privateLastHand,
                              Map<String, String> autoPublic, Map<String, String> autoPrivate) {
        LOG.debug("LasthandHelper pack  publicLastHand:" + publicLastHand +
                " privateLastHand:" + privateLastHand +
                " autoPublic:" + autoPublic +
                " autoPrivate:" + autoPrivate);
        if (publicLastHand == null || publicLastHand.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(GameTools.pack(publicLastHand)).append("%");

        if (privateLastHand == null) {
            privateLastHand = new HashMap<String, String>();
        }
        privateLastHand.put(LST, String.valueOf(System.currentTimeMillis()));
        sb.append(GameTools.pack(privateLastHand)).append("%");

        if (autoPublic != null) {
            sb.append(GameTools.pack(autoPublic));
        } else {
            sb.append(" ");
        }
        sb.append("%");

        if (autoPrivate != null) {
            sb.append(GameTools.pack(autoPrivate));
        } else {
            sb.append(" ");
        }
        LOG.debug("LasthandHelper pack result: " + sb.toString());
        return sb.toString();
    }

    public static Vector<Map<String, String>> unpack(String lasthand) throws GameException {
        LOG.debug("LasthandHelper unpack lasthand:" + lasthand);
        if (lasthand == null || lasthand.length() == 0) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(lasthand, "%", false);

        Vector<Map<String, String>> res = new Vector<Map<String, String>>();
        if (st.hasMoreTokens()) {
            extractToken(st, res);
        } else {
            res.add(null);
        }

        if (st.hasMoreTokens()) {
            extractToken(st, res);
        } else {
            res.add(null);
        }

        if (st.hasMoreTokens()) {
            extractToken(st, res);
        } else {
            res.add(null);
        }

        if (st.hasMoreTokens()) {
            extractToken(st, res);
        } else {
            res.add(null);
        }

        return res;
    }

    private static void extractToken(StringTokenizer st, Vector<Map<String, String>> res) {
        String token = st.nextToken();
        if (!StringUtils.isTrimmedEmpty(token)) {
            res.add(GameTools.unpack(token));
        } else {
            res.add(null);
        }
    }

    public static Long getLastSaveTime(String lasthand) {
        try {
            Vector<Map<String, String>> lasthands = unpack(lasthand);
            if (lasthands != null && lasthands.size() > 1) {
                Map<String, String> privateLastHand = lasthands.get(1);
                if (privateLastHand != null) {
                    String lastSaveTime = privateLastHand.get(LST);
                    return lastSaveTime != null ? Long.valueOf(lastSaveTime) : null;
                }
            }
        } catch (GameException e) {
            LOG.error("getLastSaveTime", e);
            return null;
        }
        return null;
    }

    public static void main(String[] args) {
        Map<String, String> tmp = new HashMap<String, String>();
        tmp.put("TEST", "123");

        Map<String, String> tmp1 = new HashMap<String, String>();
        tmp1.put("TESTAUTO", "321");

        String s = pack(tmp, null, tmp1, null);
        LOG.debug("s=" + s);

        try {
            Vector v = unpack(s);
            LOG.debug("v=" + v);
        } catch (GameException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
