package com.abs.casino.common.analytics.spin;

import com.google.common.base.Splitter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Optional;

/**
 * Created by isador
 * on 11/1/17
 */
public class ClientStatistic {
    private static final Logger LOG = LogManager.getLogger(ClientStatistic.class);
    private static final String CMD_PARAM = "CMD";
    private static final String SPIN_ANIMATION_TIME_PARAM = "SPINANMTIME";
    private static final String SPIN_REQUEST_TIME_PARAM = "SPINREQTIME";
    private static final char PARAM_SEPARATOR = '&';
    private static final char PARAM_KEY_VALUE_SEPARATOR = '=';
    private static final String STAT_ENCODING = "UTF-8";

    private String cmd;
    private Integer spinAnimTime;
    private Integer spinReqTime;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Optional<Integer> getSpinAnimTime() {
        return Optional.ofNullable(spinAnimTime);
    }

    public void setSpinAnimTime(int spinAnimTime) {
        this.spinAnimTime = spinAnimTime;
    }

    public Optional<Integer> getSpinReqTime() {
        return Optional.ofNullable(spinReqTime);
    }

    public void setSpinReqTime(int spinReqTime) {
        this.spinReqTime = spinReqTime;
    }

    public static ClientStatistic extract(String encodedStat) {
        ClientStatistic clientStatistic = new ClientStatistic();
        try {
            String clientStat = URLDecoder.decode(encodedStat, STAT_ENCODING);
            Map<String, String> clientStatMap = Splitter.on(PARAM_SEPARATOR)
                    .withKeyValueSeparator(PARAM_KEY_VALUE_SEPARATOR).split(clientStat);
            clientStatistic.setCmd(clientStatMap.get(CMD_PARAM));
            if (clientStatMap.containsKey(SPIN_ANIMATION_TIME_PARAM)) {
                try {
                    clientStatistic.setSpinAnimTime(Integer.parseInt(clientStatMap.get(SPIN_ANIMATION_TIME_PARAM)));
                } catch (NumberFormatException e) {
                    LOG.error("Invalid spin animation time param", e);
                }
            }
            if (clientStatMap.containsKey(SPIN_REQUEST_TIME_PARAM)) {
                try {
                    clientStatistic.setSpinReqTime(Integer.parseInt(clientStatMap.get(SPIN_REQUEST_TIME_PARAM)));
                } catch (NumberFormatException e) {
                    LOG.error("Invalid spin request time param", e);
                }
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupported encoding {}", STAT_ENCODING, e);
        }
        return clientStatistic;
    }
}
