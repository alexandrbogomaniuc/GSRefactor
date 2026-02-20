package com.dgphoenix.casino.gs.managers.payment.bonus.client;

/**
 * User: ktd
 * Date: 29.03.11
 */

public class BonusAccountInfoResult {

    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private String currency;
    private boolean isSuccess;

    private String countryCode;

    public BonusAccountInfoResult(String userName, String firstName, String lastName, String email, String currency,
                                  boolean success, String countryCode) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.currency = currency;
        this.isSuccess = success;

        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
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


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BonusAccountInfoResult [");
        sb.append("userName='").append(userName).append('\'');
        sb.append(", firstName='").append(firstName).append('\'');
        sb.append(", lastName='").append(lastName).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", currency='").append(currency).append('\'');
        sb.append(", isSuccess=").append(isSuccess);
        sb.append(", countryCode='").append(countryCode).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
