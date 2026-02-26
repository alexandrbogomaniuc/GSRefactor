package com.abs.casino.common.client.canex.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CanexResponse {

    @JsonProperty("CODE")
    @SerializedName("CODE")
    private Integer code;

    @JsonProperty("RESULT")
    @SerializedName("RESULT")
    private String result;

    @JsonProperty("USERID")
    @SerializedName("USERID")
    private String userId;

    @JsonProperty("USERNAME")
    @SerializedName("USERNAME")
    private String userName;

    @JsonProperty("FIRSTNAME")
    @SerializedName("FIRSTNAME")
    private String firstname;

    @JsonProperty("LASTNAME")
    @SerializedName("LASTNAME")
    private String lastname;

    @JsonProperty("EMAIL")
    @SerializedName("EMAIL")
    private String email;

    @JsonProperty("CURRENCY")
    @SerializedName("CURRENCY")
    private String currency;

    @JsonProperty("BALANCE")
    @SerializedName("BALANCE")
    private Long balance;

    @JsonProperty("EXTSYSTEMTRANSACTIONID")
    @SerializedName("EXTSYSTEMTRANSACTIONID")
    private String extSystemTransactionId;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
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

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public String getExtSystemTransactionId() {
        return extSystemTransactionId;
    }

    public void setExtSystemTransactionId(String extSystemTransactionId) {
        this.extSystemTransactionId = extSystemTransactionId;
    }
}
