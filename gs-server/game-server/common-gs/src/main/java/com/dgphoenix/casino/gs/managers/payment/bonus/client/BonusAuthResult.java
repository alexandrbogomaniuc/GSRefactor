package com.dgphoenix.casino.gs.managers.payment.bonus.client;

/**
 * User: ktd
 * Date: 29.03.11
 */

public class BonusAuthResult extends BonusAccountInfoResult {

    private String userId;

    public BonusAuthResult(String userId, String userName, String firstName, String lastName, String email, String currency, boolean success, String countryCode) {
        super(userName, firstName, lastName, email, currency, success, countryCode);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
