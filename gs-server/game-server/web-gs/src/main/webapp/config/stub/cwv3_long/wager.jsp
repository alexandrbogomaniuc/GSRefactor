<%@ page import="com.abs.casino.common.util.DigitFormatter" %>
<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.gs.GameServer" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.RemoteClientStubHelper" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.CCommonWallet" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    try {
        String userId = request.getParameter(CCommonWallet.PARAM_USERID);

        String strBet = request.getParameter(CCommonWallet.PARAM_BET);
        int delimIndex = strBet == null ? 0 : strBet.indexOf("|");
        String strBet1 = strBet == null ? "" : strBet.substring(0, delimIndex);
        String strBet2 = strBet == null ? "" : strBet.substring(delimIndex + 1);
        //ThreadLog.debug("delimIndex=" + delimIndex + ", strBet1=" + strBet1 + ", strBet2=" + strBet2);
        long bet = StringUtils.isTrimmedEmpty(strBet1) ? 0 : Long.valueOf(strBet1);

        String strWin = request.getParameter(CCommonWallet.PARAM_WIN);
        delimIndex = strWin == null ? 0 : strWin.indexOf("|");
        String strWin1 = strWin == null ? "" : strWin.substring(0, delimIndex);
        String strWin2 = strWin == null ? "" : strWin.substring(delimIndex + 1);

        long win = StringUtils.isTrimmedEmpty(strWin1) ? 0 : Long.valueOf(strWin1);
        if (bet > 0) {
            RemoteClientStubHelper.getInstance().makeBet(userId, bet);
            //if(true) throw new java.lang.Exception("Fake!");
        }
        if (win > 0) {
            RemoteClientStubHelper.getInstance().makeWin(userId, win);
        }

        String cookieString = "no";
        Cookie cookies[] = request.getCookies();
        if (null != cookies) {
            cookieString = "";
            for (int i = 0; i < cookies.length; ++i) {
                cookieString += cookies[i].getName() + "=" + cookies[i].getValue() + ";";
            }
        }
        String sPromoWinAmount = request.getParameter("promoWinAmount");
        String promoId = request.getParameter("promoId");
        String promoCampaignType = request.getParameter("promoCampaignType");
        StringBuilder addParams = new StringBuilder();
        if (!StringUtils.isTrimmedEmpty(sPromoWinAmount)) {
            addParams.append("<PROMOWINAMOUNT>").append(sPromoWinAmount).append("</PROMOWINAMOUNT>");
            addParams.append("<PROMOID>").append(promoId).append("</PROMOID>");
            addParams.append("<PROMOCAMPAIGNTYPE>").append(promoCampaignType).append("</PROMOCAMPAIGNTYPE>\n");
            RemoteClientStubHelper.getInstance().makeWin(userId, Long.parseLong(sPromoWinAmount));
        }
        RemoteClientStubHelper.ExtAccountInfoStub extAccountInfo =
                RemoteClientStubHelper.getInstance().getExtAccountInfo(userId);
        long resultBalance = extAccountInfo.getBalance();
        RemoteCallHelper.getInstance().updateStubBalance(userId, resultBalance);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy hh:ss:mm");
        response.getWriter().print("<EXTSYSTEM>\n" +
                "<REQUEST>\n" +
                "<USERID>" + userId + "</USERID>\n" +
                "<BET>" + bet + "|" + strBet2 + "</BET>\n" +
                "<WIN>" + win + "|" + strWin2 + "</WIN>\n" +
                "<ROUNDID>12345</ROUNDID>\n" +
                "<GAMEID>12</GAMEID>\n" +
                "<COOKIES>" + cookieString + "</COOKIES>\n" +
                addParams +
                "</REQUEST>\n" +
                "<TIME>" + dateFormatter.format(new Date()) + "</TIME>\n" +
                "<RESPONSE>\n" +
                "<RESULT>OK</RESULT>\n" +
                "<EXTSYSTEMTRANSACTIONID>154456456</EXTSYSTEMTRANSACTIONID>\n" +
                "<BALANCE>" + resultBalance + "</BALANCE>\n" +
                "<GS>" + GameServer.getInstance().getServerId() + "</GS>\n" +
                "</RESPONSE>\n" +
                "</EXTSYSTEM>");
    } catch (Throwable e) {
        ThreadLog.error("wager.jsp error", e);
    }
    response.flushBuffer();
%>