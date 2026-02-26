package com.abs.casino.support.cache.bank.edit.actions.common;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.abs.casino.support.cache.bank.edit.forms.common.AddGameForm;
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
import java.util.Set;

public class GameIdSelectAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        AddGameForm gameInfoForm = (AddGameForm) form;
        long gameId = Long.parseLong(gameInfoForm.getGameId());

        Set<Long> allBankIds = BankInfoCache.getInstance().getBankIds();
        ArrayList<LabelValueBean> banksWithSelectGame = new ArrayList<LabelValueBean>();
        for (Long id : allBankIds) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(id);
            Currency defCurrency = bankInfo.getDefaultCurrency();
            if (BaseGameCache.getInstance().isExist(id, gameId, defCurrency)) {
                banksWithSelectGame.add(
                        new LabelValueBean("bank id = " + id.toString() +
                                " (def currency = " + defCurrency.getCode() + ")", id.toString()));
            }
        }

        if (banksWithSelectGame.isEmpty()) {
            banksWithSelectGame.add(new LabelValueBean("banks are not found", "notfound"));
        }

        Collections.sort(banksWithSelectGame, new Comparator<LabelValueBean>() {
            @Override
            public int compare(LabelValueBean o1, LabelValueBean o2) {
                long id1 = Long.parseLong(o1.getValue());
                long id2 = Long.parseLong(o2.getValue());
                return (int) (id1 - id2);
            }
        });
        gameInfoForm.setBanksWithSelectedGame(banksWithSelectGame);
        gameInfoForm.setMustShowBanks(true);
        return mapping.findForward("success");
    }


}
