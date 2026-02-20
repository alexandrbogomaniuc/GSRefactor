package com.dgphoenix.casino.support.cache.bank.edit.actions.common;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.support.cache.bank.edit.forms.common.AwayFromBankInfoForm;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AwayFromBankInfoAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String forward = request.getParameter("forward");
        if (forward.equals("subcasinoSelect")) return mapping.findForward("subcasino");

        AwayFromBankInfoForm afbiForm = (AwayFromBankInfoForm) form;
        long bankId = Long.parseLong(afbiForm.getBankId());
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        Currency defaultCurrency = bankInfo.getDefaultCurrency();

        Set<Long> allGameIdsSetCach = BaseGameInfoTemplateCache.getInstance().getAllGameIds();
        Set<Long> bankGameIdsSet = BaseGameCache.getInstance().getAllGamesSet(bankId, defaultCurrency);
        HashSet<Long> allGameIdsSet = new HashSet<Long>();
        long maxId = 0;
        for (Long gameId : allGameIdsSetCach) {
            if (gameId > maxId) maxId = gameId;
            allGameIdsSet.add(gameId);
        }

        allGameIdsSet.removeAll(bankGameIdsSet);
        ArrayList<LabelValueBean> gameIdsList = new ArrayList<LabelValueBean>();
        for (Long gameId : allGameIdsSet) {
            gameIdsList.add(new LabelValueBean("Game id: " + gameId.toString(), gameId.toString()));
        }
        afbiForm.setGameIds(gameIdsList);
        afbiForm.setCurrentMaxGameId(String.valueOf(maxId));
        afbiForm.setInputModeOfId("empty");
        return mapping.findForward("addGame");
    }

}
