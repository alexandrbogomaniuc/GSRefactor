<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.CCommonWallet" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.RemoteClientStubHelper" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
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
    StringBuilder addParams = new StringBuilder();
    String hash = request.getParameter("hash");
    if (!StringUtils.isTrimmedEmpty(hash)) {
        addParams.append("<HASH>").append(hash).append("</HASH>\n");
    }

    String negativeBet = request.getParameter(CCommonWallet.PARAM_NEGATIVE_BET);
    long negativeBetValue = 0;
    if (!StringUtils.isTrimmedEmpty(negativeBet)) {
        addParams.append("<NEGATIVEBET>").append(negativeBet).append("</NEGATIVEBET>\n");
        negativeBetValue = Long.parseLong(negativeBet);
    }

    String isRoundFinished = request.getParameter(CCommonWallet.PARAM_ROUND_FINISHED);
    if (!StringUtils.isTrimmedEmpty(isRoundFinished)) {
        addParams.append("<ISROUNDFINISHED>").append(isRoundFinished).append("</ISROUNDFINISHED>\n");
    }

    RemoteClientStubHelper.ExtAccountInfoStub extAccountInfo =
            RemoteClientStubHelper.getInstance().getExtAccountInfo(userId);

    if (negativeBetValue > 0) {
        //throw new Exception("Not error: Failed negative bet test");
        //RemoteClientStubHelper.getInstance().makeBet(userId, bet);
        response.getWriter().print("<EXTSYSTEM>\n" +
                "<REQUEST>\n" +
                "<USERID>" + userId + "</USERID>\n" +
                "<BET>" + strBet1 + "|" + strBet2 + "</BET>\n" +
                "<WIN>" + strWin1 + "|" + strWin2 + "</WIN>\n" +
                "<ROUNDID>" + roundId + "</ROUNDID>\n" +
                "<GAMEID>" + gameId + "</GAMEID>\n" +
                addParams +
                "<COOKIES>" + cookieString + "</COOKIES>\n" +
                "</REQUEST>\n" +
                "<TIME>" + new Date().toString() + "</TIME>\n" +
                "<RESPONSE>\n" +
                "<RESULT>FAILED</RESULT>" + "\n" +
                "<CODE>301</CODE>" + "\n" +
                "</RESPONSE>\n" +
                "</EXTSYSTEM>");
    } else {
        if (win > 0) {
            RemoteClientStubHelper.getInstance().makeWin(userId, win);
        }
        if (bet > 0) {
            RemoteClientStubHelper.getInstance().makeBet(userId, bet);
        }
        String add_str = "";
        if (request.getParameter("BONUS_WIN") != null) {
            add_str = "<BONUSWIN>" + request.getParameter("BONUS_WIN") + "</BONUSWIN>";
        } else if (request.getParameter("BONUS_BET") != null) {
            add_str = "<BONUSBET>" + request.getParameter("BONUS_BET") + "</BONUSBET>";
        }

        response.getWriter().print("<EXTSYSTEM>\n" +
                "<REQUEST>\n" +
                "<USERID>" + userId + "</USERID>\n" +
                "<BET>" + strBet1 + "|" + strBet2 + "</BET>\n" +
                "<WIN>" + strWin1 + "|" + strWin2 + "</WIN>\n" +
                "<ROUNDID>" + roundId + "</ROUNDID>\n" +
                "<GAMEID>" + gameId + "</GAMEID>\n" +
                addParams +
                "<COOKIES>" + cookieString + "</COOKIES>\n" +
                "</REQUEST>\n" +
                "<TIME>" + new Date().toString() + "</TIME>\n" +
                "<RESPONSE>\n" +
                "<RESULT>OK</RESULT>" + add_str + "\n" +
                "<EXTSYSTEMTRANSACTIONID>" + userId + "_" + roundId + "_" + (extAccountInfo.getBalance() / 100) + "</EXTSYSTEMTRANSACTIONID>\n" +
                "<BALANCE>" + (extAccountInfo.getBalance() / 100) + "</BALANCE>\n" +
                "</RESPONSE>\n" +
                "</EXTSYSTEM>");
    }
%>