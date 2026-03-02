<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ page import="com.abs.casino.actions.enter.LanguageDetector" %>
<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.common.exception.StartParameters" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="org.apache.struts.Globals" %>
<%@ page import="java.util.Locale" %>
<%@ page contentType="text/html;charset=UTF-8" %>
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
    <link rel="stylesheet" href="/css/error-pages/maintenance.css?v=1">
    <%
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setHeader("Expires", "0"); // Proxies.
    %>
</head>
<body>
<div class="wrapper" id="wrapper">
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
</div>
<script>
    function resize() {
        var w = document.body.clientWidth;
        var h = document.body.clientHeight;
        var sx = w / 1250;
        var sy = h / 850;
        var scale = Math.min(sx, sy);

        var tx = sx < 1 ? (w - 1250) / 2 : 0;
        var ty = sy < 1 ? (h - 850) / 2 : 0;
        if (scale < 1) {
            document.getElementById('wrapper').style.transform = "translate(" + tx + "px, " + ty + "px) scale(" + scale + ")";
        }
    }
    document.addEventListener('DOMContentLoaded', resize);
    window.addEventListener('load', resize);
    window.addEventListener('resize', resize);
</script>
</body>
</html>
