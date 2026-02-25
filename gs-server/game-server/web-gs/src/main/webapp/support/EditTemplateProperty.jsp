<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfo" %>
<%@ page import="com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.TreeMap" %>
<%--
  User: chrad
  Date: 11.01.19
  Time: 15:10
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    final String[] FIELDS = {
            "FAKE_ID_FOR",
            "LGA_APPROVED",
            "REDEFINED_JP_GAME_ID",
            "MAX_COIN_LIMIT_EUR",
            "JACKPOT3_GAME",
            "PAYOUT_PERCENT",
            "JACKPOT_MULTIPLIER",
            "CHIPVALUES",
            "ISENABLED",
            "KEY_ACS_ENABLED",
            "KEY_NEED_ACS",
            "DEFCOIN",
            "DEFAULTBETPERLINE",
            "DEFAULTNUMLINES",
            "MAX_BET_1",
            "MAX_BET_2",
            "MAX_BET_3",
            "MAX_BET_4",
            "MAX_BET_5",
            "MAX_BET_6",
            "MAX_BET_12",
            "MAX_BET_18",
            "ACS_BANK_LIMIT",
            "ACS_BANK_SUM",
            "GAME_IMAGE_URL",
            "GAME_TESTING",
            "CDN_URL",
            "CDN_SUPPORT",
            "KEY_PLAYER_DEVICE_TYPE",
            "FREEBALANCE",
            "FRB_COIN",
            "FRB_BPL",
            "FRB_NUMLINES",
            "IS_AUTOPLAY_GAME",
            "WJP",
            "HAS_ACHIEVEMENTS",
            "LINES_COUNT",
            "MAX_WIN",
            "GAMBLE_ALLOWED",
            "THIRD_PARTY_PROVIDER_NAME",
            "THIRD_PARTY_GAME_ID",
            "RTP",
            "EXCLUSIVE",
            "VIVO_GET_TABLES_URL",
            "VIVO_SERVER_ID",
            "VIVO_OPERATOR_ID",
            "VIVO_START_GAME_URL",
            "CLIENTVERSION",
            "GAMEVERSION",
            "LIST_AVAILABLE_GAME_VERSIONS",
            "REPOSITORY_FILE",
            "DEVELOPMENT_VERSION",
            "FRB_TWO_PARAMS",
            "GAME_EVENT_PROCESSOR",
            "PROFILE_ID",
            "COINS_WITH_DISABLED_JP_WON",
            "IS_MINI_GAME",
            "TESTSTAND_ROWCOUNT",
            "IS_GGBG_GAME",
            "IS_LGA_SHELL_SUPPORTED",
            "MAX_BET_IN_CREDITS",
            "JACKPOT_NAME",
            "JACKPOT_HIT_FREQUENCY",
            "SIDE_JP_GAME_IDS",
            "POSSIBLE_LINES",
            "POSSIBLE_BETPERLINES",
            "PDF_RULES_NAME",
            "GAME_WITH_PROGRESS",
            "GAME_WITH_DOUBLE_UP",
            "HTML5PC_VERSION_MODE",
            "UNIFIED_LOCATION",
            "GAME_COMBOS_DISABLED",
            "GAME_COMBO_DETECTOR_NAME",
            "DEFAULTNUMLINESONLY",
            "RTP_NMI",
            "RTP_MIN_NMI",
            "RTP_MIN",
            "ROLLOVER_PERCENT",
            "HANDLE_UNJ_WIN",
            "UNJ_SHARED_GAME_STATE_CLASS",
            "SEND_UNJ_WIN_NOTIFICATION",
            "MIN_BET_FOR_ENTIRE_JACKPOT_WIN",
            "UNJ_WIN_ADDED_INTO_PAYOUT_BY_GAME",
            "KEY_SINGLE_GAME_ID_FOR_ALL_PLATFORMS",
            "MQ_STAKES_RESERVE",
            "ABS_STAKES_RESERVE",
            "MQ_STAKES_LIMIT",
            "ABS_STAKES_LIMIT",
            "MQ_LB_CONTRIBUTION",
            "MQ_AWARD_PLAYER_START_BONUS",
            "ABS_AWARD_PLAYER_START_BONUS",
            "POSSIBLE_MODELS",
            "CURRENT_MODEL",
            "HELP_URL",
            "DEMO_MODE_PARAMS",
            "HAS_BACKGROUND",
            "ADDITIONAL_FLASHVARS",
            "GL_SUPPORTED",
            "GL_MIN_BET_DEFAULT",
            "GL_MAX_BET_DEFAULT",
            "GL_MIN_BET",
            "GL_MAX_BET",
            "GL_NUMBER_OF_COINS",
            "GL_MAX_EXPOSURE",
            "GL_DEFAULT_BET",
            "JACKPOT_LIMIT_AMOUNT",
            "ORIGINAL_GAME",
            "EURO_BET_ADM_CODE",
            "MAX_WIN_PROBABILITY",
            "CLIENT_GENERATION",
            "UNJ_PCRP",
            "UNJ_BASE_AMOUNT",
            "UNJ_BCRP",
            "HANDLE_UNJ_LINKED_GAMEID",
            "RTP_WITHOUT_BF",
            "RTP_MIN_WITHOUT_BF",
            "VOLATILITY"
    };
    Arrays.sort(FIELDS);
%>
<%
    Map<String, String> props = new TreeMap<>();
    String title = "Game";
    long id = 800;
    String name = "";
    String value = "";
    String action;
    BaseGameInfoTemplate tmpl;
    if (request.getParameter("id") != null) {
        id = Long.parseLong(request.getParameter("id"));
        tmpl = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(id);
        title = tmpl.getTitle();

        BaseGameInfo gInfo = tmpl.getDefaultGameInfo();
        if (request.getParameter("name") != null && request.getParameter("value") != null && request.getParameter("action") != null) {
            name = request.getParameter("name");
            value = request.getParameter("value");
            action = request.getParameter("action");
            if (action.equals("create") || action.equals("edit")) {
                gInfo.setProperty(name, value);
            } else if (action.equals("delete")) {
                gInfo.removeProperty(name);
            }
        }
        props.putAll(gInfo.getProperties());

        try {
            RemoteCallHelper.getInstance().saveAndSendNotification(tmpl);
        } catch (CommonException e) {
            response.getWriter().write("Cannot send notification:\n" + e.getMessage());
        }
    }
%>
<html>
<head>
    <title>Game props</title>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.3.1/jquery.js"></script>
</head>
<body>
<form>
    <label>Enter game id:<input type="text" name="id" value="<%=id%>"></label>
</form>
<table>
    <h3><%=title%>
    </h3>
    <%
        for (Map.Entry<String, String> entry : props.entrySet()) {
    %>
    <tr>
        <form>
            <input type="hidden" name="id" value="<%=id%>">
            <td><%=entry.getKey()%><input type="hidden" name="name" value="<%=entry.getKey()%>"></td>
            <td><input type="text" name="value" value="<%=entry.getValue()%>"></td>
            <td><input type="submit" name="action" value="edit"></td>
            <td><input type="submit" name="action" value="delete"></td>
        </form>
    </tr>
    <%
        }
        if (!props.isEmpty()) {
    %>
    <form>
        <h3>New property</h3>
        <input type="hidden" name="id" value="<%=id%>">
        <input type="hidden" name="action" value="create">
        <label>
            <select name="name">
                <% for (String field : FIELDS) {
                %>
                <option value="<%=field%>" <%=field.equals(name) ? "selected" : ""%>><%=field%>
                </option>
                <% } %>
            </select>
        </label>
        <label>Value:<input type="text" name="value" value="<%=value%>"></label>
        <input type="submit" name="action" value="create">
    </form>
    <%
        }%>
</table>
</body>
</html>
