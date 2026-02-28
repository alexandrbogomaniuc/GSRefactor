<%@ page import="com.dgphoenix.casino.account.AccountManager" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.abs.casino.cassandra.persist.CassandraCommonGameWalletPersister" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.dgphoenix.casino.common.SessionHelper" %>
<%@ page import="com.dgphoenix.casino.common.transactiondata.ITransactionData" %>
<%@ page import="com.abs.casino.common.exception.WalletException" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.*" %>
<%@ page import="com.dgphoenix.casino.common.util.IdGenerator" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.IWallet" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.IOException" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    try {
        response.getWriter().println("<br>");

        String gameIdParam = request.getParameter("gameId");
        response.getWriter().println("gameIdParam = " + gameIdParam);
        response.getWriter().println("<br>");

        long gameId = Long.parseLong(gameIdParam);
        response.getWriter().println("gameId = " + gameId);
        response.getWriter().println("<br>");

        String accountIdParam = request.getParameter("accountId");
        response.getWriter().println("accountIdParam = " + accountIdParam);
        response.getWriter().println("<br>");

        long accountId = Long.parseLong(accountIdParam);
        response.getWriter().println("accountId = " + accountId);
        response.getWriter().println("<br>");

        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext().getBean("persistenceManager", CassandraPersistenceManager.class);
        CassandraCommonGameWalletPersister walletPersister = persistenceManager.getPersister(CassandraCommonGameWalletPersister.class);

        response.getWriter().println("Processing account with accountId = " + accountId);
        AccountInfo accountInfo = AccountManager.getInstance().getByAccountId(accountId);
        response.getWriter().println("accountInfo: " + accountInfo);
        response.getWriter().println("<br>");
        if (accountInfo == null) {
            return;
        }

        SessionHelper.getInstance().lock(accountInfo.getId());
        try {
            SessionHelper.getInstance().openSession();
            ITransactionData data = SessionHelper.getInstance().getTransactionData();
            response.getWriter().println("TransactionData: " + data);
            response.getWriter().println("<br>");

            CommonWallet wallet = (CommonWallet) data.getWallet();
            response.getWriter().println("CommonWallet from Transaction Data: " + wallet);
            response.getWriter().println("<br>");

            if (wallet == null) {
                response.getWriter().println("CommonWallet from Transaction Data: is null get it from walletPersister");
                response.getWriter().println("<br>");
                wallet = (CommonWallet) walletPersister.getWallet(accountInfo.getId());
                data.setWallet(wallet);
            }

            response.getWriter().println("CommonWallet: " + wallet);
            response.getWriter().println("<br>");

            if (wallet == null) {
                response.getWriter().println("CommonWallet: is null");
                response.getWriter().println("<br>");
            } else {
                Map<Integer, Long> unfinishedRounds = wallet.getUnfinishedGames();
                response.getWriter().println("unfinishedRounds: " + unfinishedRounds);
                response.getWriter().println("<br>");

                for (Integer unfinishedGameId : unfinishedRounds.keySet()) {

                    response.getWriter().println("Finishing unfinishedRound: roundId = " + unfinishedRounds.get(unfinishedGameId) + ", unfinishedGameId = " + unfinishedGameId);
                    response.getWriter().println("<br>");

                    if(unfinishedGameId != gameId) {
                        response.getWriter().println("unfinishedGameId != gameId skip Finishing unfinishedRound, unfinishedGameId =" + unfinishedGameId
                                + ", gameId = " + gameId + ", accountId = " + accountId);
                        response.getWriter().println("<br>");
                        continue;
                    }

                    CommonWalletOperation operation = wallet.getCurrentWalletOperation(unfinishedGameId);
                    response.getWriter().println("CurrentWalletOperation: " + operation);
                    response.getWriter().println("<br>");

                    if (operation != null) {
                        response.getWriter().println("Previous operation is not completed, skip Finishing unfinishedRound, gameId = " + gameId + ", accountId = " + accountId);
                        response.getWriter().println("<br>");
                        continue;
                    }

                    if (data.getGameSession() != null && data.getGameSession().getGameId() == unfinishedGameId) {

                        response.getWriter().println("Game is online, skip Finishing unfinishedRound, roundId = " +
                                unfinishedRounds.get(unfinishedGameId) + ", gameId = " + unfinishedGameId);
                        response.getWriter().println("<br>");
                        continue;
                    }

                    finishRound(unfinishedGameId, wallet, response);
                }

                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            }
        } catch (Exception e) {
            response.getWriter().println("Error on process accountId=" + accountId + ", message:" + e.getMessage());
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    } catch (Exception e) {
        response.getWriter().println("Error on process, message:" + e.getMessage());
    }
%>

<%!
    public void finishRound(Integer gameId, CommonWallet cWallet, HttpServletResponse response) throws CommonException, IOException {

        response.getWriter().println("finishRound: gameId = " + gameId + ", cWallet = " + cWallet);
        response.getWriter().println("<br>");

        CommonWalletOperation operation = cWallet.getCurrentWalletOperation(gameId);
        if (operation != null) {
            response.getWriter().println("finishRound: Error: gameId = " + gameId + ", previous operation is not completed, operation= " + operation);
            response.getWriter().println("<br>");
            throw new WalletException("previous operation is not completed");
        }

        response.getWriter().println("finishRound: gameId = " + gameId + ", setGameWalletRoundFinished = true");
        response.getWriter().println("<br>");
        cWallet.setGameWalletRoundFinished(gameId, true);

        Long roundId = cWallet.getGameWalletRoundId(gameId);
        response.getWriter().println("finishRound: gameId = " + gameId + ", getGameWalletRoundId = " + roundId);
        response.getWriter().println("<br>");
        
        if (roundId == null) {
            roundId = IdGenerator.getInstance().getNext(IWallet.class);

            response.getWriter().println("finishRound: gameId = " + gameId + ", setGameWalletRoundId = " + roundId);
            response.getWriter().println("<br>");
            
            cWallet.setGameWalletRoundId(gameId, roundId);
        }

        response.getWriter().println("finishRound: gameId = " + gameId + ", winAmount = 0, betAmount = 0, roundId = null");
        response.getWriter().println("<br>");
        
        cWallet.updateGameWallet(gameId, 0L, 0L, null);
    }
%>