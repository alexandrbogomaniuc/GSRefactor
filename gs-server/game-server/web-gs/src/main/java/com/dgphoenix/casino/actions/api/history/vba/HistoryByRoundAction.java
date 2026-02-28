package com.abs.casino.actions.api.history.vba;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.Triple;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.gs.persistance.bet.PlayerBetPersistenceManager;
import com.abs.casino.web.history.GameHistoryListAction;
import com.abs.casino.web.history.GameHistoryServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

/**
 * Created by inter on 19.06.15.
 */
public class HistoryByRoundAction extends Action {
    private static final Logger LOG = LogManager.getLogger(HistoryByRoundAction.class);

    private static String ROUNDID_PARAM = "ROUNDID";
    private static String LANG_PARAM = "LANG";
    private static String WHOLE_SESSION = "WHOLE_SESSION";

    private final PlayerBetPersistenceManager betPersistenceManager;

    public HistoryByRoundAction() {
        betPersistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("playerBetPersistenceManager", PlayerBetPersistenceManager.class);
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        try {
            LOG.info("Start process request, URI = {} , query string = {}", request.getRequestURI(),
                    request.getQueryString());
            String strRoundId = request.getParameter(ROUNDID_PARAM);
            long roundId = Long.parseLong(strRoundId.trim());
            Triple<List<Long>, Long, Long> result = betPersistenceManager.getGameSessionsByRoundId(roundId);
            if (result == null) {
                LOG.warn("result == null");
                return mapping.findForward("not_found_error");
            }
            LOG.info("Persister results: " + result);
            boolean isWholeSession = isWholeSession(request);
            String url = getUrl(strRoundId, result, isWholeSession, request);
            LOG.info("Final URL: " + url);
            return new ActionForward(url, true);
        } catch (Exception ex) {
            LOG.warn("Exception ", ex);
            return mapping.findForward("common_error");
        }
    }

    private String getUrl(String strRoundId, Triple<List<Long>, Long, Long> result, boolean isWholeSession,
                          HttpServletRequest request) throws CommonException {
        String url = request.getScheme() + "://" + request.getServerName();
        Long gameSessionId = Collections.max(result.first());
        Long accountId = result.third();
        String sOnline = accountId != null ? getOnline(gameSessionId, accountId) : "";
        String sTimeOffset = accountId != null ? getTimeOffset(accountId) : "";
        if (isWholeSession) {
            url += "/vabs/show.jsp?" + GameHistoryServlet.PARAM_VIEW_SESSID + "=" + gameSessionId +
                    "&GAMEID=" + result.second() + sTimeOffset;
        } else {
            url += "/vabs/show.jsp?" + GameHistoryServlet.PARAM_VIEW_SESSID + "=" + gameSessionId +
                    "&ROUNDID=" + strRoundId + "&GAMEID=" + result.second() +
                    (isHideBalance() ? "&HIDE_BALANCE=true" : "" + sOnline + sTimeOffset);
        }
        String lang = request.getParameter(LANG_PARAM);
        if (lang != null) {
            url += "&LANG=" + lang;
        }
        return url;
    }

    private String getOnline(Long gameSessionId, Long accountId) throws CommonException {
        SessionHelper.getInstance().lock(accountId);
        String sOnline = "";
        try {
            SessionHelper.getInstance().openSession();
            SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
            GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
            if (sessionInfo != null && gameSession != null && gameSessionId.equals(gameSession.getId())) {
                sOnline = "&online=true&" + GameHistoryServlet.PARAM_SESSION + "=" + sessionInfo.getSessionId();
                LOG.info("GameSession is online: " + gameSession);
            }
            SessionHelper.getInstance().markTransactionCompleted();
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
        return sOnline;
    }

    private String getTimeOffset(Long accountId) throws CommonException {
        AccountInfo accountInfo = AccountManager.getInstance().getByAccountId(accountId);
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
        long historyOffset = bankInfo.getHistoryOffsetInclDst(System.currentTimeMillis());
        return historyOffset != 0 ? ("&" + GameHistoryListAction.PARAM_TIME_OFFSET + "=" + historyOffset) : "";
    }

    private boolean isWholeSession(HttpServletRequest request) {
        String wholeSession = request.getParameter(WHOLE_SESSION);
        return !StringUtils.isTrimmedEmpty(wholeSession) && Boolean.parseBoolean(wholeSession);
    }

    public boolean isHideBalance() {
        return false;
    }
}
