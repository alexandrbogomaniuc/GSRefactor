package com.dgphoenix.casino.support.cache.bank.edit.actions.common;

import com.dgphoenix.casino.common.cache.CoinsCache;
import com.dgphoenix.casino.common.cache.LimitsCache;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.dgphoenix.casino.common.util.DigitFormatter;
import com.dgphoenix.casino.support.cache.bank.edit.forms.common.AwayFromBankInfoForm;
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

public class InputModeAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        AwayFromBankInfoForm afbiForm = (AwayFromBankInfoForm) form;
        List<Limit> limits = LimitsCache.getInstance().getAll();
        ArrayList<LabelValueBean> adaptedLimits = new ArrayList<LabelValueBean>();
        for (Limit limit : limits) {
            adaptedLimits.add(
                    new LabelValueBean(
                            "min=" + limit.getMinValue() + " max=" + limit.getMaxValue(),
                            String.valueOf(limit.getId()))
            );
        }

        List<Coin> coins = CoinsCache.getInstance().getAll();
        List<Coin> sortedCoins = generateSortedCoins(coins);


        ArrayList<LabelValueBean> adaptedCoins = new ArrayList<LabelValueBean>();
        for (Coin coin : sortedCoins) {
            adaptedCoins.add(
                    new LabelValueBean("" +
                            DigitFormatter.doubleToMoney(coin.getValue() / 100.0d),
                            String.valueOf(coin.getId()))
            );
        }

        afbiForm.setAllLimits(adaptedLimits);
        afbiForm.setAllCoins(adaptedCoins);

        return mapping.findForward("success");
    }

    private List<Coin> generateSortedCoins(List<Coin> coins) {
        List<Coin> sortedCoins = new ArrayList<Coin>(coins);
        Collections.sort(sortedCoins, new Comparator<Coin>() {
            @Override
            public int compare(Coin o1, Coin o2) {
                if (o1.getValue() > o2.getValue()) {
                    return 1;
                } else if (o1.getValue() == o2.getValue()) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        return sortedCoins;
    }

}
