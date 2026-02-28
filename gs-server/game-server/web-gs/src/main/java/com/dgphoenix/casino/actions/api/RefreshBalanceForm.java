package com.abs.casino.actions.api;

import com.abs.casino.actions.enter.CommonActionForm;
import org.apache.log4j.Logger;

/**
 * User: van0ss
 * Date: 22.12.2016
 */
public class RefreshBalanceForm extends CommonActionForm {

    protected String extUserId;

    public String getExtUserId() {
        return extUserId;
    }

    public void setExtUserId(String extUserId) {
        this.extUserId = extUserId;
    }


    @Override
    protected Logger getLogger() {
        return null;
    }

    @Override
    public String toString() {
        return "RefreshBalanceForm{" +
                "extUserId='" + extUserId + '\'' +
                "} " + super.toString();
    }
}
