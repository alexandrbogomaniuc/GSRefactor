package com.abs.casino.actions.api;

import org.apache.struts.action.ActionForm;

public class GetLeaderboardUrlsForm extends ActionForm {
    private String bankId;

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bank) {
        this.bankId = bank;
    }
}
