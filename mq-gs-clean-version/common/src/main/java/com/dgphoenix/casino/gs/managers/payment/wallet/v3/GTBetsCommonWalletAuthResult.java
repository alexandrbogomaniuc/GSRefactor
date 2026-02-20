package com.dgphoenix.casino.gs.managers.payment.wallet.v3;

public class GTBetsCommonWalletAuthResult extends CommonWalletAuthResult {
    private AuthActionType authActionType;

    public GTBetsCommonWalletAuthResult(CommonWalletAuthResult authResult, AuthActionType authActionType) {
        super(authResult.userId, authResult.balance, authResult.userName, authResult.firstName, authResult.lastName,
                authResult.email, authResult.currency, authResult.isSuccess(), authResult.countryCode);
        this.authActionType = authActionType;
    }

    public AuthActionType getAuthActionType() {
        return authActionType;
    }

    public void setAuthActionType(AuthActionType authActionType) {
        this.authActionType = authActionType;
    }

    @Override
    public String toString() {
        return "GTBetsCommonWalletAuthResult [" +
                "newSession='" + authActionType + '\'' +
                ", userId='" + userId + '\'' +
                ", balance=" + balance +
                ", userName='" + userName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", currency='" + currency + '\'' +
                ", isSuccess=" + isSuccess +
                ", countryCode='" + countryCode + '\'' +
                ", cashierToken='" + cashierToken + '\'' +
                ']';
    }
}
