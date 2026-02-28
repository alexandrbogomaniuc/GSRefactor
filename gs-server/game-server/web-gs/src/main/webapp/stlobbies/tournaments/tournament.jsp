<%@ page import="com.abs.casino.common.web.BaseAction" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.dgphoenix.casino.system.configuration.GameServerConfiguration" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%
    String cdnUrl = (String) request.getAttribute(BaseAction.KEY_CDN);
    String lobbyUrl = request.getScheme() + "://" + (StringUtils.isTrimmedEmpty(cdnUrl) ? request.getServerName() : cdnUrl);
    String wsUrl;
    int bankId = (int) request.getAttribute(BaseAction.BANK_ID_ATTRIBUTE);
    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
    String bankWsUrl = bankInfo.getMQTournamentRealModeUrl();
    boolean isSecure = "https".equals(request.getScheme());
    String forwardedScheme = request.getHeader("X-Forwarded-Proto");
    boolean isSecureForwarded = "https".equals(forwardedScheme);
    wsUrl = ((isSecure || isSecureForwarded) ? "wss" : "ws") + "://" + GameServerConfiguration.getInstance().getHost();
    String realModeUrl = (String) request.getAttribute(BaseAction.REAL_MODE_URL);
    if (StringUtils.isTrimmedEmpty(realModeUrl) && !StringUtils.isTrimmedEmpty(bankWsUrl)) {
        realModeUrl = bankWsUrl;
    }
    String sessionId = (String) request.getAttribute(BaseAction.SESSION_ID_ATTRIBUTE);
    if (StringUtils.isTrimmedEmpty(sessionId)) {
        ThreadLog.error("tournament.jsp:: SID not found, query=" + request.getQueryString());
        response.sendRedirect(lobbyUrl + "/error_pages/sessionerror.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Max Quest Tournament</title>
    <meta charset="utf-8">
    <base href="<%=lobbyUrl%>/html5pc/actiongames/tournament-lobby/vue-spa/">
    <style>
        body {
            padding: 0;
            margin: 0;
            background-color: black;
        }
    </style>
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

            var l_xhr = new XMLHttpRequest();
            l_xhr.open('GET', 'version.json?t=' + (new Date().getTime()), true);
            l_xhr.onload = function () {
                var lVersion_str = JSON.parse(l_xhr.response).version;
                loadScript('lib/validator.js', lVersion_str, loadLobby.bind(this, lVersion_str));
            }
            l_xhr.send();

            function loadLobby(version) {
                var lPlatformInfo_obj = window.getPlatformInfo ? window.getPlatformInfo() : {};
                if (lPlatformInfo_obj.supported) {
                    loadScript('build.js', version);
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
    </script>
</head>
<body>
<div id="app"></div>
<script>
    function getParams() {
        return {
            'bankId': '<%=bankId%>',
            'sessionId': '<%=sessionId%>',
            'lang': '<%=request.getAttribute(BaseAction.LANG_ID_ATTRIBUTE)%>',
            'websocket': '<%=wsUrl%>/tournamentWebSocket',
            'realModeUrl': '<%=realModeUrl%>',
            'CDN': '<%=request.getParameter(BaseAction.KEY_CDN)%>',
            'showBattlegroundTab': '<%=request.getAttribute(BaseAction.SHOW_BATTLEGROUND_TAB)%>',
            'commonPathForActionGames': '<%=lobbyUrl%>/html5pc/actiongames/common/'
        }
    }
</script>
</body>
</html>
