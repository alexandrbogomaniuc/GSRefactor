package com.abs.casino.web.history;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraAccountInfoPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.configuration.messages.MessageManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.MismatchSessionException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.DigitFormatter;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.common.web.GameServerHost;
import com.dgphoenix.casino.gs.GameServer;
import com.abs.casino.gs.managers.game.history.HistoryManager;
import com.abs.casino.gs.web.messages.GsMessageManager;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.commons.lang.LocaleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.dgphoenix.casino.cassandra.persist.CassandraGameSessionPersister.*;
import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

/**
 * User: flsh
 * Date: 16.04.2009
 */
public class GameHistoryListAction extends BaseAction<GameHistoryListForm> {
    private static final Logger LOG = LogManager.getLogger(GameHistoryListAction.class);
    public static final String GAME_HISTORY_LIST = "GAME_HISTORY_LIST";
    public static final String GAMES_LIST = "GAMES";
    public static final String SESSION_ID = "sessionId";
    public static final String BANK_ID = "BANK_ID";
    public static final String CURRENT_TIME = "CURRENT_TIME";
    public static final String TOTALS = "TOTALS";
    public static final String SUBTOTALS = "SUBTOTALS";
    public static final String CURRENCY_SYMBOL = "CURRENCY_SYMBOL";
    public static final String PARAM_TIME_OFFSET = "timeOffset";

    // key: serverId+bankId
    private static final Map<Long, String> gsUrlsMap = new ConcurrentHashMap<>();
    private static final long MAGIC_DEFAULT_VALUE_KEY = Long.MIN_VALUE;

    private final CassandraAccountInfoPersister accountInfoPersister;

    public GameHistoryListAction() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        accountInfoPersister = persistenceManager.getPersister(CassandraAccountInfoPersister.class);
    }

    public ActionForward process(ActionMapping mapping, GameHistoryListForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        LOG.debug("GameHistoryListAction :: process form: {}", form.toString());
        if (form.getGameId() != null) {
            Long gameId = (form.getGameId() == GameHistoryListForm.ALL_GAMES) ? null : form.getGameId();
            boolean testMode = GameServerConfiguration.getInstance().isStressTestMode();
            Long accountId;
            AccountInfo accountInfo;
            List<GameHistoryListEntry> result = new ArrayList<>();
            if (testMode) {
                Pair<Integer, String> pair = StringIdGenerator.extractBankAndExternalUserId(form.getSessionId());
                Integer bankId = pair.getKey();
                String externalId = pair.getValue();
                accountInfo = accountInfoPersister.getByCompositeKey(bankId,
                        externalId);
                if (accountInfo == null) {
                    LOG.error("Cannot load account: sessionId={}, bankId={}, externalId={}",
                            form.getSessionId(), bankId, externalId);
                    throw new CommonException("Account not found");
                }
                accountId = accountInfo.getId();
            } else {
                SessionHelper.getInstance().lock(form.getSessionId());
                try {
                    SessionHelper.getInstance().openSession();
                    SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
                    if (sessionInfo == null || !sessionInfo.getSessionId().equals(form.getSessionId())) {
                        LOG.error("Session error, form.getSessionId()={}, found session={}",
                                form.getSessionId(), sessionInfo);
                        addError(request, "error.login.sessionExpired");
                        return mapping.findForward(ERROR_FORWARD);
                    }
                    filterOnlineSession(form, gameId, request, result);
                    accountId = sessionInfo.getAccountId();
                    accountInfo = AccountManager.getInstance().getAccountInfo(accountId);
                    SessionHelper.getInstance().markTransactionCompleted();
                } catch (MismatchSessionException e) {
                    LOG.error("Failed to request with error: {}", e.getMessage());
                    addError(request, "error.login.sessionExpired");
                    return mapping.findForward(ERROR_FORWARD);
                } finally {
                    SessionHelper.getInstance().clearWithUnlock();
                }
            }
            populateHistory(form, request, gameId, accountId, result, accountInfo.getCurrency().getSymbol());
            LOG.debug("GameHistoryListAction :: populated form: {}", form.toString());
        }

        return mapping.findForward(SUCCESS_FORWARD);
    }

    protected void filterOnlineSession(GameHistoryListForm form, Long gameId,
                                       HttpServletRequest request,
                                       List<GameHistoryListEntry> result) throws CommonException {
        Date startDate = form.getStartDate();
        GameSession onlineSession = SessionHelper.getInstance().getTransactionData().getGameSession();
        if (onlineSession != null && (gameId == null || onlineSession.getGameId() == gameId)
                && gameSessionFilterByMode(onlineSession, form.getMode())
                && onlineSession.getStartTime() > startDate.getTime()) {
            result.add(convert(onlineSession, request, form, true));
        }
    }

    private boolean gameSessionFilterByMode(GameSession onlineSession, int mode) {
        boolean isFrbonus = onlineSession.getFrbonusId() != null;
        boolean isBonus = onlineSession.getBonusId() != null;
        if (!onlineSession.isRealMoney()) {
            return false;
        }
        switch (mode) {
            case MODE_REAL:
                return !isFrbonus && !isBonus;
            case MODE_BONUS:
                return isBonus;
            case MODE_FR_BONUS:
                return isFrbonus;
            default:
                return true;
        }
    }

    protected void populateHistory(GameHistoryListForm form, HttpServletRequest request, Long gameId, Long accountId,
                                   List<GameHistoryListEntry> result, String currencySymbol) throws CommonException {
        Date startDate = form.getStartDate();
        Date endDate = form.getEndDate();
        int sessionsCount = (int) HistoryManager.getInstance().getGameSessionsCount(accountId,
                gameId, startDate, endDate, form.getMode());
        form.setCount(sessionsCount);
        List<GameSession> gameSessions = HistoryManager.getInstance().getGameSessionList(accountId,
                gameId, startDate, endDate,
                form.getOffset(), form.getItemsPerPage(), form.getMode());
        Pair<Long, Long> subTotals = HistoryManager.getInstance().calculateTotals(gameSessions);
        Pair<Long, Long> totals = HistoryManager.getInstance().getGameSessionsTotals(accountId,
                gameId, startDate, endDate, form.getMode());

        Long lastGameSessionDate = null;
        if (!CollectionUtils.isEmpty(gameSessions)) {
            for (GameSession gameSession : gameSessions) {
                GameHistoryListEntry gameHistoryListEntry = convert(gameSession, request, form, false);
                result.add(gameHistoryListEntry);
                lastGameSessionDate = gameSession.getEndTime() != null ?
                        gameSession.getEndTime() :
                        gameSession.getStartTime();
            }
        }

        request.setAttribute(GAME_HISTORY_LIST, result);
        request.setAttribute(BANK_ID, form.getBankId());
        request.setAttribute(TOTALS, totals);
        request.setAttribute(SUBTOTALS, subTotals);
        request.setAttribute(CURRENCY_SYMBOL, currencySymbol);
        form.setLastGameSessionDateOnPage(lastGameSessionDate == null ? 0 : lastGameSessionDate);
    }

    protected GameHistoryListEntry convert(GameSession gameSession, HttpServletRequest request, GameHistoryListForm form,
                                           boolean isOnline) throws CommonException {
        GameHistoryListEntry result = createEntry();
        result.setSessionId(gameSession.getId());
        result.setOnline(Boolean.toString(isOnline));
        setGameTitle(gameSession, result, form.getLang());
        setEndDate(gameSession, request, form, isOnline, result);
        result.setIncome(DigitFormatter.doubleToMoney(((double) gameSession.getIncome()) / 100));
        result.setPayout(DigitFormatter.doubleToMoney(((double) gameSession.getPayout()) / 100));
        result.setRevenue(DigitFormatter.doubleToMoney(((double) gameSession.getPayout() - gameSession.getIncome()) / 100));
        setHistoryUrl(gameSession, request, form, isOnline, result);

        return result;
    }

    private void setHistoryUrl(GameSession gameSession, HttpServletRequest request, GameHistoryListForm form, boolean isOnline, GameHistoryListEntry result) {
        DateFormat dateFormat = getDateFormat("format.date.gamehistory", request);

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(gameSession.getBankId());
        long historyOffset = bankInfo.getHistoryOffsetInclDst(System.currentTimeMillis());

        result.setHistoryUrl("/vabs/show.jsp?" + GameHistoryServlet.PARAM_VIEW_SESSID + "=" +
                gameSession.getId() + "&" + GameHistoryServlet.PARAM_START_DATE + "=" + dateFormat
                .format(form.getStartDate()) +
                "&" + GameHistoryServlet.PARAM_END_DATE + "=" + dateFormat.format(form.getEndDate())
                + "&GAMEID=" + gameSession.getGameId()
                + ((historyOffset != 0) ? "&" + PARAM_TIME_OFFSET + "=" + historyOffset : "")
                + (isOnline ? "&online=true&" + GameHistoryServlet.PARAM_SESSION + "=" + form.getSessionId() : "")
                + (!isTrimmedEmpty(form.getLang()) ? "&LANG=" + form.getLang() : "")
        );
    }

    private void setEndDate(GameSession gameSession, HttpServletRequest request, GameHistoryListForm form, boolean isOnline, GameHistoryListEntry result) {
        DateFormat listDateFormat = getDateFormat("format.date.gamehistory.full", request);
        result.setStartDate(listDateFormat.format(form.applyOffset(gameSession.getStartTime())));
        if (isOnline) {
            String online = GsMessageManager.getInstance()
                    .getApplicationMessage(parseLocale(request), "report.online");
            result.setEndDate(online);
        } else {
            Long endTime = gameSession.getEndTime();
            result.setEndDate(endTime == null ? "-" : listDateFormat.format(form.applyOffset(endTime)));
        }
    }

    private Locale parseLocale(HttpServletRequest request) {
        Locale locale = request.getLocale();
        try {
            locale = Optional.ofNullable(request.getParameter("lang"))
                    .map(LocaleUtils::toLocale)
                    .orElse(locale);
        } catch (Exception e) {
            // ignore
        }
        return locale;
    }

    private void setGameTitle(GameSession gameSession, GameHistoryListEntry result, String lang) {
        String gameName = BaseGameInfoTemplateCache.getInstance().getGameNameById(gameSession.getGameId());
        result.setGameName(gameName);
        String titleFromTemplate = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameSession.getGameId()).getTitle();
        String title = MessageManager.getLocalizedTitleOrDefault(gameSession.getBankId(), gameSession.getGameId(), lang);
        result.setLocalizedGameName(isTrimmedEmpty(title) ? titleFromTemplate : title);
    }

    protected GameHistoryListEntry createEntry() {
        return new GameHistoryListEntry();
    }

    protected String getGsUrl(long bankId) throws CommonException {
        long key = MAGIC_DEFAULT_VALUE_KEY;
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo != null && (bankInfo.isReplaceStartServerName() || bankInfo.isReplaceEndServerName())) {
            key = bankId;
        }
        String url = gsUrlsMap.get(key);
        if (url == null) {
            url = GameServerHost.getHost(GameServer.getInstance().getHost(), GameServer.getInstance().getServerId(), bankInfo);
            gsUrlsMap.put(key, url);
        }
        return url;
    }
}
