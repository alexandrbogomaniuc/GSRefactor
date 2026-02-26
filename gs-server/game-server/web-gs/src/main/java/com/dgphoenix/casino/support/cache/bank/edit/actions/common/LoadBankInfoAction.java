package com.dgphoenix.casino.support.cache.bank.edit.actions.common;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.abs.casino.support.cache.bank.edit.forms.common.LoadBankInfoForm;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class LoadBankInfoAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        LoadBankInfoForm bankInfoForm = (LoadBankInfoForm) form;
        long bankId = Long.parseLong(bankInfoForm.getBankId());
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);

        fillBankInfoForm(bankInfoForm, bankInfo);
        return mapping.findForward("success");
    }

    public static void fillBankInfoForm(LoadBankInfoForm form, BankInfo bankInfo) {

        form.setExtId(bankInfo.getExternalBankId());
        form.setName(bankInfo.getExternalBankIdDescription());
        form.setLimit(bankInfo.getLimit());
        form.setDefaultCurrency(bankInfo.getDefaultCurrency());

        List<Coin> sortedCoins = new ArrayList<>();
        List<Coin> bankInfoCoins = bankInfo.getCoins();
        if (bankInfoCoins != null && !bankInfoCoins.isEmpty()) {
            sortedCoins.addAll(bankInfoCoins);
            Collections.sort(sortedCoins, new Comparator<Coin>() {
                @Override
                public int compare(Coin o1, Coin o2) {
                    return (int) (o1.getValue() - o2.getValue());
                }
            });
        }

        List<String> coins = new ArrayList<>();
        for (Coin coin : sortedCoins) {
            coins.add("COIN [id=" + coin.getId() + ", value=" + coin.getValue() + "]");
        }
        form.setCoins(coins);

        List<LabelValueBean> adaptedCurrencies = new ArrayList<>();
        for (Currency currency : bankInfo.getCurrencies()) {
            adaptedCurrencies.add(new LabelValueBean(currency.toString(), currency.getCode() + "/" + bankInfo.getId()));
        }
        form.setCurrencies(adaptedCurrencies);
        form.setMustShow(false);
    }

}
