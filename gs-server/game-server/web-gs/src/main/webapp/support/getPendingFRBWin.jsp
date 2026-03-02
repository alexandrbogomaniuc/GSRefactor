<%@ page import="com.abs.casino.cassandra.persist.CassandraAccountInfoPersister" %>
<%@ page import="com.abs.casino.common.SessionHelper" %>
<%@ page import="com.abs.casino.common.cache.data.payment.bonus.FRBonusWin" %>
<%@ page import="com.abs.casino.common.transactiondata.ITransactionData" %>
<%@ page import="java.util.List" %>
<%@ page import="com.abs.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.abs.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.abs.casino.common.util.ApplicationContextHelper" %>
<%--
  Created by IntelliJ IDEA.
  User: zhevlakoval
  Date: 18.05.16
  Time: 12:15
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>
</head>
<body>
<%
    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraAccountInfoPersister accountInfoPersister = persistenceManager.getPersister(CassandraAccountInfoPersister.class);
    long bankId = 1250L;

    List<Long> accountIds = accountInfoPersister.getAccountIds(bankId);

    response.getWriter().println("account count " + accountIds.size());
    int count = 0;
    for (Long accountId : accountIds) {
        count++;
        SessionHelper.getInstance().lock(accountId);
        try {
            SessionHelper.getInstance().openSession();
            ITransactionData data = SessionHelper.getInstance().getTransactionData();
            if (data != null) {
                FRBonusWin frbWin = data.getFrbWin();
                if (frbWin != null) {
                    ThreadLog.debug("getPendingFRBWin.jsp count" + count);
                    AccountInfo account = data.getAccount();
                    if (account != null) {
                        response.getWriter().println("ExternalId = " + account.getExternalId());
                    } else {
                        response.getWriter().println("!!! AccountId  = " + accountId);
                    }
                    response.getWriter().println("<br/>");
                    response.getWriter().println(frbWin.toString());
                    response.getWriter().println("<br/>");
                }
            }
        } catch (Exception e) {
            e.printStackTrace(response.getWriter());
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    }
%>
</body>
</html>