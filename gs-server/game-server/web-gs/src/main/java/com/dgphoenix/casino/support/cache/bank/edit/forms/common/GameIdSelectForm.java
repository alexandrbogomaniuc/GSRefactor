package com.abs.casino.support.cache.bank.edit.forms.common;

import org.apache.struts.action.ActionForm;
import org.apache.struts.util.LabelValueBean;

import java.util.Collection;


public class GameIdSelectForm extends ActionForm {

    private String configGameMode;
    private String gameIdExist;
    private String gameIdManual;
    private String bankId;
    private Collection<LabelValueBean> banksWithSelectedGame;
    private boolean mustShowBanks = false;
    private String selectedBankId;


    public String getSelectedBankId() {
        return selectedBankId;
    }

    public void setSelectedBankId(String selectedBankId) {
        this.selectedBankId = selectedBankId;
    }

    public boolean isMustShowBanks() {
        return mustShowBanks;
    }

    public void setMustShowBanks(boolean mustShowBanks) {
        this.mustShowBanks = mustShowBanks;
    }

    public Collection<LabelValueBean> getBanksWithSelectedGame() {
        return banksWithSelectedGame;
    }

    public void setBanksWithSelectedGame(Collection<LabelValueBean> banksWithSelectedGame) {
        this.banksWithSelectedGame = banksWithSelectedGame;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getGameIdManual() {
        return gameIdManual;
    }

    public void setGameIdManual(String gameIdManual) {
        this.gameIdManual = gameIdManual;
    }

    public String getGameIdExist() {
        return gameIdExist;
    }

    public void setGameIdExist(String gameIdExist) {
        this.gameIdExist = gameIdExist;
    }

    public String getConfigGameMode() {
        return configGameMode;
    }

    public void setConfigGameMode(String configGameMode) {
        this.configGameMode = configGameMode;
    }
}
