<%--
  Created by IntelliJ IDEA.
  User: zhevlakoval
  Date: 02.06.11
  Time: 12:54
  TansferConfiguration isPost set false
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="java.util.Map" %>
<html>
<head><title>TansferConfiguration isPost set false</title></head>
<body>
<%
    try {

        Map<Long, BankInfo> banks = BankInfoCache.getInstance().getAllObjects();

        for (Map.Entry<Long, BankInfo> entryB : banks.entrySet()) {
            long bankId = entryB.getValue().getId();
            BankInfoCache.getInstance().getBankInfo(bankId).setProperty(BankInfo.KEY_CT_REST_ISPOST, Boolean.FALSE.toString());
        }
        response.getWriter().print("OK");
    } catch (Exception e) {
        response.getWriter().print("ERROR " + e.getMessage());
    }

    response.flushBuffer();

%>
</body>
</html>