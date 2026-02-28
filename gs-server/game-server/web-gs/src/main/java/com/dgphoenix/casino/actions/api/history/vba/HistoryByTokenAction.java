package com.abs.casino.actions.api.history.vba;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraGameSessionPersister;
import com.dgphoenix.casino.cassandra.persist.CassandraHistoryTokenPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.Triple;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.gs.persistance.bet.PlayerBetPersistenceManager;
import com.abs.casino.web.history.GameHistoryServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.apache.struts.action.ActionMessages.GLOBAL_MESSAGE;

public class HistoryByTokenAction extends Action {
    private static final Logger LOG = LogManager.getLogger(HistoryByTokenAction.class);

    private static final String TOKEN_PARAM = "token";
    private static final String LANG_PARAM = "LANG";
    private static final String TIMEZONE_PARAM = "TIMEZONE";

    private final CassandraHistoryTokenPersister historyTokenPersister;
    private final CassandraGameSessionPersister gameSessionPersister;
    private final PlayerBetPersistenceManager betPersistenceManager;

    public HistoryByTokenAction() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        historyTokenPersister = persistenceManager.getPersister(CassandraHistoryTokenPersister.class);
        gameSessionPersister = persistenceManager.getPersister(CassandraGameSessionPersister.class);
        betPersistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("playerBetPersistenceManager", PlayerBetPersistenceManager.class);
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        try {
            LOG.debug("Start process request, URI = {} , query string = {}", request.getRequestURI(),
                    request.getQueryString());
            String lang = request.getParameter(LANG_PARAM);
            if (!StringUtils.isTrimmedEmpty(lang)) {
                request.getSession().setAttribute(Globals.LOCALE_KEY, new Locale(lang));
            }
            ActionMessages errors = getErrors(request);
            String token = request.getParameter(TOKEN_PARAM);
            if (StringUtils.isTrimmedEmpty(token)) {
                LOG.error("Token is missing");
                errors.add(GLOBAL_MESSAGE, new ActionMessage("error.common.missingParameter", TOKEN_PARAM));
                saveErrors(request, errors);
                return mapping.findForward("common_error");
            }
            Long roundId = historyTokenPersister.getRoundId(token);
            if (roundId == null) {
                LOG.error("Token={} not found or expired", token);
                errors.add(GLOBAL_MESSAGE, new ActionMessage("error.history.tokenNotFound", token));
                saveErrors(request, errors);
                return mapping.findForward("common_error");
            }
            Triple<List<Long>, Long, Long> result = betPersistenceManager.getGameSessionsByRoundId(roundId);
            if (result == null) {
                LOG.error("RoundId={} not found in persister", roundId);
                return mapping.findForward("not_found_error");
            }

            LOG.debug("Persister results:{}", result);
            String url = getUrl(roundId, result, request);
            LOG.debug("Final URL:{}", url);
            return new ActionForward(url, true);
        } catch (Exception ex) {
            LOG.error("Exception ", ex);
            return mapping.findForward("common_error");
        }
    }

    private String getUrl(long roundId, Triple<List<Long>, Long, Long> result, HttpServletRequest request) throws CommonException {
        String url = request.getScheme() + "://" + request.getServerName();
        Long gameSessionId = Collections.max(result.first());
        Long accountId = result.third();
        String lang = request.getParameter(LANG_PARAM);
        String additionalParams = getGameSessionParams(gameSessionId, accountId, lang);
        url += "/vabs/show.jsp?"
                + GameHistoryServlet.PARAM_VIEW_SESSID + "=" + gameSessionId + "&"
                + GameHistoryServlet.PARAM_ROUND_ID + "=" + roundId + "&"
                + GameHistoryServlet.PARAM_GAME_ID + "=" + result.second() + "&"
                + "SHOW_EXT_BET_ID=true" + additionalParams;
        String timeZone = request.getParameter(TIMEZONE_PARAM);
        if (!StringUtils.isTrimmedEmpty(timeZone)) {
            url += "&TIMEZONE=" + timeZone;
        }
        return url;
    }

    private String getGameSessionParams(Long gameSessionId, Long accountId, String lang) throws CommonException {
        SessionHelper.getInstance().lock(accountId);
        String additionalParams = "";
        String langParam = "";
        try {
            SessionHelper.getInstance().openSession();
            SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
            GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
            if (sessionInfo != null && gameSession != null && gameSessionId.equals(gameSession.getId())) {
                additionalParams = "&online=true&" + GameHistoryServlet.PARAM_SESSION + "=" + sessionInfo.getSessionId();
                langParam = gameSession.getLang();
                LOG.debug("GameSession is online: {}", gameSession);
            }
            SessionHelper.getInstance().markTransactionCompleted();
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
        if (StringUtils.isTrimmedEmpty(additionalParams)) {
            GameSession gameSession = gameSessionPersister.get(gameSessionId);
            if (gameSession != null) {
                langParam = gameSession.getLang();
            }
        }
        if (!StringUtils.isTrimmedEmpty(lang)) {
            langParam = lang;
        }
        if (!StringUtils.isTrimmedEmpty(langParam)) {
            additionalParams += "&LANG=" + langParam;
        }
        return additionalParams;
    }
}
