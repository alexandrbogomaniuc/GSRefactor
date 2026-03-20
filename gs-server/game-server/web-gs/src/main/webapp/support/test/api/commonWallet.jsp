<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String bankIdParam = request.getParameter("bankId");
    String userIdParam = request.getParameter("userId");

    if (!StringUtils.isTrimmedEmpty(bankIdParam)) {
        StringBuilder target = new StringBuilder();
        target.append(request.getContextPath()).append("/tools/api/service.jsp?bankId=")
                .append(URLEncoder.encode(bankIdParam, "UTF-8"))
                .append("&fromSupport=1");
        if (!StringUtils.isTrimmedEmpty(userIdParam)) {
            target.append("&userId=").append(URLEncoder.encode(userIdParam, "UTF-8"));
        }
        response.sendRedirect(target.toString());
        return;
    }

    Map<Long, BankInfo> allBanksMap = BankInfoCache.getInstance().getAllObjects();
    List<Long> bankIds = new ArrayList<Long>(allBanksMap.keySet());
    Collections.sort(bankIds);
%>
<html>
<head>
    <title>Legacy CommonWallet EC API Test</title>
</head>
<body>
<h2>Legacy CommonWallet EC API Test</h2>
<p>
    This legacy endpoint now acts as a compatibility launcher into the current API service tool.
    The old scenario-based runner is not available in this branch, but you can still choose a bank
    and inspect the live wallet/service environment from here.
</p>
<form method="get" action="<%=request.getRequestURI()%>">
    <label for="bankId">Bank</label>
    <select name="bankId" id="bankId">
        <% for (Long bankId : bankIds) {
            BankInfo bankInfo = allBanksMap.get(bankId);
            String label = bankInfo == null ? String.valueOf(bankId) : bankInfo.getExternalBankIdDescription();
        %>
        <option value="<%=bankId%>"><%=bankId%> - <%=label%></option>
        <% } %>
    </select>
    <label for="userId">User ID (optional)</label>
    <input type="text" name="userId" id="userId" value=""/>
    <button type="submit">Open current tool</button>
</form>
</body>
</html>
