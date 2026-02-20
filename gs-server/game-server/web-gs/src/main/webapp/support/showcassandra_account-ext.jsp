<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraAccountInfoPersister" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraAccountInfoPersister accountInfoPersister = persistenceManager.getPersister(CassandraAccountInfoPersister.class);
    String s = request.getParameter("extId");
    String b = request.getParameter("bankId");
    if (!StringUtils.isTrimmedEmpty(s) && !StringUtils.isTrimmedEmpty(b)) {
        long bankId = Long.parseLong(b);
        AccountInfo accountInfo = accountInfoPersister.getByCompositeKey(bankId, s);
        if (accountInfo == null) {
            response.getWriter().write("Account not found");
            return;
        }

        response.getWriter().print("accountInfo=" + accountInfo);

    }

    response.getWriter().print("\n</br>");
    response.flushBuffer();
%>