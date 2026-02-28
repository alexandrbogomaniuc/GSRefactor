<%@ page import="com.abs.casino.cassandra.persist.CassandraLasthandPersister" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.abs.casino.cassandra.persist.CassandraCommonGameWalletPersister" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.IWallet" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%
    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraLasthandPersister lasthandPersister = persistenceManager.getPersister(CassandraLasthandPersister.class);
    CassandraCommonGameWalletPersister commonGameWalletPersister =
            persistenceManager.getPersister(CassandraCommonGameWalletPersister.class);
    String aid = request.getParameter("aid");
    if (aid != null && !aid.isEmpty()) {
        long accountId = Long.parseLong(aid);
        Map<Long, String> realModeLasthands = lasthandPersister.getRealModeLasthands(accountId);
        response.getWriter().write("realModeLasthands: " + realModeLasthands + "</br></br>");

        IWallet wallet = commonGameWalletPersister.getWallet(accountId);
        response.getWriter().write(wallet.toString());
    }
%>