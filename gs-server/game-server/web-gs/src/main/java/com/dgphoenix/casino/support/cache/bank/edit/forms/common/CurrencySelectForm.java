package com.dgphoenix.casino.support.cache.bank.edit.forms.common;

import org.apache.struts.action.ActionForm;

import java.util.ArrayList;

public class CurrencySelectForm extends ActionForm {

    private String currencyCodeAndBankId;
    private String coinId;
    private String gameId;
    private String gamesLength;
    private boolean mustShow = false;
    private ArrayList configuredGames;


    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGamesLength() {
        return gamesLength;
    }

    public void setGamesLength(String gamesLength) {
        this.gamesLength = gamesLength;
    }

    public boolean isMustShow() {
        return mustShow;
    }

    public void setMustShow(boolean mustShow) {
        this.mustShow = mustShow;
    }

    public ArrayList getConfiguredGames() {
        return configuredGames;
    }

    public void setConfiguredGames(ArrayList configuredGames) {
        this.configuredGames = configuredGames;
    }

    public String getCurrencyCodeAndBankId() {
        return currencyCodeAndBankId;
    }

    public void setCurrencyCodeAndBankId(String currencyCodeAndBankId) {
        this.currencyCodeAndBankId = currencyCodeAndBankId;
    }

    public String getCoinId() {
        return coinId;
    }

    public void setCoinId(String coinId) {
        this.coinId = coinId;
    }
}
