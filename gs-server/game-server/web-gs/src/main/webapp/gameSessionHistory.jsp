<%@ page import="com.dgphoenix.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.session.GameSession" %>
<%@ page import="com.abs.casino.common.util.CalendarUtils" %>
<%@ page import="com.abs.casino.common.util.DigitFormatter" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.common.util.xml.xmlwriter.XmlWriter" %>
<%@ page import="java.text.ParseException" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="org.apache.commons.lang.time.DateUtils" %>
<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraAccountInfoPersister" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraGameSessionPersister" %>
<%@ page import="com.abs.casino.common.util.string.StringBuilderWriter" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%--
http://lobby-default.xxx.com/gameSessionHistory.jsp?date=2015/04/16&bankId=271&subCasinoId=58&hash=8244215b5c577ea7a4acc58a525133f9
http://default-gp3.xxx.com/gameSessionHistory.jsp?date=2015/04/16&bankId=271&subCasinoId=58&hash=8244215b5c577ea7a4acc58a525133f9
  Created by IntelliJ IDEA.
  User: zhevlakoval
  Date: 15.07.14
  Time: 16:59

  Fwd: 0003112: Enet - INTEGRATION AND SUPPORT - 2
  Wed, 16 Jul 2014 11:09:49 +0700

> > <?xml version="1.0" encoding="UTF-8"?>
> > <root>
> > <date>2014/07/10</date> <!--yyyy/MM/dd-->
> > <row>
> > <userid>1773292</userid>
> > <gameid>3</gameid>
> > <currency>eur</currency>
> > <betsamount>22.25</betsamount>
> > <win>35.81</win>
> > <bets>14</bets>
> > </row>
> > <root>

--%>
<%!
    public static final String TAG_ROOT = "root";
    public static final String TAG_DATE = "date";
    public static final String TAG_SHIFT_HOUR = "shifthour";
    public static final String TAG_ROW = "row";
    public static final String TAG_USER_ID = "userid";
    public static final String TAG_GAME_ID = "gameid";
    public static final String TAG_BANK_ID = "bankid";
    public static final String TAG_CURRENCY = "currency";
    public static final String TAG_BETS_AMOUNT = "betsamount";
    public static final String TAG_WIN = "win";
    public static final String TAG_BETS = "bets";
    public static final String TAG_GAME_SESSION_ID = "gamesessionid";
    public static final String TAG_END_DATE = "enddate";
    public static final String TAG_ERROR = "error";
    public static final String TAG_BONUS_ID = "bonusid";
    public static final String TAG_FREE_ROUND_BONUS_ID = "frbonusid";

    public static final Long SUBCASINO_ENET = 5L;
    HashMap<Long, String> accounts = new HashMap<Long, String>();
    private final CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);

%>
<%@ page contentType="text/xml;charset=UTF-8" language="java" %>
<%
    CassandraGameSessionPersister gameSessionPersister;
    accounts = new HashMap<>();
    StringBuilderWriter stringWriter = new StringBuilderWriter();
    // generate XML response
    XmlWriter xmlWriter = new XmlWriter(stringWriter);
    xmlWriter.header();
    xmlWriter.startNode(TAG_ROOT);

    int shiftHour = 0;
    if (request.getParameter("shiftHour") != null) {
        try {
            shiftHour = Integer.parseInt(String.valueOf(request.getParameter("shiftHour")));
        } catch (Exception e) {
            ThreadLog.error("gameSessionHistory.jsp incorect shiftHour " + request.getParameter("shiftHour"), e);
        }
    }
    Long subCasinoId = null;
    if (request.getParameter("subCasinoId") != null) {
        subCasinoId = Long.parseLong(String.valueOf(request.getParameter("subCasinoId")));
    }
    Long bankId = null;
    if (request.getParameter("bankId") != null) {
        bankId = Long.parseLong(String.valueOf(request.getParameter("bankId")));
    }
    boolean valid = true;
    if (bankId == null && subCasinoId == null) {
        xmlWriter.node(TAG_ERROR, "incorrect params");
        valid = false;
    }
    if (valid) {
        if (bankId == null && !subCasinoId.equals(SUBCASINO_ENET)) {
            xmlWriter.node(TAG_ERROR, "incorrect params");
            valid = false;
        }
    }
    if (valid) {
        if (request.getParameter("date") == null) {
            xmlWriter.node(TAG_ERROR, "date must be specified");
        } else {
            String dateStr = String.valueOf(request.getParameter("date"));
            Date startDate = null;
            Date endDate = null;
            boolean correctDate = true;
            try {
                startDate = getStartDate(dateStr);
                endDate = getEndDate(dateStr);
            } catch (Exception ex) {
                xmlWriter.node(TAG_ERROR, "date format is wrong " + dateStr + " (expected yyyy/MM/dd)");
                correctDate = false;
            }
            if (correctDate) {
                String hash = null;
                if (request.getParameter("hash") != null) {
                    hash = String.valueOf(request.getParameter("hash"));
                }
                if (hash == null) {
                    xmlWriter.node(TAG_ERROR, "hash must be specified");
                } else {
                    String hashCalc = getHash(subCasinoId, bankId, dateStr);
                    if (hashCalc == null || !hash.equals(hashCalc)) {
                        xmlWriter.node(TAG_ERROR, "hash is incorrect " + hash);
                    } else {

                        xmlWriter.node(TAG_DATE, dateStr);
                        if (shiftHour != 0) {
                            startDate = shiftDate(startDate, shiftHour);
                            endDate = shiftDate(endDate, shiftHour);
                            xmlWriter.node(TAG_SHIFT_HOUR, String.valueOf(shiftHour));
                        }
                        if (bankId != null) {
                            addGameSessionByBank(xmlWriter, bankId, startDate, endDate);
                        } else {
                            List<Long> banks = SubCasinoCache.getInstance().getBankIds(subCasinoId);
                            for (Long bankIdIndex : banks) {
                                addGameSessionByBank(xmlWriter, bankIdIndex, startDate, endDate);
                            }
                        }
                    }
                }
            }
        }
    }
    xmlWriter.endNode(TAG_ROOT);

    response.getWriter().write(stringWriter.toString());
    response.getWriter().flush();
%>
<%!

    private void addGameSessionByBank(XmlWriter xmlWriter, Long bankId, Date startDate, Date endDate)
            throws CommonException {
        CassandraGameSessionPersister gameSessionPersister =
                persistenceManager.getPersister(CassandraGameSessionPersister.class);
        List<Long> gameSessionIds = gameSessionPersister.getBankGameSessionsIds(bankId, null, startDate, endDate);
        List<GameSession> gameSessions = gameSessionPersister.getGameSessions(gameSessionIds);

/*
        List<GameSession> gameSessions = DBGameSessionManager.getInstance().getBankGameSessionList(
                bankId, startDate, endDate, null
        );
*/
        if (gameSessions != null && !gameSessions.isEmpty()) {
            for (GameSession gameSession : gameSessions) {
                if (gameSession != null) {
                    if (gameSession.getPayout() > 0 || gameSession.getIncome() > 0) {
                        xmlWriter.startNode(TAG_ROW);

                        xmlWriter.node(TAG_BANK_ID, String.valueOf(bankId));
                        xmlWriter.node(TAG_USER_ID, getExternalId(gameSession.getAccountId()));

                        xmlWriter.node(TAG_GAME_ID, String.valueOf(gameSession.getGameId()));

                        xmlWriter.node(TAG_CURRENCY,
                                gameSession.getCurrency() != null ? gameSession.getCurrency().getCode() : "");

                        xmlWriter.node(TAG_BETS_AMOUNT,
                                DigitFormatter.doubleToMoney(((double) gameSession.getIncome()) / 100));

                        xmlWriter.node(TAG_WIN,
                                DigitFormatter.doubleToMoney(((double) gameSession.getPayout()) / 100));

                        xmlWriter.node(TAG_BETS, String.valueOf(gameSession.getBetsCount()));

                        xmlWriter.node(TAG_GAME_SESSION_ID, String.valueOf(gameSession.getId()));

                        xmlWriter.node(TAG_END_DATE, getDateStr(gameSession.getEndTime()));

                        if (gameSession.getBonusId() != null && gameSession.getBonusId() > 0) {
                            xmlWriter.node(TAG_BONUS_ID, String.valueOf(gameSession.getBonusId()));
                        }

                        if (gameSession.getFrbonusId() != null && gameSession.getFrbonusId() > 0) {
                            xmlWriter.node(TAG_FREE_ROUND_BONUS_ID, String.valueOf(gameSession.getFrbonusId()));
                        }

                        xmlWriter.endNode(TAG_ROW);
                    }
                }
            }
        }
    }


    private String getHash(Long subCasinoId, Long bankId, String dateStr) {
        if (bankId == null) {
            if (subCasinoId != null) {
                Long defaultBankId = SubCasinoCache.getInstance().getDefaultBankId(subCasinoId);
                if (defaultBankId != null) {
                    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(defaultBankId);
                    if (bankInfo != null && bankInfo.getAuthPassword() != null && !bankInfo.getAuthPassword().isEmpty()) {
                        return StringUtils.getMD5(dateStr + bankInfo.getAuthPassword());
                    }
                }
            }
        } else {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo != null && bankInfo.getAuthPassword() != null && !bankInfo.getAuthPassword().isEmpty()) {
                return StringUtils.getMD5(dateStr + bankInfo.getAuthPassword());
            }
        }
        return null;
    }

    private String getDateStr(Long time) {
        String date = "";
        if (time != null) {
            date = new SimpleDateFormat("HH:mm:ss").format(new Date(time));
        }
        return date;
    }

    private String getExternalId(long accountId) {
        if (!accounts.containsKey(accountId)) {
            try {
                CassandraAccountInfoPersister accountInfoPersister =
                        persistenceManager.getPersister(CassandraAccountInfoPersister.class);
                AccountInfo accountInfo = accountInfoPersister.get(accountId);
                if (accountInfo != null && accountInfo.getExternalId() != null) {
                    accounts.put(accountId, accountInfo.getExternalId());
                }
            } catch (Exception e) {
                ThreadLog.error("gameSessionHistory.jsp ", e);
            }
        }
        String extId = accounts.get(accountId);
        if (extId != null) {
            return extId;
        }
        return "";
    }

    private Date getStartDate(String date) throws ParseException {
        Date startDate = parceDate(date);
        Calendar day = CalendarUtils.getStartDay(startDate);
        return day.getTime();
    }

    private Date getEndDate(String date) throws ParseException {
        Date endDate = parceDate(date);
        Calendar day = CalendarUtils.getEndDay(endDate);
        return day.getTime();
    }

    private Date shiftDate(Date date, int shiftHour) {
        return DateUtils.addHours(date, shiftHour);
    }

    private Date parceDate(String dateText) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.parse(dateText);
    }
%>