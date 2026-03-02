<%@ page import="com.abs.casino.common.cache.LoadBalancerCache" %>
<%@ page import="com.abs.casino.common.cache.data.server.ServerInfo" %>
<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="java.util.Collection" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    Collection<ServerInfo> values = LoadBalancerCache.getInstance().getAllObjects().values();
    for (ServerInfo serverInfo : values) {
        serverInfo.setMaxLoad(4000);
    }
    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(121L);
    bankInfo.setProperty(BankInfo.KEY_GAMESESSIONS_LIMIT, "60000");
    bankInfo.setProperty(BankInfo.KEY_PLAYERSESSIONS_LIMIT, "600000");

    response.getWriter().print("OK" + "\n\n<br>");
    response.flushBuffer();
%>
