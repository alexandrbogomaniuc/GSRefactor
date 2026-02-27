package com.abs.casino.gs.managers.payment.bonus.client;

public class RESTGetClient extends RESTClient {

    public RESTGetClient(long bankId) {
        super(bankId);
    }

    @Override
    protected boolean isPost() {
        return false;
    }
}
