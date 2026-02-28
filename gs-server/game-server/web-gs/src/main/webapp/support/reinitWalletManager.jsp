<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%
    Long bankId = Long.valueOf(request.getParameter("bankId"));
    RemoteCallHelper.getInstance().invalidateWalletManager(bankId);
%>