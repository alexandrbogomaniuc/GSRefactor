<%@ page import="com.dgphoenix.casino.common.SessionHelper" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.payment.IWallet" %>
<%@ page import="com.dgphoenix.casino.common.transactiondata.ITransactionData" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.cassandra.persist.CassandraTransactionDataPersister" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String _accountId = request.getParameter("accountId");
    if (StringUtils.isTrimmedEmpty(_accountId)) {
        response.getWriter().println("accountId not found \n <br/>");
    } else {
        long accountId = Long.parseLong(_accountId);
        SessionHelper.getInstance().lock(accountId);
        try {
            SessionHelper.getInstance().openSession();

            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
            IWallet wallet = transactionData.getWallet();
            if (wallet == null) {
                response.getWriter().println("wallet is already null \n <br/>");
            } else {
                transactionData.setWallet(null);
                SessionHelper.getInstance().commitTransaction();
                response.getWriter().println("wallet is removed \n <br/>");
            }

            SessionHelper.getInstance().markTransactionCompleted();
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    }


    response.getWriter().println("\n <br/>");
    response.getWriter().flush();
%>
