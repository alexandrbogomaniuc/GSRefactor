<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.StringWriter" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%--
  Author: svvedenskiy
  Date: 11/14/18

  The page shows information about SubCasino's banks
--%>

<%
    if (StringUtils.isTrimmedEmpty(request.getParameter("subCasinoId"))) {
        response.getWriter().write("Parameter 'subCasinoId' can't be empty");
        return;
    }

    long subCasinoId;
    try {

        subCasinoId = Long.parseLong(request.getParameter("subCasinoId"));
    } catch (NumberFormatException e) {
        response.getWriter().write("Parameter 'subCasinoId' not well formatted");
        return;
    }

    subCasino = SubCasinoCache.getInstance().get(subCasinoId);
    if (subCasino == null) {
        response.getWriter().write("SubCasino " + subCasinoId + " not found");
        return;
    }
%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>

<head>
    <title>Banks of SubCasino <%=subCasino.getName()%> (<%=subCasino.getId()%>)</title>
    <style type="text/css">
        table tr.head {
            font-weight: bold;
        }
    </style>
</head>
<body>

<%
    try {
        List<BankInfo> activeBanks = new ArrayList<>();
        List<BankInfo> inactiveBanks = new ArrayList<>();
        for (Long bankId : subCasino.getBankIds()) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo == null) {
                continue;
            }
            if (bankInfo.isEnabled()) {
                activeBanks.add(bankInfo);
            } else {
                inactiveBanks.add(bankInfo);
            }
        }
%>
<h1><%=subCasino.getName()%>
</h1>
<h3>Hosts:</h3>
<ul>
    <%for (String domainName : subCasino.getDomainNames()) {%>
    <li><%=domainName%>
    </li>
    <%}%>
</ul>
<h2>Active banks:</h2>
<table cellspacing="0" border="1" cellpadding="4">
    <tr class="head">
        <td>Id-[extId]-name</td>
        <td>Protocol</td>
        <td>API Endpoint</td>
        <td>Bonus systems</td>
        <td>RealBet/Win</td>
        <td>TTP AutoPayout</td>
        <td>Master bank</td>
        <td>Currencies [Default]</td>
    </tr>
    <%for (BankInfo bankInfo : activeBanks) {%>
    <tr>
        <td><%=bankDescription(bankInfo)%>
        </td>
        <td><%=protocolType(bankInfo)%>
        </td>
        <td><%=apiEndpoint(bankInfo)%>
        </td>
        <td><%=bonusSystems(bankInfo)%>
        </td>
        <td><%=Boolean.toString(bankInfo.isCWSendRealBetWin())%>
        </td>
        <td><%=Boolean.toString(bankInfo.isSupportPromoBalanceTransfer())%>
        </td>
        <td><%=masterBankInfo(bankInfo)%>
        </td>
        <td><%=bankCurrencies(bankInfo)%>
        </td>
    </tr>
    <%}%>
</table>

<h2>Inactive banks:</h2>
<table cellspacing="0" border="1" cellpadding="4">
    <tr class="head">
        <td>Id-[extId]-name</td>
        <td>Protocol</td>
        <td>API Endpoint</td>
        <td>Bonus systems</td>
        <td>Master bank</td>
        <td>Currencies [Default]</td>
    </tr>
    <%for (BankInfo bankInfo : inactiveBanks) {%>
    <tr>
        <td><%=bankDescription(bankInfo)%>
        </td>
        <td><%=protocolType(bankInfo)%>
        </td>
        <td><%=apiEndpoint(bankInfo)%>
        </td>
        <td><%=bonusSystems(bankInfo)%>
        </td>
        <td><%=masterBankInfo(bankInfo)%>
        </td>
        <td><%=bankCurrencies(bankInfo)%>
        </td>
    </tr>
    <%}%>
</table>

<%
    } catch (Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        response.getWriter().write(sw.toString());
    }
%>
<br/><br/>
</body>
</html>

<%!
    SubCasino subCasino;

    private String bankDescription(BankInfo bankInfo) {
        String descr = "<a target='_blank' href='/tools/bankProperties.jsp?bankId=" + bankInfo.getId() + "'>";
        descr += String.valueOf(bankInfo.getId());
        if (!descr.equals(bankInfo.getExternalBankId()) && !StringUtils.isTrimmedEmpty(bankInfo.getExternalBankId())) {
            descr += "-" + bankInfo.getExternalBankId();
        }
        descr += "-" + bankInfo.getExternalBankIdDescription();
        descr += "</a>";
        return descr;
    }

    private String protocolType(BankInfo bankInfo) {
        String protocol;
        if (bankInfo.getPPClass() != null && bankInfo.getPPClass().contains("PTPT")) {
            protocol = "PTPT";
        } else if (bankInfo.getPPClass() != null && bankInfo.getPPClass().contains("CTPaymentProcessor")) {
            protocol = "CT";
        } else if (StringUtils.isTrimmedEmpty(bankInfo.getCWAuthUrl())) {
            protocol = "CW v1";
        } else if (!StringUtils.isTrimmedEmpty(bankInfo.getRefundBetUrl())) {
            protocol = "CW v3.07";
        } else {
            protocol = "CW v2";
        }

        if ("CW v3.07".equals(protocol)) {
            return "<a target='_blank' href='/tools/bankEndpoints.jsp?bankId=" + bankInfo.getId() + "'>" + protocol + "</a>";
        }

        return protocol;
    }

    private String apiEndpoint(BankInfo bankInfo) {
        if (bankInfo.isStubMode()) {
            return "Stub";
        }
        String apiUrl = authUrl(bankInfo);
        if (StringUtils.isTrimmedEmpty(apiUrl)) { //CW v1 has not auth URL
            apiUrl = bankInfo.getCWWagerUrl();
        }
        if (StringUtils.isTrimmedEmpty(apiUrl)) {
            return "none";
        }
        if (apiUrl.endsWith("/")) {
            apiUrl = apiUrl.substring(0, apiUrl.length() - 1);
        }
        int lastSlashIdx = apiUrl.lastIndexOf("/");
        if (lastSlashIdx < 0) {
            return apiUrl;
        }
        return apiUrl.substring(0, apiUrl.lastIndexOf("/") + 1);
    }

    private String authUrl(BankInfo bankInfo) {
        String protocolType = protocolType(bankInfo);
        if (protocolType.contains("CW")) {
            return bankInfo.getCWAuthUrl();
        }
        if (protocolType.contains("CT")) {
            return bankInfo.getCTRESTAuthURL();
        }
        return bankInfo.getCWAuthUrl();
    }

    private String bonusSystems(BankInfo bankInfo) {
        boolean cashBonus = !StringUtils.isTrimmedEmpty(bankInfo.getBonusReleaseUrl());
        boolean frb = !StringUtils.isTrimmedEmpty(bankInfo.getFRBonusWinURL()) || bankInfo.isFRBForCTSupported();
        if (!cashBonus && !frb) {
            return "none";
        }
        if (!cashBonus) {
            return "FRB only";
        }
        if (!frb) {
            return "Cash bonus only";
        }
        return "Cash & FRB";
    }

    private String masterBankInfo(BankInfo bankInfo) {
        Long masterBankId = bankInfo.getMasterBankId();
        if (masterBankId == null || masterBankId == bankInfo.getId()) {
            return "none";
        }
        BankInfo masterInfo = BankInfoCache.getInstance().getBankInfo(masterBankId);
        return bankDescription(masterInfo);
    }

    private String bankCurrencies(BankInfo bankInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>[").append(bankInfo.getDefaultCurrency().getCode()).append("]</b>");
        if (bankInfo.getCurrencies().size() > 1) {
            stringBuilder.append(", ");
        }
        int i = 0;
        for (Currency currency : bankInfo.getCurrencies()) {
            i++;
            if (currency.equals(bankInfo.getDefaultCurrency())) {
                continue;
            }
            stringBuilder.append(currency.getCode());
            if (i < bankInfo.getCurrencies().size()) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }
%>
