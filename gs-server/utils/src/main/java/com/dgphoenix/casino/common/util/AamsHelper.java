package com.abs.casino.common.util;

import com.abs.casino.common.util.CollectionUtils;

import java.util.Map;
import java.util.TreeMap;

/**
 * User: van0ss
 * Date: 29.11.2016
 */
public class AamsHelper {

    public static final String AAMS_SESSION_ID = "AAMS_SESSION_ID";
    public static final String TICKET_ID = "TICKET_ID";

    public static String convertToStringMap(String aamsSessionId, String ticketAams) {
        Map<String, String> aamsParams = new TreeMap<>(); // Sequence is important for persisting
        aamsParams.put(AAMS_SESSION_ID, aamsSessionId == null ? "" : aamsSessionId);
        aamsParams.put(TICKET_ID, ticketAams == null ? "" : ticketAams);
        return CollectionUtils.mapToString(aamsParams);
    }
}
