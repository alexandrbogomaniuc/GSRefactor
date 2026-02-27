package com.abs.casino.payment.wallet.client.v3;

public class RESTPostCWClient extends RESTCWClient {

    public RESTPostCWClient(long bankId) {
        super(bankId);
    }

    @Override
    protected boolean isPost() {
        return true;
    }

}
