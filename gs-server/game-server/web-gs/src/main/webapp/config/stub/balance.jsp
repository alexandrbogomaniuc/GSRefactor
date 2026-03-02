<%@ page import="com.abs.casino.gs.managers.payment.wallet.CCommonWallet" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.RemoteClientStubHelper" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String userId = request.getParameter(CCommonWallet.PARAM_USERID);
    RemoteClientStubHelper.ExtAccountInfoStub extAccountInfo =
            RemoteClientStubHelper.getInstance().getExtAccountInfo(userId);


    response.getWriter().print("<EXTSYSTEM>\n" +
            "<REQUEST>\n" +
            "<USERID>" + userId + "</USERID>\n" +
            "</REQUEST>\n" +
            "<TIME>12 Jan 2004 15:15:15</TIME>\n" +
            "<RESPONSE>\n" +
            "<RESULT>OK</RESULT>\n" +
            "<BALANCE>" + extAccountInfo.getBalance() + "</BALANCE>\n" +
            "</RESPONSE>\n" +
            "</EXTSYSTEM>");
    response.flushBuffer();
%>