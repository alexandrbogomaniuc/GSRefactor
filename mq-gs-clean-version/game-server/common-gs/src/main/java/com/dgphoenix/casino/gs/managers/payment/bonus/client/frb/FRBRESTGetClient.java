package com.dgphoenix.casino.gs.managers.payment.bonus.client.frb;

public class FRBRESTGetClient extends FRBRESTClient {

    public FRBRESTGetClient(long bankId) {
        super(bankId);
    }

    @Override
    protected boolean isPost() {
        return false;
    }
}
