<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.gs.api.service.RESTServiceClient" %>
<%@ page import="com.abs.casino.gs.api.service.xml.*" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.SubCasino" %>
<%

    long bankId = Long.parseLong(request.getParameter("bankId"));
    String userId = request.getParameter("userId");
    String fromSupport = request.getParameter("fromSupport");
    boolean isFromSupport = false;
    if (!StringUtils.isTrimmedEmpty(fromSupport) && fromSupport.equals("1")) {
        isFromSupport = true;
    }

    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);

    if (!StringUtils.isTrimmedEmpty(userId)) {
        try {
            RESTServiceClient.getInstance().fundAccount(bankInfo.getId(), userId, 1000);
        } catch (CommonException e) {
            e.printStackTrace(response.getWriter());
        }
    }

    GetEnvironment environment = null;
    try {
        environment = RESTServiceClient.getInstance().getEnvironment(bankInfo.getId());
    } catch (CommonException e) {
        e.printStackTrace(response.getWriter());
    }

    if (environment != null) {
        EnvironmentResponse envValues = environment.getResponse();

        SubCasino subCasino = SubCasinoCache.getInstance().get(bankInfo.getSubCasinoId());
        String cwmType = StringUtils.isTrimmedEmpty(bankInfo.getCWMType()) ? "SEND_WIN_ONLY" : bankInfo.getCWMType();
        String checkAPIBase = "/support/testcw3/checkAPI.jsp?bankId=" + bankInfo.getId() +
                "&isPost=" + bankInfo.isPOST() +
                "&isNegativeBet=true" +
                "&cwmType=" + cwmType + "&gameId=2";

%>
<%if (isFromSupport) {%>
<b>SubCasino:</b>
<a href="/support/subCasino.do?subcasinoId=<%=bankInfo.getSubCasinoId()%>"><%=(subCasino.getId() + "-" + subCasino.getName())%>
</a>;
<b>Bank:</b>
<a href="/support/bankInfo.do?bankId=<%=bankInfo.getId()%>"><%=(bankInfo.getId() + "-" + bankInfo.getExternalBankIdDescription())%>
</a>
<br/>
<% } %>
<br/>
URLs:
<table border="1" cellpadding="7" cellspacing="0">
    <tr>
        <td>Site Lobby</td>
        <td><a href="<%=envValues.getLobbyUrl()%>"><%=envValues.getLobbyUrl()%>
        </a></td>
    </tr>
    <tr>
        <td>BSG Games</td>
        <td><a href="<%=envValues.getGamesUrl()%>"><%=envValues.getGamesUrl()%>
        </a></td>
    </tr>
</table>
<br/>
Accounts:<br/>
<table border="1" cellpadding="7" cellspacing="0">
    <tr>
        <td>Login</td>
        <td>Password</td>
        <td>Balance (fund)</td>
        <td>Currency</td>
        <td>InUse</td>
        <td>UserId</td>
        <td>Token (LongTerm)</td>
        <td>CW3 API Test</td>
    </tr>
    <%
        for (EnvironmentAccount account : envValues.getAccounts().getAccounts()) {
            TokenResponse tokenResponse =
                    null;
            try {
                tokenResponse = RESTServiceClient.getInstance()
                        .getActiveToken(bankInfo.getId(), account.getUserId()).getResponse();
            } catch (CommonException e) {
                e.printStackTrace(response.getWriter());
            }
    %>
    <tr>
        <td><%=account.getLogin()%>
        </td>
        <td><%=account.getPassword()%>
        </td>
        <td><%=account.getBalance()%>
            (<a href="<%=(request.getRequestURI() +
            "?bankId=" + bankInfo.getId() +
            "&userId=" + account.getUserId() +
            (isFromSupport ? "&fromSupport=1" : ""))%>">
                +1000
            </a>)
        </td>
        <td><%=account.getCurrency()%>
        </td>
        <td><%=account.getInUse()%>
        </td>
        <td><%=account.getUserId()%>
        </td>
        <td><%=(tokenResponse != null ? tokenResponse.getToken() + " (" + tokenResponse.getLongTerm() + ")" : "")%>
        </td>
        <td>
            <a href="<%=(checkAPIBase + "&token=" + tokenResponse.getToken())%>">token</a>
            <a href="<%=(checkAPIBase + "&userId=" + account.getUserId())%>">userId</a>
        </td>
    </tr>
    <%
            }
        }
    %>
</table>
