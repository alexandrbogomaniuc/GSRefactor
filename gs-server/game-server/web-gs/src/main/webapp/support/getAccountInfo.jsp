<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraLasthandPersister" %>
<%@ page import="com.dgphoenix.casino.common.SessionHelper" %>
<%@ page import="com.dgphoenix.casino.common.transactiondata.ITransactionData" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.CommonGameWallet" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.CommonWallet" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.transfer.PaymentTransaction" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bonus.FRBonus" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraFrBonusPersister" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%--
  Created by IntelliJ IDEA.
  User: zhevlakoval
  Date: 24.03.16
  Time: 12:41
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
    CassandraFrBonusPersister frBonusPersister = persistenceManager.getPersister(CassandraFrBonusPersister.class);
    CassandraLasthandPersister lasthandPersister = persistenceManager.getPersister(CassandraLasthandPersister.class);
    long accountId = Long.parseLong(request.getParameter("accountId"));
    SessionHelper.getInstance().lock(accountId);
    try {
        SessionHelper.getInstance().openSession();
        ITransactionData data = SessionHelper.getInstance().getTransactionData();


        if (data.getGameSession() != null || data.getPlayerSession() != null) {
%>
<p style="color: red;"> Player is online! No actions are allowed. </p>
<%
    }
    CommonWallet wallet = (CommonWallet) data.getWallet();
    if (wallet != null) {
%>
<b> Common game wallets </b>
<table border="1">
    <tr>
        <td> game id</td>
        <td> type</td>
        <td> external status</td>
        <td> game session</td>
        <td> round</td>
        <td> all</td>
    </tr>
    <%

        for (CommonGameWallet gameWallet : wallet.getCommonGameWallets()) {
            CommonWalletOperation operation = gameWallet.getBetOperation();
            if (operation == null) {
                operation = gameWallet.getWinOperation();
            }
            if (operation != null) {
    %>
    <tr>
        <td><%=gameWallet.getGameId()%>
        </td>
        <td><%=operation.getType()%>
        </td>
        <td><%=operation.getExternalStatus()%>
        </td>
        <td><%=operation.getGameSessionId()%>
        </td>
        <td><%=operation.getRoundId()%>
        </td>
        <td><%=operation.toString()%>
        </td>
        <%
            }

        %>
    </tr>
    <%
        }
    %>
</table>
<%
    }

    FRBonusWin frbWin = data.getFrbWin();
    if (frbWin != null) {
%>
frbWin : <%= frbWin.toString() %> <br/>


<%
    }

    AccountInfo accountInfo = data.getAccount();
    if (accountInfo != null) {
%>
accountInfo : <%= accountInfo.toString() %> <br/>

<%
    }
    List<FRBonus> dbBonuses = frBonusPersister.getActiveBonuses(accountId);
    if (dbBonuses != null) {
        for (FRBonus frBonus : dbBonuses) {
%>            frBonus : <%= frBonus.toString() %> <br/><%
        }
    }

    PaymentTransaction transaction = data.getPaymentTransaction();
%>
transaction: <%= transaction %><br/>
<%

    Map<Long, String> lastHands = lasthandPersister.getRealModeLasthands(accountId);
%>
<b>Last hands</b>
<table border="1">
    <tr>
        <td>game id</td>
        <td>last hand</td>
    </tr>
    <%
        for (Map.Entry<Long, String> lastHand : lastHands.entrySet()) {
    %>
    <tr>
        <td><%=lastHand.getKey()%>
        </td>
        <td><%=lastHand.getValue()%>
        </td>
    </tr>
    <%
        }
    %>
</table>
<%


    } catch (Exception e) {
        e.printStackTrace(response.getWriter());
    } finally {
        SessionHelper.getInstance().clearWithUnlock();
    }


%>
</body>
</html>
