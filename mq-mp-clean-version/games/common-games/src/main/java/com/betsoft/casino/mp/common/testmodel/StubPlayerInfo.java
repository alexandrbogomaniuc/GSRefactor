package com.betsoft.casino.mp.common.testmodel;

public class StubPlayerInfo {
    private Long bankId;
    private Long accountId;
    private String externalId;
    private String userName;
    private long balance;
    private StubCurrency currency;
    private boolean guest;
    private StubPlayerStats stats;
    private boolean showRefreshBalanceButton;

    public StubPlayerInfo(Long bankId, Long accountId, String externalId, String userName, long balance, StubCurrency currency,
                          boolean guest, boolean showRefreshBalanceButton) {
        this.bankId = bankId;
        this.accountId = accountId;
        this.externalId = externalId;
        this.userName = userName;
        this.balance = balance;
        this.currency = currency;
        this.guest = guest;
        this.showRefreshBalanceButton = showRefreshBalanceButton;
    }

    public Long getBankId() {
        return bankId;
    }

    public void setBankId(Long bankId) {
        this.bankId = bankId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public StubCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(StubCurrency currency) {
        this.currency = currency;
    }

    public StubPlayerStats getStats() {
        return stats;
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public boolean isShowRefreshBalanceButton() {
        return showRefreshBalanceButton;
    }

    public void setShowRefreshBalanceButton(boolean showRefreshBalanceButton) {
        this.showRefreshBalanceButton = showRefreshBalanceButton;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StubPlayerInfo that = (StubPlayerInfo) o;

        if (!bankId.equals(that.bankId)) return false;
        if (!accountId.equals(that.accountId)) return false;
        return externalId.equals(that.externalId);
    }

    @Override
    public int hashCode() {
        int result = bankId.hashCode();
        result = 31 * result + accountId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PlayerInfo [");
        sb.append("bankId=").append(bankId);
        sb.append(", accountId=").append(accountId);
        sb.append(", externalId='").append(externalId).append('\'');
        sb.append(", userName='").append(userName).append('\'');
        sb.append(", balance=").append(balance);
        sb.append(", currency=").append(currency);
        sb.append(", stats=").append(stats);
        sb.append(", guest=").append(guest);
        sb.append(", showRefreshBalanceButton=").append(showRefreshBalanceButton);
        sb.append(']');
        return sb.toString();
    }
}

