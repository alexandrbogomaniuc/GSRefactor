package com.dgphoenix.casino.sm.login;

/**
 * User: shegan
 * Date: 07.08.15
 */
public class VietbetLoginRequest extends LoginRequest {
    protected String customerId;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }


    @Override
    public String toString() {
        return "VietbetLoginRequest{" +
                "customerId='" + customerId + '\'' +
                '}' + super.toString();
    }
}
