package com.abs.casino.support.cache.bank.edit.actions.common;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.abs.casino.common.cache.data.game.ImmutableBaseGameInfoWrapper;
import com.dgphoenix.casino.common.configuration.messages.MessageManager;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.support.cache.bank.edit.forms.common.GameViewBean;
import com.abs.casino.support.cache.bank.edit.forms.common.LoadBankInfoForm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class CurrencySelectAction extends Action {
    private final static Logger LOG = LogManager.getLogger(CurrencySelectAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        LoadBankInfoForm loadBankInfoForm = (LoadBankInfoForm) form;

        String ccabi = loadBankInfoForm.getCurrencyCodeAndBankId();
        StringTokenizer tokenizer = new StringTokenizer(ccabi, "/");
        String currencyCode = tokenizer.nextToken();
        long bankId = Long.parseLong(tokenizer.nextToken());

        Currency currency = CurrencyCache.getInstance().get(currencyCode);
        Set<Long> games = BaseGameCache.getInstance().getAllGamesSet(bankId, currency);
        List<String> confGames = new ArrayList<>();

        List<GameViewBean> gameViewBeans = new ArrayList<>();

        for (Long id : games) {
            IBaseGameInfo baseGameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, id, currency);
            //skip games configured on master
            if (baseGameInfo instanceof ImmutableBaseGameInfoWrapper) {
                continue;
            }
            String normalGameName = null;
            try {
                normalGameName = MessageManager.getInstance().getApplicationMessage("game.name." +
                        baseGameInfo.getName());
            } catch (Exception e) {
                LOG.error("Cannot load message property name, gameId=" + id + ", gameName=" + baseGameInfo.getName());
            }
            if (StringUtils.isTrimmedEmpty(normalGameName) ||
                    "null".equalsIgnoreCase(normalGameName.trim().toLowerCase())) {
                normalGameName = baseGameInfo.getName();
            }
            GameViewBean gameViewBean = new GameViewBean();
            gameViewBean.setDescr(normalGameName + "  (id = " + id + ")");
            String url = "/support/loadgameinfo.do?bankId=" + bankId +
                    "&curCode=" + currencyCode + "&gameId=" + id;
            gameViewBean.setUrl(url);
            gameViewBeans.add(gameViewBean);
        }
        Collections.sort(gameViewBeans, new Comparator<GameViewBean>() {
            @Override
            public int compare(GameViewBean o1, GameViewBean o2) {
                return o1.getDescr().compareToIgnoreCase(o2.getDescr());
            }
        });

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        LoadBankInfoAction.fillBankInfoForm(loadBankInfoForm, bankInfo);
        request.setAttribute("gameList", gameViewBeans);
        loadBankInfoForm.setConfiguredGames(confGames);
        loadBankInfoForm.setMustShow(true);
        loadBankInfoForm.setBankId(request.getParameter("bankId"));

        return mapping.findForward("success");

    }

}
