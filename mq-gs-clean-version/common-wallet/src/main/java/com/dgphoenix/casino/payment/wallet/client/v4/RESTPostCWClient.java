package com.dgphoenix.casino.payment.wallet.client.v4;

public class RESTPostCWClient extends RESTCWClient {

    public RESTPostCWClient(long bankId) {
        super(bankId);
    }

    @Override
    protected boolean isPost() {
        return true;
    }

}
