<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ page import="com.dgphoenix.casino.actions.enter.LanguageDetector" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.exception.StartParameters" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="org.apache.struts.Globals" %>
<%@ page import="java.util.Locale" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    StartParameters parameters = (StartParameters) request.getAttribute(StartParameters.class.getSimpleName());
    String lang = "";

    if (parameters != null) {
        lang = parameters.getLang();
        if (StringUtils.isTrimmedEmpty(lang)) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBank(parameters.getExternalBankId(), parameters.getSubcasinoId());
            lang = LanguageDetector.getAlternateLanguage(bankInfo, parameters.getGameId(), null);
        }

        /* We can't use standard LocaleUtils here, because our lang param contains non-standard strings */
        Locale locale;
        if (lang.length() == 2) {
            locale = new Locale(lang);
        } else if (lang.contains("-") && lang.length() == 5) {
            locale = new Locale(lang.substring(0, 2), lang.substring(3, 5).toUpperCase());
        } else {
            locale = Locale.ENGLISH;
        }
        session.setAttribute(Globals.LOCALE_KEY, locale);
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title><bean:message key="error.maintenance.notAvailable"/></title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="/css/error-pages/maintenance.css?v=3">
    <%
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setHeader("Expires", "0"); // Proxies.
    %>
</head>
<body>
<div class="wrapper">
    <div class="border">
        <div class="content">
            <h1><bean:message key="error.maintenance.weAreSorry"/></h1>
            <p><bean:message key="error.maintenance.gameUndergoingMaintenance"/></p>
            <p><bean:message key="error.maintenance.cantBePlayedInRealMode"/></p>

            <% if (parameters != null) { %>
            <p><bean:message key="error.maintenance.clickButton"/></p>
            <p>
                <span><bean:message key="error.maintenance.theGameInFunMode"/></span>
            </p>
            <form action="/cwguestlogin.do">
                <input type="hidden" name="gameId" value="<%=parameters.getGameId()%>">
                <input type="hidden" name="bankId" value="<%=parameters.getExternalBankId()%>">
                <input type="hidden" name="lang" value="<%=lang%>">
                <% if (!StringUtils.isTrimmedEmpty(request.getParameter("CDN"))) { %>
                <input type="hidden" name="CDN" value="<%=request.getParameter("CDN")%>">
                <% } %>
                <div class="button-container">
                    <button type="submit" class="button"><bean:message key="error.maintenance.funModeButton"/></button>
                </div>
            </form>
            <% } %>
        </div>
        <img id="cbl" src="/images/error-pages/characters-big-left-2.png" class="characters-big-left"/>
        <img id="cbr" src="/images/error-pages/characters-big-right-2.png" class="characters-big-right"/>
        <img id="csr" src="/images/error-pages/characters-small-right-2.png" class="characters-small-right"/>
    </div>
</div>
<script>
    var fontScale = 1;

    <% if ("el".equals(lang) || "jp".equals(lang) || "ru".equals(lang)) { %>
    fontScale = 0.8;
    <% } %>
    <% if ("el".equals(lang)) {%>
    fontScale = 0.7;
    <% } %>

    function resize() {
        var w = document.body.clientWidth;
        var h = document.body.clientHeight;
        document.getElementsByClassName('content')[0].style.fontSize = Math.round(w / 100 * fontScale) + 'px';
        if (h < 900) {
            document.getElementsByTagName('h1')[0].style.margin = Math.round(h / 50) + 'px auto';
            document.getElementsByClassName('button-container')[0].style.margin = Math.round(h / 20) + 'px';
            document.getElementsByTagName('p')[2].style.marginTop = Math.round(h / 33) + 'px';
        }
        if (w < 1200 || h < 900) {
            var csrImage = document.getElementById('csr');
            var csrSize = csrImage.getBoundingClientRect();
            csrImage.style.right = Math.round(6 - 48 * csrSize.width / 357) + 'px';
            csrImage.style.bottom = Math.round(4 - 35 * csrSize.height / 470) + 'px';
        }
        if (h < 560) {
            document.getElementsByTagName('h1')[0].style.margin = Math.round(h / 100) + 'px auto';
            document.getElementsByClassName('button-container')[0].style.margin = Math.round(h / 100) + 'px';
        }
    }
    document.addEventListener('DOMContentLoaded', resize);
    window.addEventListener('load', resize);
    window.addEventListener('resize', resize);
</script>
</body>
</html>
