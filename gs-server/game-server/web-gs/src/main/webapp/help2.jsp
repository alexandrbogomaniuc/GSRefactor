<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.CurrencyCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.Coin" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameConstants" %>
<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.abs.casino.common.util.web.HttpClientConnection" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.io.File" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="static com.abs.casino.common.util.DigitFormatter.doubleToMoney" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page import="com.abs.casino.common.cache.data.bank.ILimit" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final String[] TEMPLATES = {"%MINWAGER%", "%MAXWAGER%", "%COINS%", "%DEFCOIN%", "%BETPERLINE%", "%BETPERSPIN%",
            "%MAX_BET_1%", "%MAX_BET_2%", "%MAX_BET_3%", "%MAX_BET_4%", "%MAX_BET_6%", "%MAX_BET_12%", "%MAX_BET_18%"};

    static final String[] MAX_BET_PROPS = {"MAX_BET_1", "MAX_BET_2", "MAX_BET_3", "MAX_BET_4", "MAX_BET_6", "MAX_BET_12", "MAX_BET_18"};

    public static String formatCoins(List<Coin> coins) {
        if (coins == null) return "";
        return coins.stream()
                .map(input -> doubleToMoney((double) input.getValue() / 100))
                .collect(Collectors.joining(", "));
    }
%>
<%
    String params = request.getParameter("PARAMS");
    StringTokenizer st = new StringTokenizer(params, ";");
    Long gameId = Long.valueOf(st.nextToken());
    Long bankId = Long.valueOf(st.nextToken());
    String currencyCode = st.nextToken();
    String lang = st.nextToken();
    Currency currency = CurrencyCache.getInstance().get(currencyCode);
    IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, currency);
    String defaultBet = null;
    Integer defaultCoin = gameInfo.getDefaultCoin();
    List<Coin> coins = gameInfo.getCoins();

    if (defaultCoin != null && coins != null && !coins.isEmpty()) {
        if (defaultCoin >= coins.size()) {
            ThreadLog.warn("help2.jsp: defaultCoin bad value: " + defaultCoin);
            defaultCoin = 0;
        }
        try {
            Coin coin = coins.get(defaultCoin);
            defaultBet = doubleToMoney(((double) coin.getValue()) / 100);
        } catch (Exception e) {
            ThreadLog.error("Cannot find defaultBet: defaultCoin=" + defaultCoin + ", coins.suze=" + coins.size());
        }
    }
    String defaultBetPerLine = gameInfo.getProperty(BaseGameConstants.KEY_DEFAULTBETPERLINE);


    String coinsString = formatCoins(gameInfo.getCoins());
    String lobbyUrl = request.getScheme() + "://" + request.getServerName();
    String help_page = lobbyUrl + File.separator + "help" + File.separator + lang + File.separator + gameId + ".html";
    String help_page_en = lobbyUrl + File.separator + "help" + File.separator + "en" + File.separator + gameId + ".html";
    String content = null;
    try {
        content = HttpClientConnection.newInstance().doRequest(help_page, "", false).toString();
    } catch (Exception e) {
        ThreadLog.error("help2.jsp: cannot load url: " + help_page + ", reason=" + e.getMessage());
    }

    if (content == null) {
        try {
            content = HttpClientConnection.newInstance().doRequest(help_page_en, "", false).toString();
        } catch (Exception e) {
            ThreadLog.error("help2.jsp: cannot load url: " + help_page_en + ", reason=" + e.getMessage());
        }
    }

    if (content != null) {
        String minWager = null;
        String maxWager = null;
        DecimalFormat df = new DecimalFormat("0.00");

        ILimit limit = gameInfo.getLimit();
        if (limit == null) {
            limit = BankInfoCache.getInstance().getBankInfo(bankId).getLimit();
        }
        if (limit != null) {
            minWager = df.format((double) limit.getMinValue() / 100);
            maxWager = df.format((double) limit.getMaxValue() / 100);
        }

        String betPerSpin = null;
        try {
            double minBetPerSpin = ((double) coins.get(0).getValue()) / 100;
            double maxBetPerSpin = ((double) coins.get(coins.size() - 1).getValue());
            betPerSpin = df.format(minBetPerSpin) + " - " + df.format(maxBetPerSpin);
        } catch (Exception e) {
            ThreadLog.warn("help2.jsp error", e);
        }
        defaultBetPerLine = defaultBetPerLine == null ? "1" : defaultBetPerLine;
        String[] mbValues = new String[MAX_BET_PROPS.length];

        for (int i = 0; i < MAX_BET_PROPS.length; i++) {
            String property = gameInfo.getProperty(MAX_BET_PROPS[i]);
            if (property != null) {
                try {
                    mbValues[i] = df.format(Double.parseDouble(property));
                } catch (Exception e) {
                    ThreadLog.debug("help2.jsp: error while parsing value: " + property +
                            ". GameId = " + gameId + ", bankId = " + bankId, e);
                    mbValues[i] = property;
                }
            }
        }
        String[] toReplaceWith = {minWager, maxWager, coinsString, defaultBet, defaultBetPerLine, betPerSpin,
                mbValues[0], mbValues[1], mbValues[2], mbValues[3], mbValues[4], mbValues[5], mbValues[6]};

        content = new String(StringUtils.replaceEach(content, TEMPLATES, toReplaceWith).getBytes("ISO-8859-1"), "UTF8");
        ThreadLog.debug("help2.jsp: Templates replaced");
        response.getWriter().println(content);
    }

%>
