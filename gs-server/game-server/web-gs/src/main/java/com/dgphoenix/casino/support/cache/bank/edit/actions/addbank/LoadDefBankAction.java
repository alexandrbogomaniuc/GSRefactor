package com.dgphoenix.casino.support.cache.bank.edit.actions.addbank;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.CoinsCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.LimitsCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.cache.data.language.LanguageType;
import com.dgphoenix.casino.support.cache.bank.edit.forms.addBank.NewBankForm;
import com.dgphoenix.casino.support.cache.bank.edit.forms.editproperties.BankPropertiesForm;
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

public class LoadDefBankAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        NewBankForm bankForm = (NewBankForm) form;

        List<LabelValueBean> adaptedCurrencyList = new ArrayList<LabelValueBean>();
        for (Object object : CurrencyCache.getInstance().getAllObjects().values()) {
            Currency currency = (Currency) object;
            adaptedCurrencyList.add(new LabelValueBean(currency.getCode(), currency.getCode()));
        }
        bankForm.setAllCurrencies(adaptedCurrencyList);

        List<LabelValueBean> adaptedLimitList = new ArrayList<LabelValueBean>();
        for (Limit limit : LimitsCache.getInstance().getAll()) {
            adaptedLimitList.add(
                    new LabelValueBean(
                            "min=" + limit.getMinValue() + ", max=" + limit.getMaxValue(),
                            String.valueOf(limit.getId())
                    )
            );
        }
        bankForm.setAllLimits(adaptedLimitList);

        List<LabelValueBean> adaptedLangList = new ArrayList<LabelValueBean>();
        for (LanguageType langType : LanguageType.values()) {
            adaptedLangList.add(new LabelValueBean(langType.getCode(), langType.getCode()));
        }
        bankForm.setAllLanguages(adaptedLangList);

        List<Coin> allCoins = new ArrayList<Coin>(CoinsCache.getInstance().getAll());
        Collections.sort(allCoins, new Comparator<Coin>() {
            @Override
            public int compare(Coin o1, Coin o2) {
                return (int) (o1.getValue() - o2.getValue());
            }
        });
        List<LabelValueBean> adaptedCoins = new ArrayList<LabelValueBean>();
        for (Coin coin : allCoins) {
            adaptedCoins.add(new LabelValueBean(String.valueOf(coin.getValue()), String.valueOf(coin.getId())));
        }

        bankForm.setAllCoins(adaptedCoins);

        List<LabelValueBean> allBanks = new ArrayList<LabelValueBean>();
        for (BankInfo bankInfo : BankInfoCache.getInstance().getAllObjects().values()) {
            allBanks.add(new LabelValueBean(bankInfo.getExternalBankIdDescription() + " (id=" + bankInfo.getId() + ")",
                    String.valueOf(bankInfo.getId())));
        }
        bankForm.setAllBanks(allBanks);
        bankForm.setGames(new ArrayList<LabelValueBean>());
        bankForm.clearBankData();

        bankForm.setBpsForm(new BankPropertiesForm());

        return mapping.findForward("success");
    }
}

