package com.abs.casino.support.cache.bank.edit.forms.addBank;

import com.abs.casino.support.cache.bank.edit.forms.editproperties.BankPropertiesForm;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;


public class NewBankForm extends ActionForm {

    private String subcasinoId;

    private String id;
    private String extId;
    private String name;

    private String defCurCode;
    private String limitId;
    private String[] coinIds;
    private String[] currencyCodes;
    private String[] gameIds;
    private String defLang;

    private Collection<LabelValueBean> allCoins;
    private Collection<LabelValueBean> allCurrencies;
    private Collection<LabelValueBean> allLimits;
    private Collection<LabelValueBean> allLanguages;
    private Collection<LabelValueBean> allBanks;
    private Collection<LabelValueBean> games;

    private String copyBankId;

    private BankPropertiesForm bpsForm;

    public void clearBankData() {
        defCurCode = null;
        limitId = null;
        coinIds = null;
        currencyCodes = null;
        gameIds = null;
        defLang = null;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        coinIds = null;
        currencyCodes = null;
        gameIds = null;
    }

    public Collection<LabelValueBean> getGames() {
        return games;
    }

    public void setGames(Collection<LabelValueBean> games) {
        this.games = games;
    }

    public String[] getGameIds() {
        return gameIds;
    }

    public void setGameIds(String[] gameIds) {
        this.gameIds = gameIds;
    }

    public Collection<LabelValueBean> getAllBanks() {
        return allBanks;
    }

    public void setAllBanks(Collection<LabelValueBean> allBanks) {
        this.allBanks = allBanks;
    }

    public String getCopyBankId() {
        return copyBankId;
    }

    public void setCopyBankId(String copyBankId) {
        this.copyBankId = copyBankId;
    }

    public Collection<LabelValueBean> getAllCoins() {
        return allCoins;
    }

    public void setAllCoins(Collection<LabelValueBean> allCoins) {
        this.allCoins = allCoins;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<LabelValueBean> getAllLanguages() {
        return allLanguages;
    }

    public void setAllLanguages(Collection<LabelValueBean> allLanguages) {
        this.allLanguages = allLanguages;
    }

    public Collection<LabelValueBean> getAllCurrencies() {
        return allCurrencies;
    }

    public void setAllCurrencies(Collection<LabelValueBean> allCurrencies) {
        this.allCurrencies = allCurrencies;
    }

    public Collection<LabelValueBean> getAllLimits() {
        return allLimits;
    }

    public void setAllLimits(Collection<LabelValueBean> allLimits) {
        this.allLimits = allLimits;
    }

    public String getSubcasinoId() {
        return subcasinoId;
    }

    public void setSubcasinoId(String subcasinoId) {
        this.subcasinoId = subcasinoId;
    }

    public String getDefCurCode() {
        return defCurCode;
    }

    public void setDefCurCode(String defCurCode) {
        this.defCurCode = defCurCode;
    }

    public String getLimitId() {
        return limitId;
    }

    public void setLimitId(String limitId) {
        this.limitId = limitId;
    }

    public String[] getCoinIds() {
        return coinIds;
    }

    public void setCoinIds(String[] coinIds) {
        this.coinIds = coinIds;
    }

    public String[] getCurrencyCodes() {
        return currencyCodes;
    }

    public void setCurrencyCodes(String[] currencyCodes) {
        this.currencyCodes = currencyCodes;
    }

    public String getDefLang() {
        return defLang;
    }

    public void setDefLang(String defLang) {
        this.defLang = defLang;
    }

    public BankPropertiesForm getBpsForm() {
        return bpsForm;
    }

    public void setBpsForm(BankPropertiesForm bpsForm) {
        this.bpsForm = bpsForm;
    }
}
