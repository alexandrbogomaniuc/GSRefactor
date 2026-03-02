package com.abs.casino.support.cache.bank.edit.forms.common;

import com.abs.casino.common.cache.BaseGameCache;
import com.abs.casino.common.cache.CurrencyCache;
import com.abs.casino.common.cache.data.game.IBaseGameInfo;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public class GameInfoForm extends ActionForm {

    private String gameName;
    private String gameId;
    private String bankId;
    private String currencyCode;
    private String gameType;
    private String gameGroup;
    private String gameVariableType;
    private String rmClassName;
    private String gsClassName;
    private String minLimitValue;
    private String maxLimitValue;
    private String bankLimit;

    private String jackpotId;
    private String pcrp;
    private String bcrp;
    private String[] coinIds;
    private String[] languages;
    private String servletName;

    //properties
    private List<LabelValueBean> properties;
    private Map<String, String> templateProperties;
    private String[] removeList;
    private String[] resetList;

    private String payoutPercent;
    private String jackpotMultiplier;
    private String chipValues;
    private String maxBetTime;
    private String defCoin;
    private String acsBankLimit;
    private String acsBankSum;
    private String imageURL;
    private String newPropKey;

    private String newPropValue;
    private boolean isEnabled;
    private boolean keyAcsEnabled;

    private boolean gameTesting;
    private boolean newProperty;
    private boolean saveAllGamesByBank;

    private boolean acceptServletNameForSubcasino;
    private String[] allCurrencyPropertyList;

    //---collection for view
    private List<LabelValueBean> coins;
    private List<String> bankCoins;
    private List<String> langList;

    private Long externalGameId;

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        String strBankId = BaseAction.extractRequestParameterIgnoreCase(request, "bankId");

        if (StringUtils.isTrimmedEmpty(strBankId)) {
            errors.add("bankId", new ActionMessage("error.gameInfoForm.emptyBankId", "ID", strBankId));
        }

        String strGameId = BaseAction.extractRequestParameterIgnoreCase(request, "gameId");
        if (StringUtils.isTrimmedEmpty(strGameId)) {
            errors.add("gameId", new ActionMessage("error.gameInfoForm.emptyGameId", "ID", strGameId));
        }

        long bankId = -1;
        try {
            bankId = Long.parseLong(strBankId);
        } catch (NumberFormatException e) {
            errors.add("bankId", new ActionMessage("error.gameInfoForm.incorrectBankId", "ID", strBankId));
        }

        long gameId = -1;
        try {
            gameId = Long.parseLong(strGameId);
        } catch (NumberFormatException e) {
            errors.add("gameId", new ActionMessage("error.gameInfoForm.incorrectGameId", "ID", strGameId));
        }

        String curCode = BaseAction.extractRequestParameterIgnoreCase(request, "curCode");
        if (StringUtils.isTrimmedEmpty(curCode)) {
            errors.add("curCode", new ActionMessage("error.gameInfoForm.emptyCurCode"));
        }

        try {
            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId,
                    CurrencyCache.getInstance().get(curCode));
            gameInfo.toString();
        } catch (Exception e) {
            errors.add("gameInfo", new ActionMessage("error.gameInfoForm.noSuchGameInfo", bankId, gameId, curCode));
        }

        return errors;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        isEnabled = false;
        keyAcsEnabled = false;
        gameTesting = false;
        newProperty = false;
        saveAllGamesByBank = false;
        acceptServletNameForSubcasino = false;
        coinIds = null;
        languages = null;
        removeList = null;
        allCurrencyPropertyList = null;
    }

    public String[] getAllCurrencyPropertyList() {
        return allCurrencyPropertyList;
    }

    public void setAllCurrencyPropertyList(String[] allCurrencyPropertyList) {
        this.allCurrencyPropertyList = allCurrencyPropertyList;
    }

    public boolean isAcceptServletNameForSubcasino() {
        return acceptServletNameForSubcasino;
    }

    public void setAcceptServletNameForSubcasino(boolean acceptServletNameForSubcasino) {
        this.acceptServletNameForSubcasino = acceptServletNameForSubcasino;
    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servlet) {
        this.servletName = servlet;
    }

    public boolean isSaveAllGamesByBank() {
        return saveAllGamesByBank;
    }

    public void setSaveAllGamesByBank(boolean saveAllGameByCurrency) {
        this.saveAllGamesByBank = saveAllGameByCurrency;
    }

    public String getNewPropKey() {
        return newPropKey;
    }

    public void setNewPropKey(String newPropKey) {
        this.newPropKey = newPropKey;
    }

    public String getNewPropValue() {
        return newPropValue;
    }

    public void setNewPropValue(String newPropValue) {
        this.newPropValue = newPropValue;
    }

    public boolean isNewProperty() {
        return newProperty;
    }

    public void setNewProperty(boolean newProperty) {
        this.newProperty = newProperty;
    }

    public void removeProperty(String index) {
        properties.remove(Integer.parseInt(index));
    }

    public String[] getRemoveList() {
        return removeList;
    }

    public void setRemoveList(String[] removeList) {
        this.removeList = removeList;
    }

    public List<LabelValueBean> getProperties() {
        return properties;
    }

    public void setProperties(List<LabelValueBean> properties) {
        this.properties = properties;
    }

    public List<LabelValueBean> getCoins() {
        return coins;
    }

    public void setCoins(List<LabelValueBean> coins) {
        this.coins = coins;
    }

    public List<String> getLangList() {
        return langList;
    }

    public void setLangList(List<String> langList) {
        this.langList = langList;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String[] getLanguages() {
        return languages;
    }

    public void setLanguages(String[] languages) {
        this.languages = languages;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getGameGroup() {
        return gameGroup;
    }

    public void setGameGroup(String gameGroup) {
        this.gameGroup = gameGroup;
    }

    public String getGameVariableType() {
        return gameVariableType;
    }

    public void setGameVariableType(String gameVariableType) {
        this.gameVariableType = gameVariableType;
    }

    public String getRmClassName() {
        return rmClassName;
    }

    public void setRmClassName(String rmClassName) {
        this.rmClassName = rmClassName;
    }

    public String getGsClassName() {
        return gsClassName;
    }

    public void setGsClassName(String gsClassName) {
        this.gsClassName = gsClassName;
    }

    public String getJackpotId() {
        return jackpotId;
    }

    public void setJackpotId(String jackpotId) {
        this.jackpotId = jackpotId;
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

    public String[] getCoinIds() {
        return coinIds;
    }

    public void setCoinIds(String[] coinIds) {
        this.coinIds = coinIds;
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

    public String getMaxBetTime() {
        return maxBetTime;
    }

    public void setMaxBetTime(String maxBetTime) {
        this.maxBetTime = maxBetTime;
    }

    public String getDefCoin() {
        return defCoin;
    }

    public void setDefCoin(String defCoin) {
        this.defCoin = defCoin;
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

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getMinLimitValue() {
        return minLimitValue;
    }

    public void setMinLimitValue(String minLimitValue) {
        this.minLimitValue = minLimitValue;
    }

    public String getMaxLimitValue() {
        return maxLimitValue;
    }

    public void setMaxLimitValue(String maxLimitValue) {
        this.maxLimitValue = maxLimitValue;
    }

    public String getBankLimit() {
        return bankLimit;
    }

    public void setBankLimit(String bankLimit) {
        this.bankLimit = bankLimit;
    }

    public List<String> getBankCoins() {
        return bankCoins;
    }

    public void setBankCoins(List<String> bankCoins) {
        this.bankCoins = bankCoins;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isKeyAcsEnabled() {
        return keyAcsEnabled;
    }

    public void setKeyAcsEnabled(boolean keyAcsEnabled) {
        this.keyAcsEnabled = keyAcsEnabled;
    }

    public boolean isGameTesting() {
        return gameTesting;
    }

    public void setGameTesting(boolean gameTesting) {
        this.gameTesting = gameTesting;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Long getExternalGameId() {
        return externalGameId;
    }

    public void setExternalGameId(Long externalGameId) {
        this.externalGameId = externalGameId;
    }

    public Map<String, String> getTemplateProperties() {
        return templateProperties;
    }

    public void setTemplateProperties(Map<String, String> templateProperties) {
        this.templateProperties = templateProperties;
    }

    public String[] getResetList() {
        return resetList;
    }

    public void setResetList(String[] resetList) {
        this.resetList = resetList;
    }
}
