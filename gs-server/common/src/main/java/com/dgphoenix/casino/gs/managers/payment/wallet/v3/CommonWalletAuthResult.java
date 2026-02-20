package com.dgphoenix.casino.gs.managers.payment.wallet.v3;

public class CommonWalletAuthResult {

    protected String userId;
    protected double balance;

    protected String userName;
    protected String firstName;
    protected String lastName;
    protected String email;

    protected String currency;

    protected boolean isSuccess;
    protected String countryCode;

    protected String cashierToken; //for Planet Win

    public CommonWalletAuthResult(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public CommonWalletAuthResult(String userId, double balance, String userName, String firstName, String lastName,
                                  String email, String currency, boolean success, String countryCode) {
        this.userId = userId;
        this.balance = balance;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.currency = currency;
        this.isSuccess = success;
        this.countryCode = countryCode;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCashierToken() {
        return cashierToken;
    }

    public void setCashierToken(String cashierToken) {
        this.cashierToken = cashierToken;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommonWalletAuthResult [");
        sb.append("userId='").append(userId).append('\'');
        sb.append(", balance=").append(balance);
        sb.append(", userName='").append(userName).append('\'');
        sb.append(", firstName='").append(firstName).append('\'');
        sb.append(", lastName='").append(lastName).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", currency='").append(currency).append('\'');
        sb.append(", isSuccess=").append(isSuccess);
        sb.append(", countryCode='").append(countryCode).append('\'');
        sb.append(", cashierToken='").append(cashierToken).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
