<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.dgphoenix.casino.actions.lobby.STLobbyAction" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.common.web.logout.LogoutCommonConstants" %>
<%@ page import="com.dgphoenix.casino.common.web.BaseAction" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.system.configuration.GameServerConfiguration" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<html>
<head>
    <%
        String sessionId = (String) request.getAttribute(BaseAction.SESSION_ID_ATTRIBUTE);
        if (StringUtils.isTrimmedEmpty(sessionId)) {
            sessionId = request.getParameter("sessionId");
        }
        String logoutUrl = request.getScheme() + "://" + request.getServerName() + "/logoutproxy.do?sessionId=" + sessionId + "&actionName=" + LogoutCommonConstants.ACTION_NAME_LOGOUT;
        String openGameLink = request.getAttribute(STLobbyAction.OPEN_GAME_LINK) + "";
        String includeJsp = request.getAttribute(STLobbyAction.STANDALONE_JSP_NAME) + "";

        Integer bankId = (Integer) request.getAttribute(STLobbyAction.BANK_ID_ATTRIBUTE);
        if (bankId != null) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo != null && bankInfo.getSubCasinoId() == 58) { //todo: Only for Default system, delete after MQ release
                String maxquestPass = GameServerConfiguration.getInstance().getStringPropertySilent("maxquestpass");
                if (!StringUtils.isTrimmedEmpty(maxquestPass)) {
                    openGameLink = openGameLink.replace("gameId=", "pass=" + maxquestPass + "&gameId=");
                }
            }
        }
    %>
    <script type="text/javascript" src="/js/util.js"></script>
    <script>
        function initRequest() {
            var xmlHttp;
            try {
                xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
            } catch (e) {
                try {
                    xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
                } catch (e) {
                    try {
                        xmlHttp = new XMLHttpRequest();
                    } catch (e) {
                        alert("Your browser does not support AJAX!");
                        return false;
                    }
                }
            }
            return xmlHttp;
        }

        var isClosed = false;
        function makeExitRequest() {
            if (!isClosed) {
                var url = '<%=logoutUrl%>';
                var in_method = "POST";
                var agent = navigator.userAgent;
                if (agent.indexOf("Firefox") != -1) in_method = "GET";

                var xmlHttp = initRequest();
                xmlHttp.open(in_method, url, false);
                xmlHttp.send(null);
                var status;
                if (xmlHttp.status == 200) {
                    status = "success";
                } else status = "error";

                isClosed = true;
                return status;
            }
        }

        window.onunload = function () {
            makeExitRequest();
        };

        function preparedSubmit() {
            isClosed = true;
        }

        function openGame(gameId) {
            openWnd('<%=openGameLink%>' + gameId, '800', '600', 'Game', 'no');
        }
    </script>

    <title>Standalone Lobby</title>
</head>
<body>
<html:form action="/stlobby">
    <html:hidden property="bankId"/>
    <html:hidden property="SID" value="<%=sessionId%>"/>
    <html:hidden property="lang"/>
    <html:hidden property="subCasinoId"/>

    <jsp:include page="<%=includeJsp%>"/>
</html:form>
</body>
</html>
