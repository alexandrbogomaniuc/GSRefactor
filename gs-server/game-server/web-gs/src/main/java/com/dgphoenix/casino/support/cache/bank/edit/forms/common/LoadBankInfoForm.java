package com.abs.casino.support.cache.bank.edit.forms.common;

import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.bank.Limit;
import com.abs.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public class LoadBankInfoForm extends ActionForm {
    private static Logger LOG = LogManager.getLogger(LoadBankInfoForm.class);
    private String bankId;
    private String name;
    private String extId;
    private Limit limit;
    private Currency defaultCurrency;
    private List<LabelValueBean> currencies;
    private List<String> coins;
    private String currencyCodeAndBankId;
    private boolean mustShow = false;
    private List<String> configuredGames;

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = new ActionErrors();

        String strBankId = BaseAction.extractRequestParameterIgnoreCase(request, "bankId");

        if (StringUtils.isTrimmedEmpty(strBankId)) {
            actionErrors.add("bankId", new ActionMessage("error.loadBankInfo.emptyId"));
            return actionErrors;
        }
        Long bankId;
        try {
            bankId = Long.parseLong(strBankId);
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo == null) {
                actionErrors.add("bankId", new ActionMessage("error.loadBankInfo.noSuchBank", "ID", strBankId));
            } else {
                //noinspection ResultOfMethodCallIgnored
                bankInfo.toString();
            }
        } catch (Exception e) {
            LOG.error("BankInfo is wrong: " + strBankId, e);
            actionErrors.add("bankId", new ActionMessage("error.loadBankInfo.incorrect", "ID", strBankId));
        }

        return actionErrors;
    }

    public String getCurrencyCodeAndBankId() {
        return currencyCodeAndBankId;
    }

    public void setCurrencyCodeAndBankId(String currencyCodeAndBankId) {
        this.currencyCodeAndBankId = currencyCodeAndBankId;
    }

    public boolean isMustShow() {
        return mustShow;
    }

    public void setMustShow(boolean mustShow) {
        this.mustShow = mustShow;
    }

    public List<String> getConfiguredGames() {
        return configuredGames;
    }

    public void setConfiguredGames(List<String> configuredGames) {
        this.configuredGames = configuredGames;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(Currency defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public List<LabelValueBean> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<LabelValueBean> currencies) {
        this.currencies = currencies;
    }

    public List getCoins() {
        return coins;
    }

    public void setCoins(List coins) {
        this.coins = coins;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }
}
