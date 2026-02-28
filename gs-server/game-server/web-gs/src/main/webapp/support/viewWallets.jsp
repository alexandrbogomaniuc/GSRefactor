<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraTransactionDataPersister" %>
<%@ page import="com.abs.casino.common.transactiondata.TrackingStatus" %>
<%@ page import="com.abs.casino.common.transactiondata.TrackingState" %>
<%@ page import="com.abs.casino.common.transactiondata.TrackingInfo" %>
<%@ page import="com.dgphoenix.casino.common.util.Pair" %>
<%@ page import="com.dgphoenix.casino.common.SessionHelper" %>
<%@ page import="com.dgphoenix.casino.common.transactiondata.ITransactionData" %>
<%@ page import="com.dgphoenix.casino.common.cache.ServerConfigsCache" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.common.cache.data.payment.bonus.CommonFRBonusWin" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.CommonWallet" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.CommonGameWallet" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.IWallet" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation" %>
<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="org.apache.commons.lang3.exception.ExceptionUtils" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.bonus.FRBWinOperation" %>
<%@ page import="com.abs.casino.common.cache.data.payment.frb.FRBWinOperationStatus" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%--
  Created by IntelliJ IDEA.
  User: vladislav
  Date: 05/08/15
  Time: 14:34
  To change this template use File | Settings | File Templates.
--%>

<%!

    private class GsWithTransactions {

        public final int gameServerId;
        public final Map<TrackingStatus, List<ITransactionData>> transactionsDataByStatusMap = new EnumMap<>(TrackingStatus.class);
        public final Map<TrackingStatus, List<ITransactionData>> transactionsDataWithFRBByStatusMap = new EnumMap<>(TrackingStatus.class);
        public final Map<TrackingStatus, List<ITransactionData>> transactionsDataWithPaymentTransactionByStatusMap = new EnumMap<>(TrackingStatus.class);

        public GsWithTransactions(int gameServerId) {
            this.gameServerId = gameServerId;
        }

    }

    private boolean hasFrbWin(ITransactionData transactionData) {
        FRBonusWin frbWin = transactionData.getFrbWin();
        if (frbWin != null) {
            Map<String, CommonFRBonusWin> frBonusWins = frbWin.getFRBonusWins();

            if (frBonusWins != null) {
                for (CommonFRBonusWin frBonusWin : frBonusWins.values()) {
                    if (frBonusWin != null) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private String replacePendingStatusOnTrackButton(String str, long accountId, long gameId, boolean isFRBOperation) {
        return str.replaceAll("PEENDING_SEND_ALERT",
                "<a class='trackButton' href='javascript:trackGameWallet(" + accountId + "," + gameId + "," + isFRBOperation + ")'>PEENDING_SEND_ALERT</a>");
    }

    private String getTransactionDataInfoAsString(ITransactionData transactionData) {
        CommonWallet wallet = (CommonWallet) transactionData.getWallet();
        StringBuilder walletStringBuilder = new StringBuilder();
        if (wallet != null) {
            walletStringBuilder.append("<b> Wallet [accountId=").append(wallet.getAccountId()).append("] GameWallets:</b><br>");

            Set<Integer> walletGamesIds = wallet.getWalletGamesIds();
            if (walletGamesIds != null) {
                for (Integer gameId : walletGamesIds) {
                    CommonGameWallet gameWallet = wallet.getGameWallet(gameId);
                    walletStringBuilder.append("<div name='").append(gameId).append("'> <b>GameId=").append(gameId).append(":</b>").
                            append(replacePendingStatusOnTrackButton(gameWallet.toString(), wallet.getAccountId(), gameId, false)).append("</div>");
                }
            }
        }

        FRBonusWin generalFRBWin = transactionData.getFrbWin();
        StringBuilder frbWinStringBuilder = new StringBuilder();
        if (generalFRBWin != null) {
            frbWinStringBuilder.append("<b> FrbWin [accountId=").append(generalFRBWin.getAccountId()).append("] FrbWins:</b><br>");

            Map<String, CommonFRBonusWin> frBonusWins = generalFRBWin.getFRBonusWins();
            if (frBonusWins != null) {
                for (CommonFRBonusWin frBonusWin : frBonusWins.values()) {
                    frbWinStringBuilder.append("<div name='").append(frBonusWin.getGameId()).append("FRB").append("'> <b>GameId=").append(frBonusWin.getGameId()).append(":</b>").
                            append(replacePendingStatusOnTrackButton(frBonusWin.toString(), generalFRBWin.getAccountId(), frBonusWin.getGameId(), true)).append("</div>");
                }
            }
        }

        StringBuilder transactionStringBuilder = new StringBuilder();
        transactionStringBuilder.append("<b> AccountId </b>").append(transactionData.getAccountId()).append("<br>").
                append("<b> lockId </b>").append(transactionData.getLockId()).append("<br>").
                append("<b> AccountInfo </b>").append(transactionData.getAccount()).append("<br>").
                append("<b> PlayerSession </b>").append(transactionData.getPlayerSession()).append("<br>").
                append("<b> GameSession </b>").append(transactionData.getGameSession()).append("<br>").
                append("<b> LastHand </b>").append(transactionData.getLasthand()).append("<br>").
                append("<b> LastBet </b>").append(transactionData.getLastBet()).append("<br>").
                append(walletStringBuilder).
                append("<b> LastUpdateInfo </b>").append(transactionData.getLastUpdateInfo()).append("<br>").
                append("<b> Bonus </b>").append(transactionData.getBonus()).append("<br>").
                append("<b> FrBonus </b>").append(transactionData.getFrBonus()).append("<br>").
                append(frbWinStringBuilder).
                append("<b> PaymentTransaction </b>").append(transactionData.getPaymentTransaction()).append("<br>").
                append("<b> frbNotification </b>").append(transactionData.getFrbNotification()).append("<br>").
                append("<b> PromoMembers </b>").append(transactionData.getPromoMemberInfos()).append("<br>").
                append("<b> TrackingState </b>").append(transactionData.getTrackingState()).append("<br>").
                append("<b> NeedRemove= </b>").append(transactionData.isNeedRemove()).append("<br>").
                append("<b> TrackingStateChanged= </b>").append(transactionData.isTrackingStateChanged()).append("<br>").
                append("<b> TrackingInfo </b>").append(transactionData.getTrackingInfo()).append("<br>").
                append("<b> AtomicallyStoredData </b>").append(transactionData.getAtomicallyStoredData());
        return transactionStringBuilder.toString();
    }

%>

<%
    //ajax handler for track of pending wallet
    String accountIdAsString = request.getParameter("accountId");
    String gameIdAsString = request.getParameter("gameId");
    String isFRBAsString = request.getParameter("isFRB");

    if (!StringUtils.isTrimmedEmpty(accountIdAsString) && !StringUtils.isTrimmedEmpty(gameIdAsString) && !StringUtils.isTrimmedEmpty(isFRBAsString)) {
        try {
            Long accountId = Long.valueOf(accountIdAsString);
            Integer gameId = Integer.valueOf(gameIdAsString);
            Boolean isFRB = Boolean.valueOf(isFRBAsString);

            SessionHelper.getInstance().lock(accountId);
            try {
                SessionHelper.getInstance().openSession();
                ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();

                TrackingState trackingState = transactionData.getTrackingState();
                if (trackingState.getStatus() == TrackingStatus.PENDING) {
                    trackingState.setStatus(TrackingStatus.TRACKING);
                    transactionData.updateTrackingState(trackingState.getGameServerId());
                }

                if (isFRB) {
                    FRBonusWin generalFRBWin = transactionData.getFrbWin();
                    if (generalFRBWin != null) {
                        CommonFRBonusWin frbWin = generalFRBWin.getFRBWin(gameId);
                        if (frbWin != null) {
                            FRBWinOperation operation = frbWin.getOperation();
                            if (operation != null) {
                                operation.setExternalStatus(FRBWinOperationStatus.FAIL);
                            }
                        }
                    }

                } else {
                    IWallet wallet = transactionData.getWallet();
                    if (wallet != null) {
                        CommonWallet commonWallet = (CommonWallet) wallet;
                        CommonWalletOperation currentWalletOperation = commonWallet.getCurrentWalletOperation(gameId);
                        if (currentWalletOperation != null) {
                            currentWalletOperation.setExternalStatus(WalletOperationStatus.FAIL);
                        }
                    }
                }

                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();

                response.getWriter().write("OK");
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (Exception e) {
            ThreadLog.error("viewWallets.jsp error", e);
            response.getWriter().write(ExceptionUtils.getStackTrace(e));
        }

        return;
    }
    ///////////////////

    //collect all transactions
    List<Integer> gameServersIds = new ArrayList<>(ServerConfigsCache.getInstance().getAllObjects().keySet());
    Collections.sort(gameServersIds);

    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraTransactionDataPersister transactionDataPersister =
            persistenceManager.getPersister(CassandraTransactionDataPersister.class);

    final List<GsWithTransactions> gameServersWithTransactions = new ArrayList<>();
    for (Integer gameServerId : gameServersIds) {
        GsWithTransactions gsWithTransactions = new GsWithTransactions(gameServerId);

        for (TrackingStatus trackingStatus : TrackingStatus.values()) {
            gsWithTransactions.transactionsDataByStatusMap.put(trackingStatus, new ArrayList<ITransactionData>());
            gsWithTransactions.transactionsDataWithFRBByStatusMap.put(trackingStatus, new ArrayList<ITransactionData>());
            gsWithTransactions.transactionsDataWithPaymentTransactionByStatusMap.put(trackingStatus, new ArrayList<ITransactionData>());
        }

        for (TrackingStatus trackingStatus : TrackingStatus.values()) {
            for (Pair<String, Pair<TrackingState, TrackingInfo>> trackingInfoPair :
                    transactionDataPersister.getTrackingInfo(trackingStatus, gameServerId)) {
                try {
                    String lockId = trackingInfoPair.getKey();
                    ITransactionData transactionData = transactionDataPersister.getFromDB(lockId);

                    if (transactionData.getPaymentTransaction() == null || trackingStatus == TrackingStatus.ONLINE) {
                        gsWithTransactions.transactionsDataByStatusMap.get(trackingStatus).add(transactionData);
                    }

                    if (transactionData.getPaymentTransaction() != null) {
                        if (trackingStatus == TrackingStatus.ONLINE) {
                            gsWithTransactions.transactionsDataWithPaymentTransactionByStatusMap.get(TrackingStatus.TRACKING).add(transactionData);
                        } else {
                            gsWithTransactions.transactionsDataWithPaymentTransactionByStatusMap.get(trackingStatus).add(transactionData);
                        }
                    }

                    if (hasFrbWin(transactionData)) {
                        gsWithTransactions.transactionsDataWithFRBByStatusMap.get(trackingStatus).add(transactionData);
                    }

                } catch (Exception ignored) {
                    //ignore
                }
            }
        }

        gameServersWithTransactions.add(gsWithTransactions);
    }
    //////////////////////////////////////////////

    //calculate transactions count
    int totalOnline = 0;
    int totalPendingWallets = 0;
    int totalTrackingWallets = 0;
    int totalTrackingPaymentTransactions = 0;
    int totalPendingWithPaymentTransactions = 0;
    int totalTrackingFRB = 0;
    int totalPendingFRB = 0;

    for (GsWithTransactions gameServerWithTransactions : gameServersWithTransactions) {
        totalOnline += gameServerWithTransactions.transactionsDataByStatusMap.get(TrackingStatus.ONLINE).size();
        totalTrackingWallets += gameServerWithTransactions.transactionsDataByStatusMap.get(TrackingStatus.TRACKING).size();
        totalPendingWallets += gameServerWithTransactions.transactionsDataByStatusMap.get(TrackingStatus.PENDING).size();
        totalTrackingPaymentTransactions += gameServerWithTransactions.transactionsDataWithPaymentTransactionByStatusMap.get(TrackingStatus.TRACKING).size();
        totalPendingWithPaymentTransactions += gameServerWithTransactions.transactionsDataWithPaymentTransactionByStatusMap.get(TrackingStatus.PENDING).size();
        totalTrackingFRB += gameServerWithTransactions.transactionsDataWithFRBByStatusMap.get(TrackingStatus.TRACKING).size();
        totalPendingFRB += gameServerWithTransactions.transactionsDataWithFRBByStatusMap.get(TrackingStatus.PENDING).size();
    }
    /////////////////////////////

%>

<script type="text/javascript" src="/support/js/jquery-1.3.2.min.js"></script>

<script>

    function redirect(gameServerId, trackingStatus, hasFRB, hasPT) {
        window.location = window.location.protocol + "//" + window.location.host + "" + window.location.pathname +
                "?" + "gsId=" + gameServerId + "&status=" + trackingStatus + "&hasFRB=" + hasFRB + "&hasPT=" + hasPT;
    }

    function trackGameWallet(accountId, gameId, isFRBOperation) {
        $.post("viewWallets.jsp", {accountId: accountId, gameId: gameId, isFRB: isFRBOperation},
                function (data) {
                    if (data.trim() == "OK") {
                        var additionalFRBParam = "";
                        if (isFRBOperation == true) {
                            additionalFRBParam = "FRB";
                        }

                        $("div.transaction[name='" + accountId + "'] div[name='" + gameId + "" + additionalFRBParam + "'] a").each(function (i, obj) {
                            $(this).replaceWith("FAIL");
                        });
                    } else {
                        $("body").replaceWith(data);
                    }
                });
    }

</script>

<style type="text/css">
    .transaction {
        margin: 4px;
        border: 1px solid;
    }

    .trackButton {
        background-color: #d0451b;
        -moz-border-radius: 3px;
        -webkit-border-radius: 3px;
        border-radius: 3px;
        border: 1px solid #942911;
        display: inline-block;
        cursor: pointer;
        color: #ffffff;
        font-size: 13px;
        padding: 6px 24px;
        text-decoration: none;
        font-family: Arial;
    }

    .trackButton:hover {
        background-color: #bc3315;
    }

    .trackButton:active {
        position: relative;
        top: 1px;
    }
</style>


<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>View Transaction Data</title>
</head>
<body>

<table border="1px">
    <tr>
        <td>Server</td>
        <td>Online</td>
        <td>Tracking_Wallets</td>
        <td>Pending_Wallets</td>
        <td>Tracking_PaymentTransactions</td>
        <td>Pending_PaymentTransactions</td>
        <td>Tracking_FRBWins</td>
        <td>Pending_FRBWins</td>
    </tr>

    <%
        for (GsWithTransactions serverWithTransactions : gameServersWithTransactions) {
    %>
    <tr>
        <td><%=serverWithTransactions.gameServerId%>
        </td>
        <td>
            <a href="javascript:redirect('<%=serverWithTransactions.gameServerId%>', '<%=TrackingStatus.ONLINE%>')">
                <%=serverWithTransactions.transactionsDataByStatusMap.get(TrackingStatus.ONLINE).size()%>
            </a>
        </td>
        <td>
            <a href="javascript:redirect('<%=serverWithTransactions.gameServerId%>', '<%=TrackingStatus.TRACKING%>')">
                <%=serverWithTransactions.transactionsDataByStatusMap.get(TrackingStatus.TRACKING).size()%>
            </a>
        </td>
        <td>
            <a href="javascript:redirect('<%=serverWithTransactions.gameServerId%>', '<%=TrackingStatus.PENDING%>')">
                <%=serverWithTransactions.transactionsDataByStatusMap.get(TrackingStatus.PENDING).size()%>
            </a>
        </td>
        <td>
            <a href="javascript:redirect('<%=serverWithTransactions.gameServerId%>', '<%=TrackingStatus.TRACKING%>', 'false', 'true')">
                <%=serverWithTransactions.transactionsDataWithPaymentTransactionByStatusMap.get(TrackingStatus.TRACKING).size()%>
            </a>
        </td>
        <td>
            <a href="javascript:redirect('<%=serverWithTransactions.gameServerId%>', '<%=TrackingStatus.PENDING%>', 'false', 'true')">
                <%=serverWithTransactions.transactionsDataWithPaymentTransactionByStatusMap.get(TrackingStatus.PENDING).size()%>
            </a>
        </td>
        <td>
            <a href="javascript:redirect('<%=serverWithTransactions.gameServerId%>', '<%=TrackingStatus.TRACKING%>', 'true')">
                <%=serverWithTransactions.transactionsDataWithFRBByStatusMap.get(TrackingStatus.TRACKING).size()%>
            </a>
        </td>
        <td>
            <a href="javascript:redirect('<%=serverWithTransactions.gameServerId%>', '<%=TrackingStatus.PENDING%>', 'true')">
                <%=serverWithTransactions.transactionsDataWithFRBByStatusMap.get(TrackingStatus.PENDING).size()%>
            </a>
        </td>
    </tr>
    <%
        }
    %>

    <tr>
        <td>Summary</td>
        <td><%=totalOnline%>
        </td>
        <td><%=totalTrackingWallets%>
        </td>
        <td><%=totalPendingWallets%>
        </td>
        <td><%=totalTrackingPaymentTransactions%>
        </td>
        <td><%=totalPendingWithPaymentTransactions%>
        </td>
        <td><%=totalTrackingFRB%>
        </td>
        <td><%=totalPendingFRB%>
        </td>
    </tr>

</table>


<div id="transactionsList">
    <%
        try {
            String gameServerIdAsString = request.getParameter("gsId");
            String trackingStatusAsString = request.getParameter("status");
            String hasFRB = request.getParameter("hasFRB");
            String hasPT = request.getParameter("hasPT");

            if (!StringUtils.isTrimmedEmpty(gameServerIdAsString) && !StringUtils.isTrimmedEmpty(trackingStatusAsString)) {
                Integer gameServerId = Integer.valueOf(gameServerIdAsString);
                TrackingStatus trackingStatus = TrackingStatus.valueOf(trackingStatusAsString);

                Iterator<ITransactionData> iterator;
                if (!StringUtils.isTrimmedEmpty(hasFRB) && hasFRB.equals("true")) {
                    iterator = gameServersWithTransactions.get(gameServerId - 1).transactionsDataWithFRBByStatusMap.get(trackingStatus).iterator();

                } else if (!StringUtils.isTrimmedEmpty(hasPT) && hasPT.equals("true")) {
                    iterator = gameServersWithTransactions.get(gameServerId - 1).transactionsDataWithPaymentTransactionByStatusMap.get(trackingStatus).iterator();

                } else {
                    iterator = gameServersWithTransactions.get(gameServerId - 1).transactionsDataByStatusMap.get(trackingStatus).iterator();
                }

                while (iterator.hasNext()) {
                    ITransactionData transactionData = iterator.next();
                    long accountId = transactionData.getAccountId() == 0 ?
                            transactionData.getWallet() == null ?
                                    transactionData.getPaymentTransaction() == null ? 0 : transactionData.getPaymentTransaction().getAccountId()
                                    : transactionData.getWallet().getAccountId()
                            : transactionData.getAccountId();
    %>
    <div class="transaction" name="<%=accountId%>">
        <%=getTransactionDataInfoAsString(transactionData)%>
    </div>
    <%
                }
            }

        } catch (Exception ignored) {
            response.getWriter().write("<b>Incorrect parameters</b>");
        }
    %>

</div>


</body>
</html>

