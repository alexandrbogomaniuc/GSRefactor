package com.abs.casino.common.cache.data.payment.transfer.paymentmean;

public enum PaymentMeanId {
    FOUNDS_ON_DEMAND(6l);

    private long id;

    private PaymentMeanId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public static PaymentMeanId getById(long id) {
        switch ((int) id) {
            case 6: {
                return FOUNDS_ON_DEMAND;
            }
            default: {
                return null;
            }
        }
    }

}
