<%@ page import="com.dgphoenix.casino.gs.managers.payment.wallet.CCommonWallet" %>
<%@ page import="java.util.Date" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String hash = request.getParameter("hash");
    String userId = request.getParameter(CCommonWallet.PARAM_USERID);
    String casinoTransactionId = request.getParameter("casinoTransactionId");

    response.getWriter().print("\n<EXTSYSTEM>\n" +
            "    <REQUEST>\n" +
            "        <HASH>" + hash + "</HASH>\n" +
            "        <USERID>" + userId + "</USERID>\n" +
            "        <CASINOTRANSACTIONID>" + casinoTransactionId + "</CASINOTRANSACTIONID>\n" +
            "    </REQUEST>\n" +
            "    <TIME>" + new Date().toString() + "</TIME>\n" +
            "    <RESPONSE>\n" +
            "        <CODE>310</CODE>\n" +
            "        <RESULT>FAILED</RESULT>\n" +
            "    </RESPONSE>\n" +
            "</EXTSYSTEM>");
%>