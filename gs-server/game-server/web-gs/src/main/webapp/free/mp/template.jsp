<%@ page import="com.abs.casino.common.web.BaseAction" %>
<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="java.util.TimeZone" %>
<%@ page import="java.util.SimpleTimeZone" %>
<%@ page import="com.dgphoenix.casino.system.configuration.GameServerConfiguration" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%@ page import="com.abs.casino.actions.game.pov.StartPovGameParams" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="static org.apache.http.entity.ContentType.APPLICATION_JSON" %>
<%@ page import="com.abs.casino.common.config.HostConfiguration" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%
    String cdnUrl = request.getParameter(BaseAction.KEY_CDN);
    HostConfiguration hostConfiguration = ApplicationContextHelper.getBean(HostConfiguration.class);

    String scheme = request.getScheme();
    if(hostConfiguration != null && hostConfiguration.isProductionCluster()) {
        scheme = "https";
    }

    String lobbyUrl = scheme + "://" + (StringUtils.isTrimmedEmpty(cdnUrl) ? request.getServerName() : cdnUrl);
    String serverUrl = scheme + "://" + request.getServerName();
    String sessionErrorURL = serverUrl + "/error_pages/sessionerror.jsp";

    String bankId = request.getParameter(BaseAction.BANK_ID_ATTRIBUTE);
    if (bankId == null) {
        request.getParameter("bankId");
    }

    if (StringUtils.isTrimmedEmpty(bankId)) {
        ThreadLog.error("MQ TEMPLATE.JSP:: bankId not found");
        response.sendRedirect(sessionErrorURL);
        return;
    }

    long lbankId = Long.parseLong(bankId);
    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(lbankId);

    String homeURL = request.getParameter(BaseAction.PARAM_HOME_URL);
    if (StringUtils.isTrimmedEmpty(homeURL)) {
        homeURL = StringUtils.isTrimmedEmpty(bankInfo.getHomeURL()) ? "" : bankInfo.getHomeURL();
    }
    String sessionId = request.getParameter(BaseAction.SESSION_ID_ATTRIBUTE);
    if (StringUtils.isTrimmedEmpty(sessionId)) {
        ThreadLog.error("MQ TEMPLATE.JSP:: SID not found, query=" + request.getQueryString());
        response.sendRedirect(sessionErrorURL);
        return;
    }
    String gameId = request.getParameter(BaseAction.GAME_ID_ATTRIBUTE);
    if (StringUtils.isTrimmedEmpty(gameId)) {
        ThreadLog.error("MQ TEMPLATE.JSP:: gameId not found, SID=" + sessionId + ", query=" + request.getQueryString());
        response.sendRedirect(sessionErrorURL);
        return;
    }
    String timeZoneName = bankInfo.getTimeZone();
    Integer offset = null;
    if (timeZoneName != null) {
        TimeZone timeZone = SimpleTimeZone.getTimeZone(timeZoneName);
        if (timeZone != null) {
            offset = timeZone.getOffset(System.currentTimeMillis()) / 60000;
        }
    }
    BaseGameInfoTemplate baseGameInfoTemplate = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(Long.parseLong(gameId));
    if (baseGameInfoTemplate.isBattleGroundsMultiplayerGame()) {
        ThreadLog.error("MQ TEMPLATE.JSP:: wrong BTG mode, SID=" + sessionId + ", query=" + request.getQueryString());
        response.sendRedirect(sessionErrorURL);
        return;
    }
    boolean tripleMaxBlast = baseGameInfoTemplate.getGameId() == 875;
    ;
    String swfLocation = baseGameInfoTemplate.getSwfLocation();
    String gamePath = StringUtils.isTrimmedEmpty(swfLocation) ? "/html5pc/shooter/" : swfLocation;
    String gameFolder = baseGameInfoTemplate.getMultiplayerGameFolderName();
    String templateJsPath = lobbyUrl + gamePath + (StringUtils.isTrimmedEmpty(gameFolder) ? "lobby" : gameFolder);
    String cdnUrls = bankInfo.getCdnUrls();
    //for POV standalone client need return JSON
    if (baseGameInfoTemplate.isPovMultiplayerGame()) {
        StartPovGameParams params = new StartPovGameParams(bankId, sessionId, gameId,
                request.getParameter(BaseAction.LANG_ID_ATTRIBUTE),
                request.getParameter(BaseAction.GAMEMODE_ATTRIBUTE),
                request.getParameter(BaseAction.WEB_SOCKET_URL),
                request.getParameter(BaseAction.GAMESERVERID_ATTRIBUTE),
                request.getParameter("noFRB"),
                "",
                homeURL,
                lobbyUrl + "/flash/shell/global_shell/rules/aams/",
                offset,
                GameServerConfiguration.getInstance().isTestSystem(),
                bankInfo.getCustomerSettingsUrl(),
                "",
                bankInfo.getMaxQuestWeaponMode().name(),
                bankInfo.isRoundWinsWithoutBetsAllowed(),
                null,
                null,
                false);
        response.setContentType(APPLICATION_JSON.toString());
        response.getWriter().write(new Gson().toJson(params));
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title><%=baseGameInfoTemplate.getTitle()%></title>

    <meta http-equiv="Content-Type" content="text/html, charset=utf-8"/>
    <meta name="mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-capable" content="yes"/>
    <meta name="apple-touch-fullscreen" content="yes"/>
    <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent"/>
    <meta name="msapplication-tap-highlight" content="no"/>
    <meta name="MobileOptimized" content="960"/>

    <meta name="viewport"
          content="width=device-width, minimum-scale=1.0, maximum-scale=1.0, initial-scale=1.0, user-scalable=no, minimal-ui"/>

    <meta charset="utf-8"/>

    <style>
        html, body {
            padding: 0;
            margin: 0;
            background-color: black
        }
    </style>
</head>
<body>
<% if (!StringUtils.isTrimmedEmpty(cdnUrls) && cdnUrls.contains("MCDN")) { %>
<script async data-id="pulse" data-trackid="m9nkg2ac"
        src="https://beacon.idcservices.net/pulse.js?trackid=m9nkg2ac"></script>
<% } %>
<script>
    (function () {
        window.gameEnvReady = false;

        function onGameEnvReady() {
            window.removeEventListener('load', onGameEnvReady);
            window.gameEnvReady = true;
        }

        if (!!window.addEventListener) {
            window.addEventListener("load", onGameEnvReady);
        }

        ensureLegacyLobbyQueryParams();

        var l_xhr = new XMLHttpRequest();
        l_xhr.open('GET', '<%=templateJsPath%>/version.json?t=' + (new Date().getTime()), true);
        l_xhr.onload = function () {
            var lVersion_str = JSON.parse(l_xhr.response).version;
            loadScript('<%=templateJsPath%>/validator.js', lVersion_str, loadLobby.bind(this, lVersion_str));
        };
        l_xhr.send();

        function loadLobby(version) {
            var lPlatformInfo_obj = window.getPlatformInfo ? window.getPlatformInfo() : {};
            if (lPlatformInfo_obj.supported) {
                loadScript('<%=templateJsPath%>/game.js', version);
            }
        }

        function ensureLegacyLobbyQueryParams() {
            try {
                if (!window.history || !window.history.replaceState || !window.URLSearchParams) {
                    return;
                }

                var queryParams = new URLSearchParams(window.location.search || '');
                setIfMissing(queryParams, 'BANKID', '<%=bankId%>');
                setIfMissing(queryParams, 'SID', '<%=sessionId%>');
                setIfMissing(queryParams, 'GAMEID', '<%=gameId%>');
                setIfMissing(queryParams, 'MODE', '<%=request.getParameter(BaseAction.GAMEMODE_ATTRIBUTE)%>');
                setIfMissing(queryParams, 'LANG', '<%=request.getParameter(BaseAction.LANG_ID_ATTRIBUTE)%>');
                setIfMissing(queryParams, 'GAMESERVERID', '<%=request.getParameter(BaseAction.GAMESERVERID_ATTRIBUTE)%>');
                setIfMissing(queryParams, 'WEB_SOCKET_URL', '<%=request.getParameter(BaseAction.WEB_SOCKET_URL)%>');

                var normalizedQuery = queryParams.toString();
                var currentUrl = window.location.pathname + window.location.search + window.location.hash;
                var targetUrl = window.location.pathname + (normalizedQuery ? ('?' + normalizedQuery) : '') + window.location.hash;
                if (targetUrl !== currentUrl) {
                    window.history.replaceState(null, '', targetUrl);
                }
            } catch (e) {
                if (window.console && window.console.warn) {
                    window.console.warn('Failed to normalize lobby URL params', e);
                }
            }
        }

        function setIfMissing(params, key, value) {
            if (value !== null && value !== undefined && value !== '' && value !== 'null' && !params.has(key)) {
                params.set(key, value);
            }
        }

        function loadScript(src, version, onLoadHandler) {
            var lScript_e = document.createElement('script');
            lScript_e.type = 'text/javascript';
            lScript_e.src = src + (version ? '?version=' + version : '');
            lScript_e.async = false;
            if (onLoadHandler) {
                lScript_e.onload = onLoadHandler;
            }
            document.head.appendChild(lScript_e);
        }
    })();

    function getParams() {
        return {
            'bankId': '<%=bankId%>',
            'sessionId': '<%=sessionId%>',
            'gameId': '<%=gameId%>',
            'lang': '<%=request.getParameter(BaseAction.LANG_ID_ATTRIBUTE)%>',
            'mode': '<%=request.getParameter(BaseAction.GAMEMODE_ATTRIBUTE)%>',
            'websocket': '<%=request.getParameter(BaseAction.WEB_SOCKET_URL)%>',
            'WEB_SOCKET_URL': '<%=request.getParameter(BaseAction.WEB_SOCKET_URL)%>',
            'LOBBY_WEB_SOCKET': '<%=request.getParameter(BaseAction.WEB_SOCKET_URL)%>',
            'serverId': '<%=request.getParameter(BaseAction.GAMESERVERID_ATTRIBUTE)%>',
            'GAMESERVERID': '<%=request.getParameter(BaseAction.GAMESERVERID_ATTRIBUTE)%>',
            'SID': '<%=sessionId%>',
            'BANKID': '<%=bankId%>',
            'GAMEID': '<%=gameId%>',
            'MODE': '<%=request.getParameter(BaseAction.GAMEMODE_ATTRIBUTE)%>',
            'LANG': '<%=request.getParameter(BaseAction.LANG_ID_ATTRIBUTE)%>',
            <% if(tripleMaxBlast) { %>'isTripleMaxBlast': 'true', <% } %>
            'JS_HOME': 'openHome',
            'CUSTOMER_SETTINGS_URL': '<%=lobbyUrl%><%=bankInfo.getCustomerSettingsUrl()%>',
            'MQ_HELP_PATH': '<%=lobbyUrl%>/flash/shell/global_shell/rules/aams/',
            'ABS_HELP_PATH': '<%=lobbyUrl%>/flash/shell/global_shell/rules/aams/',
            <% if (offset != null) { %>'MQ_TIMER_OFFSET': '<%=offset%>', 'ABS_TIMER_OFFSET': '<%=offset%>', <% } %>
            'MQ_TIMER_FREQ': '15', // in sec,
            'ABS_TIMER_FREQ': '15',
            <% if (GameServerConfiguration.getInstance().isTestSystem()) { %>'TEST_SYSTEM': true, <% } %>
            'MQ_WEAPONS_MODE': '<%=bankInfo.getMaxQuestWeaponMode().name()%>',
            'ABS_WEAPONS_MODE': '<%=bankInfo.getMaxQuestWeaponMode().name()%>',
            'MQ_WEAPONS_SAVING_ALLOWED': '<%=bankInfo.isRoundWinsWithoutBetsAllowed()%>',
            'ABS_WEAPONS_SAVING_ALLOWED': '<%=bankInfo.isRoundWinsWithoutBetsAllowed()%>',
            'MQ_CLIENT_ERROR_HANDLING': <%=GameServerConfiguration.getInstance().isTestSystem()%>,
            'ABS_CLIENT_ERROR_HANDLING': <%=GameServerConfiguration.getInstance().isTestSystem()%>,
            'DISABLE_MQ_BACKGROUND_LOADING': '<%=bankInfo.isMQBackgroundLoadingDisabled()%>',
            'DISABLE_ABS_BACKGROUND_LOADING': '<%=bankInfo.isMQBackgroundLoadingDisabled()%>',
            'DISABLE_MQ_AUTOFIRING': '<%=false%>',
            'DISABLE_ABS_AUTOFIRING': '<%=false%>',
            'CW_SEND_REAL_BET_WIN': '<%=bankInfo.isCWSendRealBetWin()%>',
            'commonPathForActionGames': '<%=lobbyUrl%>/html5pc/actiongames/common/',
            'ROOMS_SORT_ORDER': '<%=bankInfo.getMQRoomsSortOrder()%>',
            'ABS_ROOMS_SORT_ORDER': '<%=bankInfo.getMQRoomsSortOrder()%>',
            'CLIENT_LOG_LEVEL': '<%=bankInfo.getMQClientLogLevel().name()%>',
            'ABS_CLIENT_LOG_LEVEL': '<%=bankInfo.getMQClientLogLevel().name()%>'
        };
    }

    function openHome() {
        var target = "<%=homeURL%>";
        if (!target) {
            target = document.referrer || "";
        }
        if (!target) {
            if (window.history && window.history.length > 1) {
                window.history.back();
                return;
            }
            target = "/";
        }
        if (window.parent != null && window.parent !== window) {
            window.parent.location = target;
        } else {
            window.location = target;
        }
    }

    function getLobbyPath() {
        return '<%=lobbyUrl%><%=gamePath%>lobby/';
    }

    function getGamePath() {
        return '<%=lobbyUrl%><%=gamePath%>game/';
    }

    function getCustomerspecDescriptorStoragePathURL() {
        return "<%=lobbyUrl%><%=bankInfo.getCustomerSettingsHtml5Pc()%>";
    }
</script>
</body>
</html>
