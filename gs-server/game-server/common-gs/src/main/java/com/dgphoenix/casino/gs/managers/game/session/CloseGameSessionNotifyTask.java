package com.abs.casino.gs.managers.game.session;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.engine.tracker.AbstractCommonTracker;
import com.abs.casino.common.engine.tracker.ICommonTrackingTaskDelegate;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.abs.casino.statistics.http.HttpClientCallbackHandler;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;


/**
 * Created by quant on 13.04.16.
 */
public class CloseGameSessionNotifyTask implements ICommonTrackingTaskDelegate {
    private static final Logger LOG = LogManager.getLogger(CloseGameSessionNotifyTask.class);
    private static final byte VERSION = 1;
    private final static String RESPONSE_ERROR = "ERROR";
    private final static String RESPONSE_OK = "OK";

    private final static long EXPIRATION_INTERVAL = TimeUnit.MINUTES.toMillis(30);
    private final static long SLEEP_TIMEOUT = TimeUnit.SECONDS.toMillis(10);


    private CloseGameSessionNotifyRequest request;
    private long trackingPeriod;
    private long trackingFrequency;

    public CloseGameSessionNotifyTask() {}

    public CloseGameSessionNotifyTask(CloseGameSessionNotifyRequest request) {
        this.request = request;
        trackingPeriod = EXPIRATION_INTERVAL;
        trackingFrequency = SLEEP_TIMEOUT;
    }

    public CloseGameSessionNotifyTask(CloseGameSessionNotifyRequest request, long trackingPeriod, long trackingFrequency) {
        this.request = request;
        this.trackingPeriod = trackingPeriod;
        this.trackingFrequency = trackingFrequency;
    }

    @Override
    public void process(String key, AbstractCommonTracker tracker) throws CommonException {
        if (System.currentTimeMillis() - request.getTrackingStartTime() > trackingPeriod) {
            return;
        }
        if (request.getProcessor() == null) {
            defaultProcessor();
        } else {
            request.process();
        }
    }

    private void defaultProcessor() throws CommonException {
        String response;
        try {
            long now = System.currentTimeMillis();
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(request.getBankId());
            String url = request.getUrl();
            String params = request.getParams();
            LOG.info("request to url:" + url + " bankId:" + request.getBankId() + " is:" + params);
            response = HttpClientConnection.newInstance(HttpClientCallbackHandler.getInstance()).
                    doRequest(url, params, request.isPost(), true, bankInfo.isUseHttpProxy()).toString();
            LOG.info("request response from url:" + url + " bankId:" + request.getBankId() + " is:" +
                    response + " time: " + (System.currentTimeMillis() - now));
        } catch (Exception e) {
            LOG.error("Failed to send notification.", e);
            throw e;
        }

        if (!response.equals(RESPONSE_OK)) {
            String message = "response was not successful: " + response;
            LOG.error(message);
            throw new CommonException(message);
        }
    }


    @Override
    public long getTaskSleepTimeout() throws CommonException {
        return trackingFrequency;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        kryo.writeObject(output, request);
        output.writeByte(VERSION);
        output.writeLong(trackingPeriod, true);
        output.writeLong(trackingFrequency, true);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        request = kryo.readObject(input, CloseGameSessionNotifyRequest.class);
        byte ver = 0;
        try {
            ver = input.readByte();
        } catch (Exception e) {
            // Ignore exception. Versioning was not supported earlier
        }
        if (ver > 0) {
            trackingPeriod = input.readLong(true);
            trackingFrequency = input.readLong(true);
        }
    }

    public CloseGameSessionNotifyRequest getRequest() {
        return request;
    }
}
