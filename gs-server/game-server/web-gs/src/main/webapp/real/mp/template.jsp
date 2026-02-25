<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.common.web.BaseAction" %>
<%@ page import="com.dgphoenix.casino.system.configuration.GameServerConfiguration" %>
<%@ page import="com.dgphoenix.casino.common.promo.IPromoCampaignManager" %>
<%@ page import="com.dgphoenix.casino.gs.GameServerComponentsHelper" %>
<%@ page import="com.dgphoenix.casino.common.promo.Status" %>
<%@ page import="com.dgphoenix.casino.common.promo.IPromoCampaign" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="java.util.*" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="com.dgphoenix.casino.common.SessionHelper" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.dgphoenix.casino.common.transactiondata.ITransactionData" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%@ page import="static org.apache.http.entity.ContentType.APPLICATION_JSON" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.dgphoenix.casino.actions.game.pov.StartPovGameParams" %>
<%@ page import="org.apache.commons.validator.routines.UrlValidator" %>
<%@ page import="com.google.common.net.InternetDomainName" %>
<%@ page import="java.net.URI" %>
<%@ page import="java.net.URISyntaxException" %>
<%@ page import="com.dgphoenix.casino.common.config.HostConfiguration" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.dgphoenix.casino.gs.socket.mq.BattlegroundService" %>
<%@ page errorPage="/error_pages/mq/error.jsp" %>
<%
    String tournamentId = request.getParameter(BaseAction.PARAM_TOURNAMENT_ID);
    HostConfiguration hostConfiguration = ApplicationContextHelper.getBean(HostConfiguration.class);
    BattlegroundService battlegroundService = ApplicationContextHelper.getBean(BattlegroundService.class);

    String scheme = request.getScheme();
    String forwardedScheme = request.getHeader("X-Forwarded-Proto");

    boolean isProduction = hostConfiguration != null && hostConfiguration.isProductionCluster();
    boolean isForwarded = forwardedScheme != null;

    if (isForwarded) {
        scheme = forwardedScheme;
    } else if (isProduction) {
        scheme = "https";
    }

    String cdnUrl = request.getParameter(BaseAction.KEY_CDN);
    String lobbyUrl = scheme + "://" + (StringUtils.isTrimmedEmpty(cdnUrl) ? request.getServerName() : cdnUrl);
    String serverUrl = scheme + "://" + request.getServerName();
    String sessionErrorURL = serverUrl + "/error_pages/sessionerror.jsp";

    String bankId = request.getParameter(BaseAction.BANK_ID_ATTRIBUTE);
    if (bankId == null) {
        request.getParameter("bankId");
    }
    String bonusId = request.getParameter(BaseAction.PARAM_BONUS_ID);

    if (StringUtils.isTrimmedEmpty(bankId)) {
        ThreadLog.error("MQ TEMPLATE.JSP:: bankId not found");
        response.sendRedirect(sessionErrorURL);
        return;
    }

    long lbankId = Long.parseLong(bankId);
    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(lbankId);

    String[] schemes = {"http", "https"};
    UrlValidator urlValidator = new UrlValidator(schemes);
    String cashierUrl = getCashierUrl(request, bankInfo, urlValidator);
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

    boolean isCrashGame = gameId.equals("863");
    String battlegroundBuyIn = request.getParameter("battlegroundBuyIn");
    String privateRoomId = request.getParameter("privateRoomId");
    String continueIncompleteRound = request.getParameter("continueIncompleteRound");
    String prefRoomId = request.getParameter("prefRoomId");

    String add = "?gameId=" + gameId + "&sessionId=" + sessionId;
    String historyActionURL = StringUtils.isTrimmedEmpty(bankInfo.getHistoryActionURL()) ?
            ("/gamehistory.do" + add) : bankInfo.getHistoryActionURL() + add;
    String homeURL = getHomeUrl(request, bankInfo, urlValidator);

    String timeZoneName = bankInfo.getTimeZone();
    Integer offset = null;
    if (timeZoneName != null) {
        TimeZone timeZone = SimpleTimeZone.getTimeZone(timeZoneName);
        if (timeZone != null) {
            offset = timeZone.getOffset(System.currentTimeMillis()) / 60000;
        }
    }
    BaseGameInfoTemplate baseGameInfoTemplate = BaseGameInfoTemplateCache.getInstance().
            getBaseGameInfoTemplateById(Long.parseLong(gameId));
    boolean battlegroundRequestParamsMissed = StringUtils.isTrimmedEmpty(battlegroundBuyIn) && StringUtils.isTrimmedEmpty(privateRoomId);
    if (baseGameInfoTemplate.isBattleGroundsMultiplayerGame() && battlegroundRequestParamsMissed) {
        ThreadLog.error("MQ TEMPLATE.JSP:: wrong BTG mode, SID=" + sessionId + ", query=" + request.getQueryString());
        response.sendRedirect(sessionErrorURL);
        return;
    }
    boolean tripleMaxBlast = baseGameInfoTemplate.getGameId() == 875;
    String swfLocation = baseGameInfoTemplate.getSwfLocation();
    String gamePath = StringUtils.isTrimmedEmpty(swfLocation) ? "/html5pc/shooter/" : swfLocation;
    String gameFolder = baseGameInfoTemplate.getMultiplayerGameFolderName();
    String templateJsPath = lobbyUrl + gamePath + (StringUtils.isTrimmedEmpty(gameFolder) ? "lobby" : gameFolder);
    String fatalErrorUrl = bankInfo.getFatalErrorPageUrl() + "?gameId=" + gameId;
    request.getSession().setAttribute("lang", request.getParameter(BaseAction.LANG_ID_ATTRIBUTE));
    String cdnUrls = bankInfo.getCdnUrls();
    IPromoCampaignManager promoCampaignManager = GameServerComponentsHelper.getPromoCampaignManager();
    String activePromos = "";
    try {
        AccountInfo accountInfo = null;
        SessionHelper sessionHelper = SessionHelper.getInstance();
        sessionHelper.lock(sessionId);
        try {
            sessionHelper.openSession();
            ITransactionData transactionData = sessionHelper.getTransactionData();
            if (transactionData != null) {
                accountInfo = transactionData.getAccount();
            }
        } finally {
            sessionHelper.clearWithUnlock();
        }
        Set<IPromoCampaign> promoCampaigns = promoCampaignManager.
                getPromoCampaigns(Long.parseLong(bankId), Long.parseLong(gameId), Status.STARTED, accountInfo);
        List<String> promos = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (IPromoCampaign promoCampaign : promoCampaigns) {
            if (isActivePromo(tournamentId, promoCampaign)) {
                sb.setLength(0);
                String promoDetailURL = promoCampaign.getPromoDetailURL(Long.valueOf(bankId));
                sb.append(promoCampaign.getId()).append(",")
                        .append(promoCampaign.getName()).append(",")
                        .append(promoCampaign.getActionPeriod().getEndDate().getTime()).append(",")
                        .append(promoDetailURL != null ? URLEncoder.encode(promoDetailURL, "UTF-8") : "NONE");
                promos.add(sb.toString());
            }
        }
        activePromos = String.join("|", promos);

        if(accountInfo != null && !StringUtils.isTrimmedEmpty(privateRoomId) && !StringUtils.isTrimmedEmpty(bankId)) {

            battlegroundService.updatePlayersStatusInPrivateRoomToLoading(accountInfo, privateRoomId);

            battlegroundService.getFriendsWithOnlineStatus(accountInfo);

        }

    } catch (CommonException e) {
        ThreadLog.debug("error getting promo info, ", e);
    }

    //for POV standalone client need return JSON
    if (baseGameInfoTemplate.isPovMultiplayerGame()) {
        StartPovGameParams params = new StartPovGameParams(bankId, sessionId, gameId,
                request.getParameter(BaseAction.LANG_ID_ATTRIBUTE),
                request.getParameter(BaseAction.GAMEMODE_ATTRIBUTE),
                request.getParameter(BaseAction.WEB_SOCKET_URL),
                request.getParameter(BaseAction.GAMESERVERID_ATTRIBUTE),
                request.getParameter("noFRB"),
                cashierUrl,
                homeURL,
                lobbyUrl + "/flash/shell/global_shell/rules/aams/",
                offset,
                GameServerConfiguration.getInstance().isTestSystem(),
                bankInfo.getCustomerSettingsUrl(),
                activePromos,
                bankInfo.getMaxQuestWeaponMode().name(),
                bankInfo.isRoundWinsWithoutBetsAllowed(),
                bonusId,
                tournamentId,
                false);
        response.setContentType(APPLICATION_JSON.toString());
        response.getWriter().write(new Gson().toJson(params));
        return;
    }

%>
<!DOCTYPE html>
<html>
<head>
    <title><%=baseGameInfoTemplate.getTitle() + (tournamentId != null ? " Tournament" : "")%>
    </title>

    <meta http-equiv="Content-Type" content="text/html, charset=utf-8"/>
    <meta name="mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-capable" content="yes"/>
    <meta name="apple-touch-fullscreen" content="yes"/>
    <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent"/>
    <meta name="msapplication-tap-highlight" content="no"/>
    <% if (!isCrashGame) {%>
    <meta name="MobileOptimized" content="960"/>
    <%}%>
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

        <%if (bankInfo.isPostSidToParent()) { %>
        window.addEventListener("message", function (e) {
            const allowedOrigins = '<%=bankInfo.getAllowedOrigin()%>'.split(',');
            if (allowedOrigins.includes(e.origin)) {
                const response = {
                    'sid': '<%=sessionId%>',
                    'supportSitout': <%=StringUtils.isTrimmedEmpty(battlegroundBuyIn) && StringUtils.isTrimmedEmpty(privateRoomId)%>
                }
                e.source.postMessage(response, e.origin);
            }
        });
        <%}%>

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
            <% if (!StringUtils.isTrimmedEmpty(battlegroundBuyIn)) { %>'battlegroundBuyIn': '<%=battlegroundBuyIn%>', <% } %>
            <% if (!StringUtils.isTrimmedEmpty(privateRoomId)) { %>'privateRoomId': '<%=privateRoomId%>', <% } %>
            <% if (!StringUtils.isTrimmedEmpty(prefRoomId)) { %>'prefRoomId': '<%=prefRoomId%>', <% } %>
            'continueIncompleteRound': '<%=Boolean.parseBoolean(continueIncompleteRound)%>',
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
            'noFRB': '<%=request.getParameter("noFRB")%>',
            <% if (!StringUtils.isTrimmedEmpty(cashierUrl)) { %>'JS_BUYIN_FUNC_NAME': 'openCashier', <% } %>
            'JS_HISTORY': 'openHistory',
            'JS_HOME': 'openHome',
            'MQ_HELP_PATH': '<%=lobbyUrl%>/flash/shell/global_shell/rules/aams/',
            <% if (offset != null) { %> 'MQ_TIMER_OFFSET': '<%=offset%>', <% } %>
            'MQ_TIMER_FREQ': '15', // in sec
            <% if (GameServerConfiguration.getInstance().isTestSystem()) { %> 'TEST_SYSTEM': true, <% } %>
            'JS_CLOSE_ERROR_FUNC_NAME': 'openFatalError',
            'CUSTOMER_SETTINGS_URL': '<%=lobbyUrl%><%=bankInfo.getCustomerSettingsUrl()%>',
            <% if (!activePromos.isEmpty()) { %>
            <% if (tournamentId == null) { %>
            'ACTIVE_PROMOS': '<%=activePromos%>',
            <% } %>
            'JS_SHOW_PROMO_DETAILS_FUNC_NAME': 'showPromoDetails',
            <% } %>
            'MQ_WEAPONS_MODE': '<%=bankInfo.getMaxQuestWeaponMode().name()%>',
            'ABS_WEAPONS_MODE': '<%=bankInfo.getMaxQuestWeaponMode().name()%>',
            'MQ_WEAPONS_SAVING_ALLOWED': '<%=bankInfo.isRoundWinsWithoutBetsAllowed()%>',
            <% if (bonusId != null) { %>'bonusId': '<%=bonusId%>', <% } %>
            <% if (tournamentId != null) { %> 'tournamentId': '<%=tournamentId%>', <% } %>
            'MQ_CLIENT_ERROR_HANDLING': <%=GameServerConfiguration.getInstance().isTestSystem()%>,
            'ABS_CLIENT_ERROR_HANDLING': <%=GameServerConfiguration.getInstance().isTestSystem()%>,
            'DISABLE_MQ_BACKGROUND_LOADING': '<%=bankInfo.isMQBackgroundLoadingDisabled()%>',
            'DISABLE_ABS_BACKGROUND_LOADING': '<%=bankInfo.isMQBackgroundLoadingDisabled()%>',
            'DISABLE_MQ_AUTOFIRING': '<%=false%>',
            'ROOMS_SORT_ORDER': '<%=bankInfo.getMQRoomsSortOrder()%>',
            'CW_SEND_REAL_BET_WIN': '<%=bankInfo.isCWSendRealBetWin()%>',
            'commonPathForActionGames': '<%=lobbyUrl%>/html5pc/actiongames/common/',
            'isBattleGroundGame': '<%=baseGameInfoTemplate.isBattleGroundsMultiplayerGame()%>',
            'JS_MQB_HOME': 'openMQBLobby',
            'CLIENT_LOG_LEVEL': '<%=bankInfo.getMQClientLogLevel().name()%>'
        };
    }

    <% if (!activePromos.isEmpty()) { %>

    function showPromoDetails(url) {
        window.open(url, "mywindow", "menubar=0,toolbar=0,location=0,resizable=1,status=1,scrollbars=1,width=1024px,height=768px");
    }

    <% } %>


    function openCashier() {
        window.open('<%=cashierUrl%>');
    }

    function openHistory() {
        window.open('<%=historyActionURL%>', "mywindow", "menubar=0,toolbar=0,location=0,resizable=1,status=1,scrollbars=1,width=800px,height=600px");
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

    function openMQBLobby() {
        openHome();
    }

    function openFatalError() {
        window.location.replace('<%=fatalErrorUrl%>');
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
<%!
    @SuppressWarnings("UnstableApiUsage")
    private String extractDomain(String homeURL) {
        try {
            URI uri = new URI(homeURL);
            String host = uri.getHost();
            return InternetDomainName.isValid(host)
                    ? InternetDomainName.from(host).topPrivateDomain().toString()
                    : null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private boolean isValidUrl(String originalUrl, UrlValidator urlValidator, BankInfo bankInfo) {
        if (StringUtils.isTrimmedEmpty(originalUrl) || !urlValidator.isValid(originalUrl)) {
            return false;
        }
        String domain = extractDomain(originalUrl);
        if (domain == null) {
            return false;
        }
        List<String> allowedDomains = bankInfo.getAllowedDomains();
        return allowedDomains.isEmpty() || allowedDomains.contains(domain);
    }

    private String getHomeUrl(HttpServletRequest request, BankInfo bankInfo, UrlValidator urlValidator) {
        String homeURL = request.getParameter(BaseAction.PARAM_HOME_URL);
        if (isValidUrl(homeURL, urlValidator, bankInfo)) {
            return homeURL;
        }
        return StringUtils.isTrimmedEmpty(bankInfo.getHomeURL()) ? "" : bankInfo.getHomeURL();
    }

    private String getCashierUrl(HttpServletRequest request, BankInfo bankInfo, UrlValidator urlValidator) {
        String cashierUrl = request.getParameter(BaseAction.PARAM_CASHIER_URL);
        if (isValidUrl(cashierUrl, urlValidator, bankInfo)) {
            return cashierUrl;
        }
        return StringUtils.isTrimmedEmpty(bankInfo.getCashierUrl()) ? "" : bankInfo.getCashierUrl();
    }

    private boolean isActivePromo(String tournamentId, IPromoCampaign promoCampaign) {
        boolean isTournamentPromo = promoCampaign.getTemplate().getPromoType().isScoreCounting() && tournamentId != null;
        boolean isPromo = tournamentId == null && !promoCampaign.getTemplate().getPromoType().isScoreCounting();
        return ((isTournamentPromo || isPromo) && !promoCampaign.isNetworkPromoCampaign());
    }
%>
