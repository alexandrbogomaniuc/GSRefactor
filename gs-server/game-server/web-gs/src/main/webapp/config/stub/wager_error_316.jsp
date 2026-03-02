<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.CCommonWallet" %>
<%@ page import="java.util.Date" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    try {
        String userId = request.getParameter(CCommonWallet.PARAM_USERID);
        String gameId = request.getParameter(CCommonWallet.PARAM_GAMEID);
        if (gameId == null) gameId = "12";
        String roundId = request.getParameter(CCommonWallet.PARAM_ROUNDID);
        if (roundId == null) roundId = "12345";

        String strBet = request.getParameter(CCommonWallet.PARAM_BET);
        int delimIndex = strBet == null ? 0 : strBet.indexOf("|");
        String strBet1 = strBet == null ? "" : strBet.substring(0, delimIndex);
        String strBet2 = strBet == null ? "" : strBet.substring(delimIndex + 1);

        String hash = request.getParameter("hash");

        response.getWriter().print("<EXTSYSTEM>\n" +
                "<REQUEST>\n" +
                "<USERID>" + userId + "</USERID>\n" +
                "<BET>" + strBet1 + "|" + strBet2 + "</BET>\n" +
                "<ISROUNDFINISHED></ISROUNDFINISHED>" +
                "<ROUNDID>" + roundId + "</ROUNDID>\n" +
                "<GAMEID>" + gameId + "</GAMEID>\n" +
                "<HASH>" + hash + "</HASH>\n" +
                "</REQUEST>\n" +
                "<TIME>" + new Date().toString() + "</TIME>\n" +
                "<RESPONSE>\n" +
                "<RESULT>ERROR</RESULT>" + "\n" +
                "<CODE>316</CODE>" + "\n" +
                "</RESPONSE>\n" +
                "</EXTSYSTEM>");

    } catch (Throwable e) {
        ThreadLog.error("wager_error_300.jsp error", e);
    }
    response.flushBuffer();
%>