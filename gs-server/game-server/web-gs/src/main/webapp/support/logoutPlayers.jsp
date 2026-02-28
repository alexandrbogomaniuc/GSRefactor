<%@ page import="com.dgphoenix.casino.account.AccountManager" %>
<%@ page import="com.abs.casino.sm.IPlayerSessionManager" %>
<%@ page import="com.abs.casino.sm.PlayerSessionFactory" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.session.GameSession" %>
<%@ page import="com.dgphoenix.casino.common.util.Pair" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="com.abs.casino.cassandra.persist.CassandraTransactionDataPersister" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.dgphoenix.casino.common.SessionHelper" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<%
    Set<Long> gameIds = new HashSet<>();
    //gameIds.add(792L);
    //gameIds.add(798L);
    gameIds.add(775L);
    gameIds.add(776L);
    gameIds.add(777L);
    gameIds.add(778L);
    List<Long> bankIds = new ArrayList<>();
    bankIds.add(4379L);
    bankIds.add(4381L);
    bankIds.add(273L);

    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraTransactionDataPersister transactionDataPersister = persistenceManager.getPersister(CassandraTransactionDataPersister.class);

    for (long bankId : bankIds) {
        AccountManager accountManager = AccountManager.getInstance();
        try {
            IPlayerSessionManager psm = PlayerSessionFactory.getInstance().getPlayerSessionManager(bankId);
            Iterable<Pair<String, GameSession>> gameSessions = transactionDataPersister.getGameSessions((int) bankId);
            for (Pair<String, GameSession> pair : gameSessions) {
                if (gameIds.contains(pair.getValue().getGameId())) {
                    AccountInfo accountInfo = accountManager.getAccountInfo(pair.getValue().getAccountId());
                    if (accountInfo != null) {
                        logoutPlayer(response.getWriter(), accountInfo, pair.getKey(), "unj merging", psm);
                    }
                }
            }
        } catch (CommonException e) {
            response.getWriter().println("Unable close all player sessions for bank: " + bankId);
            response.getWriter().println("<br>");
            e.printStackTrace(response.getWriter());
            response.getWriter().println("<br>");
        }
    }
    response.getWriter().println("Done");
%>

<%!
    static public void logoutPlayer(PrintWriter writer, AccountInfo accountInfo, String lockId, String reason, IPlayerSessionManager psm) throws CommonException {
        SessionHelper sessionHelper = SessionHelper.getInstance();
        sessionHelper.lockByAccountHash(lockId);
        try {
            sessionHelper.openSession();
            psm.logout(accountInfo, reason, sessionHelper.getTransactionData().getPlayerSession());
            sessionHelper.commitTransaction();
            sessionHelper.markTransactionCompleted();
        } catch (CommonException e) {
            writer.println("Can't logout for accountId: " + accountInfo.getId());
            writer.println("<br>");
            e.printStackTrace(writer);
            writer.println("<br>");
        } finally {
            sessionHelper.clearWithUnlock();
        }
    }
%>