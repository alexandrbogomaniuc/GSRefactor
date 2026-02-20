<%@ page import="com.dgphoenix.casino.common.cache.CurrencyCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="com.dgphoenix.casino.common.SessionHelper" %>
<%@ page import="com.dgphoenix.casino.common.transactiondata.ITransactionData" %>
<%@ page import="com.dgphoenix.casino.account.AccountManager" %>
<%--
  Created by IntelliJ IDEA.
  User: zhevlakoval
  Date: 08.12.14
  Time: 11:20
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>
</head>
<body>
<%!
    PrintWriter writer;
    long[] accounts = new long[]{
            2863601924L,
            2863601921L,
            2863601925L
    };
    Currency currency = CurrencyCache.getInstance().get("ARS");
%>
<%
    writer = response.getWriter();
    writer.println(currency + "<br/>");
    try {
        for (long accountId : accounts) {
            SessionHelper.getInstance().lock(accountId);
            try {
                SessionHelper.getInstance().openSession();
                AccountManager.getInstance().getAccountInfo(accountId, true);
                ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
                AccountInfo account = transactionData.getAccount();
                account.setCurrency(currency);
                account.setCountryCode(currency.getCode());
                SessionHelper.getInstance().commitTransaction();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }
        writer.println("finish<br/>");
    } catch (Exception ex) {
        ex.printStackTrace(writer);
    }
%>
</body>
</html>