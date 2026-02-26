package com.abs.casino.support.cache.bank.edit.forms.common;


import org.apache.struts.action.ActionForm;
import org.apache.struts.util.LabelValueBean;

import java.util.Collection;

public class AwayFromBankInfoForm extends ActionForm {

    private String bankId;
    private Collection<LabelValueBean> gameIds;
    private String currentMaxGameId;
    private String inputModeOfId = "empty";
    private Collection<LabelValueBean> allLimits;
    private Collection<LabelValueBean> allCoins;


    public Collection<LabelValueBean> getAllCoins() {
        return allCoins;
    }

    public void setAllCoins(Collection<LabelValueBean> allCoins) {
        this.allCoins = allCoins;
    }

    public Collection<LabelValueBean> getAllLimits() {
        return allLimits;
    }

    public void setAllLimits(Collection<LabelValueBean> allLimits) {
        this.allLimits = allLimits;
    }

    public String getInputModeOfId() {
        return inputModeOfId;
    }

    public void setInputModeOfId(String inputModeOfId) {
        this.inputModeOfId = inputModeOfId;
    }

    public String getCurrentMaxGameId() {
        return currentMaxGameId;
    }

    public void setCurrentMaxGameId(String currentMaxGameId) {
        this.currentMaxGameId = currentMaxGameId;
    }

    public Collection<LabelValueBean> getGameIds() {
        return gameIds;
    }

    public void setGameIds(Collection<LabelValueBean> gameIds) {
        this.gameIds = gameIds;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }
}
