package com.dgphoenix.casino.sm.login;

/**
 * User: isirbis
 * Date: 08.10.14
 */
public class SLLoginRequest extends CWLoginRequest {
    String smartLiveOperator;

    public String getSmartLiveOperator() {
        return smartLiveOperator;
    }

    public void setSmartLiveOperator(String smartLiveOperator) {
        this.smartLiveOperator = smartLiveOperator;
    }

    @Override
    public String toString() {
        return "SLLoginRequest[" +
                "smartLiveOperator='" + smartLiveOperator + '\'' +
                ']' + super.toString();
    }
}
