package com.abs.casino.common.cache.data.payment.transfer.paymentmean;

/**
 * User: flsh
 * Date: 11.08.2009
 */
public enum PaymentMeanType {
    ACCOUNT_INFO_EXTERNAL_ID("ExternalId");

    private String name;

    private PaymentMeanType(String name) {
        this.name = name;
    }
}
