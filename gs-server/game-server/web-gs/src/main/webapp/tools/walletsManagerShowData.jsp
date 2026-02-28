<%@ page import="static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty" %>
<%@ page import="com.dgphoenix.casino.account.AccountManager" %>
<%@ page import="com.abs.casino.actions.support.walletsmanager.WalletsManagerAction" %>
<%@ page import="com.dgphoenix.casino.common.SessionHelper" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.LasthandInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.bonus.FRBWinOperation" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusNotification" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin" %>
<%@ page import="com.abs.casino.common.cache.data.payment.frb.FRBWinOperationStatus" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.session.GameSession" %>
<%@ page import="com.dgphoenix.casino.common.transactiondata.ITransactionData" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.bonus.tracker.FRBonusNotificationTracker" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.CommonWallet" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.WalletPersister" %>
<%@ page import="com.dgphoenix.casino.gs.persistance.LasthandPersister" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.time.Instant" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.ZoneId" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%--
  Created by IntelliJ IDEA.
  User: quant
  Date: 05.07.16
  Time: 13:17
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<style type="text/css">
    .success {
        color: green;
    }
    .error {
        color: red;
    }
</style>

<%!


    private String printWalletInfo(CommonWallet wallet, String gameId, boolean isPlaying) {
        StringBuilder res = new StringBuilder();
        if (wallet != null) {
            CommonWalletOperation operation = wallet.getCurrentWalletOperation(Integer.valueOf(gameId));
            if (operation != null) {
                res.append("Common game wallet:");
                res.append("<table border=\"1\">");
                res.append("<tr>");
                res.append("<td> id </td>");
                res.append("<td> type </td>");
                res.append("<td> amount </td>");
                res.append("<td> int status </td>");
                res.append("<td> external status </td>");
                res.append("<td> start time </td>");
                res.append("<td> action </td>");
                res.append("</tr>");

                res.append("<tr>");
                res.append("<td>").append(operation.getId()).append("</td>");
                res.append("<td>").append(operation.getType()).append("</td>");
                res.append("<td>").append(operation.getAmount()).append("</td>");
                res.append("<td>").append(operation.getInternalStatus()).append("</td>");
                res.append("<td>").append(fixPendingSendAlertSpell(String.valueOf(operation.getExternalStatus()))).append("</td>");
                res.append("<td>").append(new Date(operation.getStartTime())).append("</td>");
                if (!isPlaying) {
                    String onClick = "return confirmDeletion(" + gameId + ", '" + WalletsManagerAction.ACCOUNT_DATA_WALLET + "', " + operation.getId() + ");";
                    res.append("<td>").
                            append("<button name = \"changeType\" value=\"").
                            append("DELETED").
                            append("\" onclick=\"").append(onClick).append("\"> delete </button>").append("</td>");
                }
                res.append("</tr>");
                res.append("</table>");
            }
        }
        return res.toString();
    }

    private String printFRBWin(FRBonusWin frbonusWin, String gameId, boolean isPlaying) {
        StringBuilder res = new StringBuilder();
        if (frbonusWin != null) {
            FRBWinOperation operation = frbonusWin.getFRBonusWinOperation(Long.valueOf(gameId));
            if (operation != null) {
                res.append("FrbWin:");
                res.append("<table border=\"1\">");
                res.append("<tr>");
                res.append("<td> bonus id </td>");
                res.append("<td> amount </td>");
                res.append("<td> external status </td>");
                res.append("<td> start time </td>");
                res.append("<td> action </td>");
                res.append("</tr>");

                res.append("<tr>");
                res.append("<td>").append(operation.getBonusId()).append("</td>");
                res.append("<td>").append(operation.getAmount()).append("</td>");
                res.append("<td>").append(fixPendingSendAlertSpell(String.valueOf(operation.getExternalStatus()))).append("</td>");
                res.append("<td>").append(new Date(operation.getStartTime())).append("</td>");
                if (!isPlaying) {
                    String onClick = "return confirmDeletion(" + gameId + ", '" + WalletsManagerAction.ACCOUNT_DATA_FRB_WIN + "', " + operation.getId() + ");";
                    if (!operation.getExternalStatus().equals(FRBWinOperationStatus.COMPLETED)
                        /*operation.getExternalStatus().equals(FRBWinOperationStatus.PEENDING_SEND_ALERT)
                            || operation.getExternalStatus().equals(FRBWinOperationStatus.PENDING)
                            || operation.getExternalStatus().equals(FRBWinOperationStatus.FAIL)*/) {
                        res.append("<td>").
                                append("<button name = \"changeType\" value=\"").append("DELETED").append("\" onclick=\"").append(onClick).append("\"> delete </button>").append("</td>");
                    }
                }
                res.append("</tr>");
                res.append("</table>");
            }
        }
        return res.toString();
    }

    private String printFrbNotification(FRBonusNotification frBonusNotification) {
//        Example:
//        FRBonusNotification{id=44869625, accountId=2957074320, bonusId=1030934821, extBonusId='1801201016388603',
//                winSum=510, startTime=1516439956634, bonusStatus=CLOSED, externalStatus=STARTED}

        boolean isTracking = FRBonusNotificationTracker.getInstance().containsKey(frBonusNotification.getAccountId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(frBonusNotification.getStartTime()), ZoneId.systemDefault());

        StringBuilder res = new StringBuilder();
        res.append("FRBonusNotification:");
        res.append("<table border=\"1\">");
        res.append("<tr>");
        res.append("<td> ID </td>");
        res.append("<td> Bonus ID </td>");
        res.append("<td> Ext bonus ID </td>");
        res.append("<td> Win amount </td>");
        res.append("<td> Start time </td>");
        res.append("<td> Bonus status </td>");
        res.append("<td> Notification status </td>");
        res.append("<td> Delete </td>");
        res.append("<td> Restart </td>");
        res.append("</tr>");

        res.append("<tr>");
        res.append("<td>").append(frBonusNotification.getId()).append("</td>");
        res.append("<td>").append(frBonusNotification.getBonusId()).append("</td>");
        res.append("<td>").append(frBonusNotification.getExtBonusId()).append("</td>");
        res.append("<td>").append(frBonusNotification.getWinSum()).append("</td>");
        res.append("<td>").append(startTime.format(formatter)).append("</td>");
        res.append("<td>").append(frBonusNotification.getBonusStatus()).append("</td>");
        res.append("<td>").append(fixPendingSendAlertSpell(String.valueOf(frBonusNotification.getExternalStatus()))).append("</td>");

        String onClick = "return confirmDeletionFrbNotif('" + WalletsManagerAction.ACCOUNT_DATA_DEL_FRB_NOTIFICATION + "', " + frBonusNotification.getId() + ");";
        res.append("<td>").append("<button name = \"changeType\" value=\"").append("CANCELED")
                .append("\" onclick=\"").append(onClick).append("\"> delete </button>").append("</td>");

        if (!isTracking) {
            onClick = "return setActionParams(null, '" + WalletsManagerAction.ACCOUNT_DATA_RESTART_FRB_NOTIFICATION + "');";
            res.append("<td>").append("<button name = \"changeType\" value=\"").append("FAIL")
                    .append("\" onclick=\"").append(onClick).append("\"> restart </button>").append("</td>");
        } else {
            res.append("<td>").append("scheduled").append("</td>");
        }

        res.append("</tr>");
        return res.toString();
    }

    private String printLastHand(LasthandInfo lastHandInfo, long gameId, boolean isPlaying) {
        StringBuilder res = new StringBuilder();
        if (lastHandInfo != null) {
            String lastHand = lastHandInfo.getLasthandData();
            if (StringUtils.isNotEmpty(lastHand)) {
                res.append("Last hands:");
                res.append("<table border=\"1\">");
                res.append("<tr>");
                res.append("<td>last hand</td>");
                res.append("<td>action</td>");
                res.append("</tr>");

                res.append("<tr>");
                res.append("<td>");
                res.append("Public part");
                String publicLastHand = lastHand.substring(0, lastHand.indexOf('%'));
                if (StringUtils.isNotEmpty(publicLastHand)) {
                    res.append(": ").append(publicLastHand);
                } else {
                    res.append(" is empty.");
                }
                res.append("</td>");
                if (!isPlaying) {
                    String onClick = "return confirmDeletion(" + gameId + ", '" + WalletsManagerAction.ACCOUNT_DATA_LAST_HAND + "', " + gameId + ");";
                    res.append("<td>").
                            append("<button name = \"changeType\" value=\"").append("DELETED").append("\" onclick=\"").append(onClick).append("\"> delete </button>").append("</td>");
                }
                res.append("</tr>");
                res.append("</table>");
            }
        }
        return res.toString();
    }

    private String printInfo(CommonWallet wallet, FRBonusWin frbonusWin, long accountId, String gameId, boolean isPlaying) {
        StringBuilder res = new StringBuilder();
        long game = Long.valueOf(gameId);
        LasthandInfo lastHandInfo = LasthandPersister.getInstance().get(accountId, game);
        if ((wallet != null && wallet.getCurrentWalletOperation((int) game) != null)
                || (frbonusWin != null && frbonusWin.getCurrentFRBonusWinOperation(game) != null)
                || (lastHandInfo != null && StringUtils.isNotEmpty(lastHandInfo.getLasthandData()))) {
            String gameName = BaseGameInfoTemplateCache.getInstance().getGameNameById(game);
            gameName = (gameName != null) ? gameName : "Game name is unknown";
            String gameInfoString = "<b> Game id";
            if (isPlaying) {
                gameInfoString = "<b style=\"color: red;\"> Currently plaing game id";
            }
            res.append(gameInfoString).append(": ").append(game).append(" (").append(gameName).append(") </b> <br>");
            res.append(printWalletInfo(wallet, gameId, isPlaying));
            res.append(printFRBWin(frbonusWin, gameId, isPlaying));
            res.append(printLastHand(lastHandInfo, game, isPlaying));
            res.append("<br>");
        }
        return res.toString();
    }

    private String fixPendingSendAlertSpell(String str) {
        if (str == null) {
            return null;
        }

        return str.replace("PEENDING", "PENDING");
    }
%>

<%
    long accountId = 0;
    int bankId = 0;
    String extUserId = "";
    String accId = request.getParameter("accountId");
    if (!isTrimmedEmpty(accId)) {
        accountId = Long.valueOf(accId);
    } else {
        String bnkId = request.getParameter("bankId");
        if (!isTrimmedEmpty(bnkId)) {
            bankId = Integer.valueOf(bnkId);
        }
        extUserId = request.getParameter("extUserId");
    }
%>

<%
    AccountInfo accountInfo = null;
    Object operationStatus = request.getAttribute("operationStatus");
    if (operationStatus != null) {
        if (operationStatus.toString().contains("success")) {%>
<p class="success"><%=operationStatus%>
</p> <br>
<%} else {%>
<p class="error"><%=operationStatus%>
</p> <br>
<%
        }
    }
    if (accountId != 0) {
        accountInfo = AccountManager.getInstance().getByAccountId(accountId);
    } else if (bankId != 0 && extUserId != null) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo != null) {
            accountInfo = AccountManager.getInstance().getByCompositeKey(bankInfo.getSubCasinoId(), bankInfo, extUserId);
        } else {
%>
<p class="error"> Bank id <%=bankId%> not exists. </p>
<%
        }
    }

    if (accountInfo == null) {
        if (accountId != 0 || bankId != 0) {
%>
<p class="error"> Cannot find the player. </p>
<%
    }
} else {
    try {
        accountId = accountInfo.getId();
        SessionHelper.getInstance().lock(accountId);
        try {
            SessionHelper.getInstance().openSession();
            ITransactionData data = SessionHelper.getInstance().getTransactionData();

            Set<String> gameIds = new HashSet<>();
            CommonWallet wallet = (CommonWallet) WalletPersister.getInstance().getWallet(accountId);
            if (wallet != null) {
                for (Integer gameId : wallet.getWalletGamesIds()) {
                    gameIds.add(gameId.toString());
                }
            }
            FRBonusWin frbonusWin = data.getFrbWin();
            if (frbonusWin != null) {
                gameIds.addAll(frbonusWin.getFRBonusWins().keySet());
            } else {
%><p class="success">There is no stuck FRBonusWin</p><%
    }

    GameSession gameSession = data.getGameSession();
    if (gameSession != null) {%>
<%=printInfo(wallet, frbonusWin, accountId, String.valueOf(gameSession.getGameId()), true)%>
<%
        gameIds.remove(String.valueOf(gameSession.getGameId()));
    }

    if (gameIds.size() < 1) {
%><p class="success">There is no wallet operations</p><%
    }

    for (String gameId : gameIds) {%>
<%=printInfo(wallet, frbonusWin, accountId, gameId, false)%>
<%
    }

    FRBonusNotification frBonusNotification = data.getFrbNotification();
    if (frBonusNotification != null) {
%><%=printFrbNotification(frBonusNotification)%><%
} else {
%><p class="success">There is no stuck FRBonusNotification</p><%
        }
    } finally {
        SessionHelper.getInstance().clearWithUnlock();
    }
} catch (Exception e) {
%><p class="error"> Internal error. </p><%
        }
    }
%>
