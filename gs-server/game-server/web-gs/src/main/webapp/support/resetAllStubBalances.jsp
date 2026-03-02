<%@ page import="com.abs.casino.gs.managers.payment.wallet.RemoteClientStubHelper" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    RemoteClientStubHelper.getInstance().resetAllStubBalances();
%>