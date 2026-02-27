package com.abs.casino.web.history;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraTempBetPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.MismatchSessionException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.Triple;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.gs.managers.game.history.HistoryManager;
import com.dgphoenix.casino.gs.persistance.bet.PlayerBetPersistenceManager;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.ArchiveBetTools;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;
import static com.google.common.base.Preconditions.checkState;

public class GameHistoryServlet extends HttpServlet {
    private static final Logger LOG = LogManager.getLogger(GameHistoryServlet.class);

    public static final String PARAM_VIEW_SESSID = "VIEWSESSID";
    public static final String PARAM_START_DATE = "STARTDATE";
    public static final String PARAM_END_DATE = "ENDDATE";
    public static final String PARAM_SESSION = "SESSION";
    public static final String PARAM_GAME_NAME = "GAMENAME";
    public static final String PARAM_TIME_ZONE = "TIMEZONE";
    public static final String PARAM_IS_ONLINE = "online";
    public static final String PARAM_ROUND_ID = "ROUNDID";
    public static final String PARAM_GAME_ID = "GAMEID";

    private static final String PARAM_START_RECORD = "STARTRECORD";
    private static final String PARAM_RECORDS = "RECORDS";
    private static final String PARAM_CMD = "CMD";

    private static final String CMD_GET_INFO = "GETINFO";
    private static final String RESULT_OK = "OK";
    private static final String RESULT_ERROR = "ERROR";

    private static final long EMPTY_VALUE = -1;

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private final CassandraTempBetPersister tempBetPersister;
    private final PlayerBetPersistenceManager betPersistenceManager;

    public GameHistoryServlet() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        tempBetPersister = persistenceManager.getPersister(CassandraTempBetPersister.class);
        betPersistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("playerBetPersistenceManager", PlayerBetPersistenceManager.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        ServletOutputStream out = response.getOutputStream();
        try {
            String strCmd = getMandatoryParameter(request, PARAM_CMD);
            processCommands(strCmd, request, out);

        } catch (Exception e) {
            LOG.error("Error during processing command", e);
            out.print(GSON.toJson(new GetGameInfoResponse(RESULT_ERROR)));
        }
    }

    private void processCommands(String strCmd, HttpServletRequest request, ServletOutputStream out) throws IOException {
        switch (strCmd) {
            case CMD_GET_INFO:
                processGetInfo(request, out);
                break;

            default:
                throw new RuntimeException("Unknown command: " + strCmd);
        }
    }

    private void processGetInfo(HttpServletRequest request, ServletOutputStream out) throws IOException {
        HistoryManager historyManager = HistoryManager.getInstance();

        GetGameInfoResponse response;
        try {
            long gameSessionId = -1;
            String gameSessionIdAsString = request.getParameter(PARAM_VIEW_SESSID);
            if (gameSessionIdAsString != null) {
                gameSessionId = Long.parseLong(gameSessionIdAsString);
            }

            boolean showOnline = isShowOnlineSession(request);

            String roundIdAsString = request.getParameter(PARAM_ROUND_ID);
            long roundId = -1;
            List<Long> gameSessionsList = null;
            if (roundIdAsString != null) {
                roundId = Long.parseLong(roundIdAsString);
                if (gameSessionId == -1 && !showOnline) {
                    Triple<List<Long>, Long, Long> result =
                            betPersistenceManager.getGameSessionsByRoundId(roundId);
                    if (result != null) {
                        gameSessionsList = result.first();
                        for (long sid : gameSessionsList) {
                            if (historyManager.getHistoryGameSession(sid) != null) {
                                gameSessionId = sid;
                                break;
                            }
                        }
                    }
                    if (gameSessionId == -1) {
                        throw new RuntimeException("Session not found");
                    }
                }
            }

            String recordsAsString = getMandatoryParameter(request, PARAM_RECORDS);
            int recordsCount = Integer.parseInt(recordsAsString);

            String startRecordAsString = getMandatoryParameter(request, PARAM_START_RECORD);
            int startRecord = Integer.parseInt(startRecordAsString);

            String gameNameAsString = getMandatoryParameter(request, PARAM_GAME_NAME);
            String playerSessionId = null;
            if (showOnline) {
                playerSessionId = getMandatoryParameter(request, PARAM_SESSION);
            }
            Pair<GameSession, Boolean> gameSessionInfo = getGameSessionInfo(gameSessionId, showOnline, playerSessionId,
                    historyManager, roundId);
            GameSession gameSession = gameSessionInfo.getKey();
            showOnline = gameSessionInfo.getValue();

            checkState(gameSession != null, "GameSession isn't found");

            long accountId = gameSession.getAccountId();

            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(accountId);
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
            int baseHistoryOffset = bankInfo.getHistoryTimeOffset();
            ZoneId dstZone = bankInfo.getHistoryDSTZone();
            String paramTimeZone = request.getParameter(PARAM_TIME_ZONE);
            if (!StringUtils.isTrimmedEmpty(paramTimeZone)) {
                ZoneId zoneId = ZoneId.of(paramTimeZone);
                baseHistoryOffset = (int) TimeUnit.SECONDS.toMinutes(zoneId.getRules().getOffset(Instant.now()).getTotalSeconds())
                        + (int) TimeUnit.DAYS.toMinutes(1);
                dstZone = null;
            }

            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoByName(bankInfo.getId(), gameNameAsString);
            checkState(gameInfo != null, "Game with name='" + gameNameAsString + "' not found");

            response = new GetGameInfoResponse(RESULT_OK);

            boolean showOnlyRound = roundId != -1;

            Pair<Integer, List<PlayerBet>> betsList;
            if (showOnline) {
                List<PlayerBet> onlinePayerBets = tempBetPersister.getOnlinePayerBets(gameSession.getId());
                if (showOnlyRound) {
                    List<PlayerBet> roundBets = new ArrayList<>();
                    Triple<List<Long>, Long, Long> result = betPersistenceManager.getGameSessionsByRoundId(roundId);
                    if (result != null && result.first().size() > 1) {
                        for (Long sessionId : result.first()) {
                            if (sessionId != gameSession.getId()) {
                                Pair<Integer, List<PlayerBet>> betsListAndCount = historyManager
                                        .getBetsListAndCount(sessionId, startRecord, recordsCount, String.valueOf(roundId));
                                roundBets.addAll(betsListAndCount.getValue());
                            }
                        }
                    }
                    for (PlayerBet bet : onlinePayerBets) {
                        if (bet.getServletData().contains(ArchiveBetTools.ROUND_ID + "=" + roundId)) {
                            roundBets.add(bet);
                        }
                    }
                    betsList = new Pair<>(roundBets.size(), roundBets);
                } else {
                    betsList = new Pair<>(onlinePayerBets.size(), onlinePayerBets);
                }
            } else {
                if (gameSessionsList != null) {
                    List<PlayerBet> innerBetsList = new ArrayList<>();
                    for (Long sessId : gameSessionsList) {
                        Pair<Integer, List<PlayerBet>> betsListAndCount = historyManager
                                .getBetsListAndCount(sessId, startRecord, recordsCount, String.valueOf(roundId));
                        innerBetsList.addAll(betsListAndCount.getValue());
                    }
                    betsList = new Pair<>(innerBetsList.size(), innerBetsList);
                } else if (showOnlyRound) {
                    betsList = historyManager
                            .getBetsListAndCount(gameSessionId, startRecord, recordsCount, String.valueOf(roundId));
                } else {
                    betsList = historyManager.getBetsListAndCount(gameSessionId, startRecord, recordsCount);
                }
            }

            Integer betsCount = betsList.getKey();
            if (showOnlyRound && !showOnline) {
                betsCount = historyManager.getBetsCount(gameSessionId, roundId);
            }

            response.setTotalRecords(betsCount);
            int currentRecord = recordsCount == -1 || betsCount < startRecord + recordsCount
                    ? betsCount
                    : startRecord + recordsCount;
            response.setCurrentRecord(currentRecord);

            LOG.debug("gameSessionId={}, showOnlyRound = {} betsCount = {} roundId = {}", gameSessionId,
                    showOnlyRound, betsCount, roundId);

            for (PlayerBet bet : betsList.getValue()) {
                String gameStateName = GameStateNameResolver.getGameStateName(bet.getGameStateId());
                String strTime = parseDate(new Date(bet.getTime()
                        + getHistoryOffset(baseHistoryOffset, dstZone, bet.getTime())));

                response.addBet(strTime, bet.getGameStateId(), gameStateName, (double) bet.getBet() / 100,
                        (double) bet.getWin() / 100, (double) bet.getBalance() / 100, bet.getData(),
                        bet.getServletData(), bet.getExtBetId());
            }

            GameSession prevSession = historyManager.getPrevSession(gameSessionId);
            GameSession nextSession = historyManager.getNextSession(gameSessionId);

            if (showOnlyRound) {
                response.setPreviousSessionId(EMPTY_VALUE);
                response.setNextSessionId(EMPTY_VALUE);
            } else {
                long previousSessionId = prevSession != null && prevSession.getId() != gameSessionId
                        ? prevSession.getId()
                        : EMPTY_VALUE;
                long nextSessionId = nextSession != null && nextSession.getId() != gameSessionId
                        ? nextSession.getId()
                        : EMPTY_VALUE;
                response.setPreviousSessionId(previousSessionId);
                response.setNextSessionId(nextSessionId);
            }

            String fraction = gameSession.getCurrencyFraction();
            response.setCurrency(fraction != null ? fraction : gameSession.getCurrency().getCode());

            response.setAccountId(accountInfo.getExternalId());

            response.setBrandName(bankInfo.getCustomerBrandName());

        } catch (Exception e) {
            LOG.error("ProcessGetInfo:: error during getting game info", e);
            response = new GetGameInfoResponse(RESULT_ERROR);
        }

        out.print(GSON.toJson(response));
    }

    public long getHistoryOffset(int baseHistoryOffset, ZoneId dstZone, long millis) {
        long result = 0;
        if (baseHistoryOffset != 0) {
            result = TimeUnit.MINUTES.toMillis(baseHistoryOffset - TimeUnit.DAYS.toMinutes(1));
        }
        if (dstZone != null) {
            result += getDSTTimeInMillis(dstZone, millis);
        }
        return result;
    }

    private static String parseDate(Date time) {
        DateFormatSymbols dateSymbols = new DateFormatSymbols(Locale.US);

        Calendar objCalendar = Calendar.getInstance();
        objCalendar.setTime(time);

        int nDay = objCalendar.get(Calendar.DAY_OF_MONTH);
        int nMonth = objCalendar.get(Calendar.MONTH);
        int nYear = objCalendar.get(Calendar.YEAR);

        int nHour = objCalendar.get(Calendar.HOUR_OF_DAY);
        int nMinutes = objCalendar.get(Calendar.MINUTE);
        int nSeconds = objCalendar.get(Calendar.SECOND);

        return nDay + " " + dateSymbols.getShortMonths()[nMonth] + " " + nYear + " " +
                (nHour < 10 ? "0" : "") + nHour + ":" +
                (nMinutes < 10 ? "0" : "") + nMinutes + ":" +
                (nSeconds < 10 ? "0" : "") + nSeconds;
    }

    public static String getMandatoryParameter(HttpServletRequest request, String paramName) {
        String param = request.getParameter(paramName);
        if (isTrimmedEmpty(param)) {
            throw new RuntimeException("GameHistoryServlet: missing mandatory parameter: " + paramName);
        }
        return param;
    }

    public static boolean isShowOnlineSession(HttpServletRequest request) {
        String strShowOnline = request.getParameter(PARAM_IS_ONLINE);
        return "true".equalsIgnoreCase(strShowOnline);
    }

    /**
     * Returns offset for time zone according parameters in BankInfo (include daylight time)
     *
     * @param gameSessionId valid game session identifier
     * @return configured offset
     */
    public static long getHistoryAndVabOffset(long gameSessionId, boolean onlineSession, String playerSessionId) throws CommonException {
        HistoryManager historyManager = HistoryManager.getInstance();
        GameSession gameSession = getGameSessionInfo(gameSessionId, onlineSession, playerSessionId, historyManager, null)
                .getKey();
        if (gameSession == null) {
            LOG.warn("Can not found game session with id={}", gameSessionId);
            return 0;
        }
        long bankId = gameSession.getBankId();
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        long offset = bankInfo.getHistoryTimeOffset();
        if (offset == 0) {
            return offset;
        }
        offset -= TimeUnit.DAYS.toMinutes(1);
        ZoneId historyDSTZone = bankInfo.getHistoryDSTZone();
        if (historyDSTZone != null) {
            List<PlayerBet> playerBets = historyManager.getBetsListAndCount(gameSessionId, 0, 1).getValue();
            if (!playerBets.isEmpty()) {
                PlayerBet playerBet = playerBets.get(0);
                long time = playerBet.getTime();
                offset += getDSTTimeInMillis(historyDSTZone, time);
            }
        }
        return offset;
    }

    public static long getDSTTimeInMillis(ZoneId zoneId, long milliTime) {
        Duration daylightSavings = zoneId.getRules().getDaylightSavings(Instant.ofEpochMilli(milliTime));
        return TimeUnit.SECONDS.toMillis(daylightSavings.getSeconds());
    }

    public static Pair<GameSession, Boolean> getGameSessionInfo(long gameSessionId, boolean isOnlineSession,
                                                                String playerSessionId, HistoryManager historyManager,
                                                                Long roundId) throws CommonException {
        GameSession gameSession = null;
        if (isOnlineSession) {
            SessionHelper.getInstance().lock(playerSessionId);
            try {
                SessionHelper.getInstance().openSession();
                gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
                SessionHelper.getInstance().markTransactionCompleted();
            } catch (MismatchSessionException ignore) {
                LOG.debug("MismatchSessionException, gameSessionId: {}, playerSessionId = {}, roundId = {}",
                        gameSessionId, playerSessionId, roundId);
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
                //for cases when gameSession was closed after set isOnline=true and MismatchSessionException in SessionHelper.getInstance().openSession();
                if (gameSession == null) {
                    gameSession = historyManager.getHistoryGameSession(gameSessionId);
                    isOnlineSession = false;
                }
            }
        } else {
            gameSession = historyManager.getHistoryGameSession(gameSessionId);
        }
        return new Pair<>(gameSession, isOnlineSession);
    }

    private static class GetGameInfoResponse {

        private final String result;
        private Integer totalRecords;
        private Integer currentRecord;
        private List<PlayerBet> playerBets;
        private Long previousSessionId;
        private Long nextSessionId;
        private String currency;
        private String accountId;
        private String brandName;

        public GetGameInfoResponse(String result) {
            this.result = result;
        }

        public void addBet(String time, int stateId, String stateName, double bet, double win, double balance,
                           String betData, String servletData, String extBetId) {
            if (playerBets == null) {
                playerBets = new ArrayList<>();
            }
            playerBets.add(new PlayerBet(time, stateId, stateName, bet, win, balance, betData, servletData, extBetId));
        }

        public void setTotalRecords(Integer totalRecords) {
            this.totalRecords = totalRecords;
        }

        public void setCurrentRecord(Integer currentRecord) {
            this.currentRecord = currentRecord;
        }

        public void setPreviousSessionId(Long previousSessionId) {
            this.previousSessionId = previousSessionId;
        }

        public void setNextSessionId(Long nextSessionId) {
            this.nextSessionId = nextSessionId;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public void setBrandName(String brandName) { this.brandName = brandName; }

        private static class PlayerBet {
            private final String time;
            private final int stateId;
            private final String stateName;
            private final double bet;
            private final double win;
            private final double balance;
            private final String betData;
            private final String servletData;
            private final String extBetId;

            public PlayerBet(String time, int stateId, String stateName, double bet, double win, double balance,
                             String betData, String servletData, String extBetId) {
                this.time = time;
                this.stateId = stateId;
                this.stateName = stateName;
                this.bet = bet;
                this.win = win;
                this.balance = balance;
                this.betData = betData;
                this.servletData = servletData;
                this.extBetId = extBetId;
            }
        }
    }
}
