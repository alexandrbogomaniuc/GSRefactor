package com.dgphoenix.casino.sm.login;

/**
 * User: isirbis
 * Date: 07.10.14
 */
public class CWLoginRequest extends LoginRequest {
    protected Long balance;
    protected Boolean isGuest;
    protected Boolean isCheckBalance;

    //used only in stlobby
    private String fakeExternalSessionId;

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Boolean isGuest() {
        if (isGuest == null) {
            return false;
        }
        return isGuest;
    }

    public void setIsGuest(Boolean isGuest) {
        this.isGuest = isGuest;
    }

    public Boolean isCheckBalance() {
        if (isCheckBalance == null) {
            return true;
        }
        return isCheckBalance;
    }

    public void setCheckBalance(Boolean isCheckBalance) {
        this.isCheckBalance = isCheckBalance;
    }

    public String getFakeExternalSessionId() {
        return fakeExternalSessionId;
    }

    public void setFakeExternalSessionId(String fakeExternalSessionId) {
        this.fakeExternalSessionId = fakeExternalSessionId;
    }

    @Override
    public String toString() {
        return "CWLoginRequest[" +
                "balance=" + balance +
                ", isGuest=" + isGuest +
                ", isCheckBalance=" + isCheckBalance +
                ", fakeExternalSessionId='" + fakeExternalSessionId + '\'' +
                ']' + super.toString();
    }
}
