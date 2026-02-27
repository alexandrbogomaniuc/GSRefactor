package com.dgphoenix.casino.gs.managers.payment.wallet.processor;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.web.HttpClientConnection;
import com.dgphoenix.casino.common.util.xml.XmlRequestResult;
import com.abs.casino.common.util.xml.parser.Parser;
import com.dgphoenix.casino.gs.managers.game.session.CloseGameSessionNotifyRequest;
import com.dgphoenix.casino.gs.managers.game.session.INotifyResponseProcessor;
import com.dgphoenix.casino.statistics.http.HttpClientCallbackHandler;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Splitter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Map;

public class NotifyResponseProcessor implements INotifyResponseProcessor {
    private static final Logger LOG = LogManager.getLogger(NotifyResponseProcessor.class);
    private static final byte VERSION = 0;

    @Override
    public void process(CloseGameSessionNotifyRequest request) throws CommonException {
        String response;
        XmlRequestResult result;
        try {
            long now = System.currentTimeMillis();
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(request.getBankId());
            String url = request.getUrl();
            String params = request.getParams();
            LOG.info("request to url:" + url + " bankId:" + request.getBankId() + " is:" + params);

            result = new XmlRequestResult();
            Parser parser = Parser.instance();

            response = HttpClientConnection.newInstance(HttpClientCallbackHandler.getInstance()).doRequest(
                    bankInfo.isUsesJava8Proxy(), url, splitParams(params), request.isPost(),
                    CollectionUtils.stringToMap(bankInfo.getCWSpecialRequestHeaders()),
                    bankInfo.isUseHttpProxy());

            parser.parse(response, result);

            LOG.info("request response from url:" + url + " bankId:" + request.getBankId() + " is:" +
                    response + " time: " + (System.currentTimeMillis() - now));
        } catch (Exception e) {
            LOG.error("Failed to send notification.", e);
            throw e;
        }

        if (!result.isSuccessful()) {
            String message = "response was not successful: " + response;
            LOG.error(message);
            throw new CommonException(message);
        }
    }

    private Map<String, String> splitParams(String params) {
        return Splitter.on("&").omitEmptyStrings().withKeyValueSeparator("=").split(params);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
    }

}
