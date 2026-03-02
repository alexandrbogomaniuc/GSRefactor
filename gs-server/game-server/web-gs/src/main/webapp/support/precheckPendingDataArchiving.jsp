<%@ page import="com.abs.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.abs.casino.cassandra.persist.CassandraTransactionDataPersister" %>
<%@ page import="com.abs.casino.common.SessionHelper" %>
<%@ page import="com.abs.casino.common.cache.ServerConfigsCache" %>
<%@ page import="com.abs.casino.common.cache.data.payment.bonus.CommonFRBonusWin" %>
<%@ page import="com.abs.casino.common.cache.data.payment.bonus.FRBWinOperation" %>
<%@ page import="com.abs.casino.common.cache.data.payment.bonus.FRBonusWin" %>
<%@ page import="com.abs.casino.common.transactiondata.ITransactionData" %>
<%@ page import="com.abs.casino.common.transactiondata.TrackingInfo" %>
<%@ page import="com.abs.casino.common.transactiondata.TrackingState" %>
<%@ page import="com.abs.casino.common.transactiondata.TrackingStatus" %>
<%@ page import="com.abs.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.abs.casino.common.util.Pair" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.CommonWallet" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.IWalletOperation" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.concurrent.TimeUnit" %>
<%@ page import="java.util.Date" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraTransactionDataPersister transactionDataPersister =
            persistenceManager.getPersister(CassandraTransactionDataPersister.class);

    List<TrackingStatus> statuses = new ArrayList<>();
    statuses.add(TrackingStatus.PENDING);
    statuses.add(TrackingStatus.ONLINE);
    statuses.add(TrackingStatus.TRACKING);

    long curTime = System.currentTimeMillis();
    long pendingDaysMin = 30;

    List<IWalletOperation> cwOperationsToArchiving = new ArrayList<>();
    List<FRBWinOperation> frbOperationsToArchiving = new ArrayList<>();
    int totalAffected = 0;

    List<Integer> gameServersIds = new ArrayList<>(ServerConfigsCache.getInstance().getAllObjects().keySet());
    for (Integer gameServer : gameServersIds) {
        for (TrackingStatus trackingStatus : statuses) {
            for (Pair<String, Pair<TrackingState, TrackingInfo>> trackingInfoPair :
                    transactionDataPersister.getTrackingInfo(trackingStatus, gameServer)) {
                try {
                    String lockId = trackingInfoPair.getKey();
                    SessionHelper.getInstance().lockByAccountHash(lockId);
                    try {
                        SessionHelper.getInstance().openSession();
                        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();

                        //Common wallet operations
                        CommonWallet wallet = (CommonWallet) transactionData.getWallet();
                        boolean affected = false;
                        if (wallet != null) {
                            for (int gameId : wallet.getWalletGamesIds()) {
                                IWalletOperation operation = wallet.getCurrentWalletOperation(gameId);
                                if (operation != null) {
                                    long days = TimeUnit.MILLISECONDS.toDays(curTime - operation.getStartTime());
                                    if (days > pendingDaysMin) {
                                        cwOperationsToArchiving.add(operation);
                                        affected = true;
                                        response.getWriter().println("<br>" + operation);
                                        response.getWriter().flush();
                                    }
                                }
                            }
                        }

                        //FRBonusWin operations
                        FRBonusWin frbWin = transactionData.getFrbWin();
                        if (frbWin != null) {
                            Map<String, CommonFRBonusWin> frBonusWins = frbWin.getFRBonusWins();
                            if (frBonusWins != null) {
                                for (CommonFRBonusWin frBonusWin : frBonusWins.values()) {
                                    if (frBonusWin != null) {
                                        FRBWinOperation operation = frBonusWin.getOperation();
                                        if (operation != null) {
                                            long days = TimeUnit.MILLISECONDS.toDays(curTime - operation.getStartTime());
                                            if (days > pendingDaysMin) {
                                                frbOperationsToArchiving.add(operation);
                                                affected = true;
                                                response.getWriter().println("<br>" + operation);
                                                response.getWriter().flush();
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (affected) {
                            totalAffected++;
                        }
                    } catch (Exception e) {
                        response.getWriter().println(e.getMessage());
                        response.getWriter().println("<br>");
                    } finally {
                        SessionHelper.getInstance().markTransactionCompleted();
                        SessionHelper.getInstance().clearWithUnlock();
                    }
                } catch (Exception e) {
                    response.getWriter().println("Can't lock lockId = " + trackingInfoPair.getKey());
                    response.getWriter().println("<br>");
                }
            }
        }
    }

    response.getWriter().println("<h3>Total TD will be affected = " + totalAffected + "</h3>");
    response.getWriter().println("<br>Total CommonWallet operations = " + cwOperationsToArchiving.size());
    response.getWriter().println("<br>Total FRBWin operations = " + frbOperationsToArchiving.size());

    /*response.getWriter().println("CommonWallet operations to archiving: <br><br>");
    for (IWalletOperation operation : cwOperationsToArchiving) {
        response.getWriter().println(operation + "<br>");
    }

    response.getWriter().println("<br><br>FRB operations to archiving: <br><br>");
    for (FRBWinOperation frbWinOperation : frbOperationsToArchiving) {
        response.getWriter().println(frbWinOperation + " date=" + new Date(frbWinOperation.getStartTime()) + "<br>");
    }*/
%>