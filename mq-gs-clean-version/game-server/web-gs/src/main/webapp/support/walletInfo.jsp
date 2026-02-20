<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.CommonGameWallet" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.CommonWallet" %>
<%@ page import="static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty" %>
<%@ page import="com.dgphoenix.casino.common.SessionHelper" %>
<%@ page import="com.dgphoenix.casino.common.transactiondata.ITransactionData" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraLasthandPersister" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraTransactionDataPersister" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%--
  Created by IntelliJ IDEA.
  User: quant
  Date: 27.01.16
  Time: 16:40
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <script type="text/javascript" src="/support/js/jquery-1.3.2.min.js"></script>

    <script>
        function setActionParams(gameId, accountData) {
            $("#gameId")[0].value = gameId;
            $("#accountData")[0].value = accountData;
            return true;
        }

        function showData(accountId) {
            $.ajax({
                type: "POST",
                url: 'walletInfo.jsp',
                data: "accountId=" + accountId,
                success: function (data) {
                    $("html").html(data);
                },
                error: function (xhr, status, error) {
                    $("html").html(xhr.responseText);
                }
            });
        }
    </script>

    <title></title>
</head>
<body>

<%
    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraLasthandPersister lasthandPersister = persistenceManager.getPersister(CassandraLasthandPersister.class);
    long accountId = 0;
    String accId = request.getParameter("accountId");
    if (!isTrimmedEmpty(accId)) {
        accountId = Long.valueOf(accId);
    }
%>

<html:form action="/support/walletinfo" method="post">
    <table>
        <tr>
            <td>
                Account id
                <html:text styleId="accountId" property="accountId" value='<%=(accountId != 0 ? String.valueOf(accountId) : "")%>'/>
            </td>
            <td>
                <html:hidden styleId="gameId" property="gameId"/>
            </td>
            <td>
                <html:hidden styleId="accountData" property="accountData"/>
            </td>
        </tr>
        <tr>
            <td>
                <a href="javascript:showData($('#accountId')[0].value)">show wallets and lastHands</a>
            </td>
        </tr>
    </table>
    <%
        if (accountId != 0) {
            SessionHelper.getInstance().lock(accountId);
            try {
                SessionHelper.getInstance().openSession();
                ITransactionData data = SessionHelper.getInstance().getTransactionData();

                if (data.getGameSession() != null || data.getPlayerSession() != null) {%>
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
            <td> action</td>
        </tr>
        <%
            for (CommonGameWallet gameWallet : wallet.getCommonGameWallets()) {
                CommonWalletOperation operation = gameWallet.getBetOperation();
                if (operation == null) {
                    operation = gameWallet.getWinOperation();
                }
                if (operation != null) {%>
        <tr>
            <td><%=gameWallet.getGameId()%>
            </td>
            <td><%=operation.getType()%>
            </td>
            <td><%=operation.getExternalStatus()%>
            </td>
            <%
                if (data.getGameSession() == null && data.getPlayerSession() == null) {
                    String onClick = "return setActionParams(" + gameWallet.getGameId() + ", '" + CassandraTransactionDataPersister.WALLET_FIELD + "');";
                    if (operation.getExternalStatus().equals(WalletOperationStatus.PEENDING_SEND_ALERT)) {%>
            <td>
                <button name="changeType" value="<%="RESOLVED"%>" onclick="<%=onClick%>"> resolve</button>
            </td>
            <%} else {%>
            <td>
                <button name="changeType" value="<%="DELETED"%>" onclick="<%=onClick%>"> delete</button>
                <button name="changeType" value="<%="UNRESOLVED"%>" onclick="<%=onClick%>"> suspend</button>
            </td>
            <%
                    }
                }
            %>
        </tr>
        <%
                }
            }
        %>
    </table>
    <%
        }

        Map<Long, String> lastHands = lasthandPersister.getRealModeLasthands(accountId);
    %>
    <b>Last hands</b>
    <table border="1">
        <tr>
            <td>game id</td>
            <td>last hand</td>
            <td>action</td>
        </tr>
        <%
            for (Map.Entry<Long, String> lastHand : lastHands.entrySet()) {%>
        <tr>
            <td><%=lastHand.getKey()%>
            </td>
            <td><%=lastHand.getValue()%>
            </td>
            <%
                if (data.getGameSession() == null && data.getPlayerSession() == null) {
                    String onClick = "return setActionParams(" + lastHand.getKey() + ", '" + CassandraTransactionDataPersister.LAST_HAND_FIELD + "');";
            %>
            <td>
                <button name="changeType" value="<%="DELETED"%>" onclick="<%=onClick%>"> delete</button>
            </td>
            <%}%>
        </tr>
        <%}%>
    </table>
    <%
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }
    %>
</html:form>

</body>
</html>
