package com.abs.casino.support.gamehistory;

import com.abs.casino.account.AccountManager;
import com.abs.casino.common.SessionHelper;
import com.abs.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.web.history.GameHistoryListAction;
import com.abs.casino.web.history.GameHistoryListEntry;
import com.abs.casino.web.history.GameHistoryListForm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * User: van0ss
 * Date: 05.04.2016
 */
public class GameHistorySupportAction extends GameHistoryListAction {
    private static final Logger LOG = LogManager.getLogger(GameHistorySupportAction.class);

    @Override
    public ActionForward process(ActionMapping mapping, GameHistoryListForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        LOG.debug("process form:" + form.toString());
        Long gameId = null;
        if (form.getGameId() != null && form.getGameId() != GameHistoryListForm.ALL_GAMES) {
            gameId = form.getGameId();
        }
        List<GameHistoryListEntry> result = new ArrayList<>();

        String currencySymbol;
        GameHistorySupportForm supportForm = (GameHistorySupportForm) form;
        SessionHelper.getInstance().lock(supportForm.getAccountId());
        try {
            SessionHelper.getInstance().openSession();
            SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
            if (sessionInfo != null) {
                filterOnlineSession(form, gameId, request, result);
            }
            currencySymbol = AccountManager.getInstance().getAccountInfo(supportForm.getAccountId()).getCurrency().getSymbol();
            SessionHelper.getInstance().markTransactionCompleted();
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }

        populateHistory(form, request, gameId, supportForm.getAccountId(), result, currencySymbol);
        return mapping.findForward(SUCCESS_FORWARD);
    }
}
