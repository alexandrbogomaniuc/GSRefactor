<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.CCommonWallet" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.RemoteClientStubHelper" %>
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
        long bet = StringUtils.isTrimmedEmpty(strBet1) ? 0 : (long) (Double.parseDouble(strBet1) * 100);

        String strWin = request.getParameter(CCommonWallet.PARAM_WIN);
        delimIndex = strWin == null ? 0 : strWin.indexOf("|");
        String strWin1 = strWin == null ? "" : strWin.substring(0, delimIndex);
        String strWin2 = strWin == null ? "" : strWin.substring(delimIndex + 1);
        long win = StringUtils.isTrimmedEmpty(strWin1) ? 0 : (long) (Double.parseDouble(strWin1) * 100);


        String cookieString = "no";
        Cookie cookies[] = request.getCookies();
        if (null != cookies) {
            cookieString = "";
            for (int i = 0; i < cookies.length; ++i) {
                cookieString += cookies[i].getName() + "=" + cookies[i].getValue() + ";";
            }
        }

        RemoteClientStubHelper.ExtAccountInfoStub extAccountInfo =
                RemoteClientStubHelper.getInstance().getExtAccountInfo(userId);

        long balance = 0;
        if (bet > 0) {
            balance = 10000;
        }

        if (win > 0) {
            balance = 50000;
        }

        response.getWriter().print("<EXTSYSTEM>\n" +
                "<REQUEST>\n" +
                "<USERID>" + userId + "</USERID>\n" +
                "<BET>" + strBet1 + "|" + strBet2 + "</BET>\n" +
                "<WIN>" + strWin1 + "|" + strWin2 + "</WIN>\n" +
                "<ROUNDID>" + roundId + "</ROUNDID>\n" +
                "<GAMEID>" + gameId + "</GAMEID>\n" +
                "<COOKIES>" + cookieString + "</COOKIES>\n" +
                "</REQUEST>\n" +
                "<TIME>" + new Date().toString() + "</TIME>\n" +
                "<RESPONSE>\n" +
                "<RESULT>OK</RESULT>" + "\n" +
                "<EXTSYSTEMTRANSACTIONID>" + userId + "_" + roundId + "_" + balance + "</EXTSYSTEMTRANSACTIONID>\n" +
                "<BALANCE>" + balance + "</BALANCE>\n" +
                "</RESPONSE>\n" +
                "</EXTSYSTEM>");

    } catch (Throwable e) {
        ThreadLog.error("wager_if_win_error.jsp error", e);
    }
    response.flushBuffer();
%>