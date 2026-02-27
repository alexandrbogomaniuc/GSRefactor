package com.abs.casino.gs.managers.payment.wallet.common.remote;

import org.apache.struts.action.ActionForm;

/**
 * User: ktd
 * Date: 24.03.2010
 */
public class CommonWalletRegisterForm extends ActionForm {
    private String firstName;
    private String lastName;
    private String email;
    private String userId;
    private String username;
    private long bankId;
    private long subCasinoId;
    private String currencyCode;    //ISO 4217 currency code (3 uppercase chars)

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public long getSubCasinoId() {
        return subCasinoId;
    }

    public void setSubCasinoId(long subCasinoId) {
        this.subCasinoId = subCasinoId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}