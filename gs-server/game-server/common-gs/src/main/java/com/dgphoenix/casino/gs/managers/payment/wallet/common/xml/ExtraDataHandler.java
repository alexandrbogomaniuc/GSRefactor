package com.dgphoenix.casino.gs.managers.payment.wallet.common.xml;

import com.abs.casino.common.util.xml.parser.XmlHandler;

/**
 * User: flsh
 * Date: 14.06.13
 */
public class ExtraDataHandler extends XmlHandler {
    public static final String USER_TAG = "UserInfo";

    protected void registerAll() {
        registerProcessor(USER_TAG, ExtraDataProcessor.class.getName());
    }
}
