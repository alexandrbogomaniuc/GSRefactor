package com.betsoft.casino.bots.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrashBetKeyUtil {
    private static final Logger LOG = LogManager.getLogger(CrashBetKeyUtil.class);
    public static final String DELIMITER = "_0_";

    public static String getNicknameFromCrashBetKey(String crashBetKey) {

        //LOG.debug("getNicknameFromCrashBetKey: crashBetKey={}", crashBetKey);

        if (crashBetKey != null && crashBetKey.contains(DELIMITER)) {

            String[] crashBetKeyParts = crashBetKey.split(DELIMITER);
            //LOG.debug("getNicknameFromCrashBetKey: crashBetKeyParts={}", crashBetKeyParts);

            if (crashBetKeyParts.length >= 2) {

                String nickname = String.join(DELIMITER,
                        java.util.Arrays.copyOfRange(crashBetKeyParts, 1, crashBetKeyParts.length));
                //LOG.debug("getNicknameFromCrashBetKey: generated nickname={}", nickname);

                return nickname;
            }
        }
        return null;
    }

    public static String getCrashBetKeyFromNickname(String nickname) {
        return getCrashBetKeyFromTimeAndNickname(System.currentTimeMillis(), nickname);
    }

    public static String getCrashBetKeyFromTimeAndNickname(long timeMs, String nickname) {
        return timeMs + DELIMITER + nickname;
    }
}
