package com.dgphoenix.casino.gs.managers.payment.wallet.common.xml;

import com.abs.casino.common.util.xml.parser.XmlHandler;

public class CWHandler extends XmlHandler {
    public final static String RESPONSE_TAG = "response".toUpperCase();

    protected void registerAll() {
        registerProcessor(RESPONSE_TAG, CWResponseProcessor.class.getName());
    }
}
