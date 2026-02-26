package com.abs.casino.support.cache.bank.edit.forms.common;

import org.apache.struts.action.ActionForm;
import org.apache.struts.util.LabelValueBean;

import java.util.Collection;

public class AddGameForm extends ActionForm {

    private String inputModeOfId = "";
    private String gameId;
    private String bankId;
    private Collection<LabelValueBean> banksWithSelectedGame;
    private boolean mustShowBanks = false;
    private String selectedBankId;
    private String configGameStatus;
    private String createGameStatus;


    private String gameName;
    private String gameType;
    private String gameGroup;
    private String gameVariableType;
    private String rmClassName;
    private String spClassName;
    private String limitId;
    private String createJackpot;
    private String pcrp;
    private String bcrp;
    private String[] coinIds;

    //properties

    private String payoutPercent;
    private String jackpotMultiplier;
    private String chipValues;
    private String isEnabled;
    private String keyAcsEnabled;
    private String maxBetTime;
    private String defCoin;
    private String maxBet1;
    private String maxBet2;
    private String maxBet3;
    private String maxBet4;
    private String maxBet5;
    private String maxBet6;
    private String maxBet12;
    private String maxBet18;
    private String acsBankLimit;
    private String acsBankSum;
    private String imageURL;
    private String gameTesting;


    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getGameTesting() {
        return gameTesting;
    }

    public void setGameTesting(String gameTesting) {
        this.gameTesting = gameTesting;
    }

    public String getCreateGameStatus() {
        return createGameStatus;
    }

    public void setCreateGameStatus(String createGameStatus) {
        this.createGameStatus = createGameStatus;
    }

    public String getPayoutPercent() {
        return payoutPercent;
    }

    public void setPayoutPercent(String payoutPercent) {
        this.payoutPercent = payoutPercent;
    }

    public String getJackpotMultiplier() {
        return jackpotMultiplier;
    }

    public void setJackpotMultiplier(String jackpotMultiplier) {
        this.jackpotMultiplier = jackpotMultiplier;
    }

    public String getChipValues() {
        return chipValues;
    }

    public void setChipValues(String chipValues) {
        this.chipValues = chipValues;
    }

    public String getEnabled() {
        return isEnabled;
    }

    public void setEnabled(String enabled) {
        isEnabled = enabled;
    }

    public String getKeyAcsEnabled() {
        return keyAcsEnabled;
    }

    public void setKeyAcsEnabled(String keyAcsEnabled) {
        this.keyAcsEnabled = keyAcsEnabled;
    }

    public String getDefCoin() {
        return defCoin;
    }

    public void setDefCoin(String defCoin) {
        this.defCoin = defCoin;
    }

    public String getMaxBet1() {
        return maxBet1;
    }

    public void setMaxBet1(String maxBet1) {
        this.maxBet1 = maxBet1;
    }

    public String getMaxBet2() {
        return maxBet2;
    }

    public void setMaxBet2(String maxBet2) {
        this.maxBet2 = maxBet2;
    }

    public String getMaxBet3() {
        return maxBet3;
    }

    public void setMaxBet3(String maxBet3) {
        this.maxBet3 = maxBet3;
    }

    public String getMaxBet4() {
        return maxBet4;
    }

    public void setMaxBet4(String maxBet4) {
        this.maxBet4 = maxBet4;
    }

    public String getMaxBet5() {
        return maxBet5;
    }

    public void setMaxBet5(String maxBet5) {
        this.maxBet5 = maxBet5;
    }

    public String getMaxBet6() {
        return maxBet6;
    }

    public void setMaxBet6(String maxBet6) {
        this.maxBet6 = maxBet6;
    }

    public String getMaxBet12() {
        return maxBet12;
    }

    public void setMaxBet12(String maxBet12) {
        this.maxBet12 = maxBet12;
    }

    public String getMaxBet18() {
        return maxBet18;
    }

    public void setMaxBet18(String maxBet18) {
        this.maxBet18 = maxBet18;
    }

    public String getAcsBankLimit() {
        return acsBankLimit;
    }

    public void setAcsBankLimit(String acsBankLimit) {
        this.acsBankLimit = acsBankLimit;
    }

    public String getAcsBankSum() {
        return acsBankSum;
    }

    public void setAcsBankSum(String acsBankSum) {
        this.acsBankSum = acsBankSum;
    }

    public String getPcrp() {
        return pcrp;
    }

    public void setPcrp(String pcrp) {
        this.pcrp = pcrp;
    }

    public String getBcrp() {
        return bcrp;
    }

    public void setBcrp(String bcrp) {
        this.bcrp = bcrp;
    }

    public String getCreateJackpot() {
        return createJackpot;
    }

    public void setCreateJackpot(String createJackpot) {
        this.createJackpot = createJackpot;
    }

    public String[] getCoinIds() {
        return coinIds;
    }

    public void setCoinIds(String[] coinIds) {
        this.coinIds = coinIds;
    }

    public String getLimitId() {
        return limitId;
    }

    public void setLimitId(String limitId) {
        this.limitId = limitId;
    }

    public String getRmClassName() {
        return rmClassName;
    }

    public void setRmClassName(String rmClassName) {
        this.rmClassName = rmClassName;
    }

    public String getSpClassName() {
        return spClassName;
    }

    public void setSpClassName(String spClassName) {
        this.spClassName = spClassName;
    }

    public String getGameVariableType() {
        return gameVariableType;
    }

    public void setGameVariableType(String gameVariableType) {
        this.gameVariableType = gameVariableType;
    }

    public String getGameGroup() {
        return gameGroup;
    }

    public void setGameGroup(String gameGroup) {
        this.gameGroup = gameGroup;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getConfigGameStatus() {
        return configGameStatus;
    }

    public void setConfigGameStatus(String configGameStatus) {
        this.configGameStatus = configGameStatus;
    }

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

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getInputModeOfId() {
        return inputModeOfId;
    }

    public void setInputModeOfId(String inputModeOfId) {
        this.inputModeOfId = inputModeOfId;
    }
}
