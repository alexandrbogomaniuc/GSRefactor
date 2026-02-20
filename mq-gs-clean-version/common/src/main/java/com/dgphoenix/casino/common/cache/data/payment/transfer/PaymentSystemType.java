package com.dgphoenix.casino.common.cache.data.payment.transfer;

/**
 * User: flsh
 * Date: 11.08.2009
 */
public enum PaymentSystemType {
    INTERNAL("Internal", true),
    UPR("UniquePokerRoom", true),
    CEREUS("Cereus", true),
    COMMON_TRANSFER("CommonTransfer", true),
    RED7("Red7", true),
    RPK("RoyalPK", true),
    FACEBOOK("Facebook", false);

    private String name;
    private boolean individuallyTracked;

    private PaymentSystemType(String name, boolean individuallyTracked) {
        this.name = name;
        this.individuallyTracked = individuallyTracked;
    }

    public String getName() {
        return name;
    }

    public boolean isIndividuallyTracked() {
        return individuallyTracked;
    }
}
