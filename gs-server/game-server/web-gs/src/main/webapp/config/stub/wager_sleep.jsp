<%@ page import="com.abs.casino.common.exception.NotEnoughMoneyException" %>
<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.CCommonWallet" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.RemoteClientStubHelper" %>
<%@ page import="com.abs.casino.gs.managers.payment.wallet.RemoteClientStubHelper.ExtAccountInfoStub" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String userId = "";
    String gameId = "";
    String roundId = "";
    String strBet1 = "";
    String strBet2 = "";
    String strWin1 = "";
    String strWin2 = "";
    String cookieString = "";
    long sleep = 0;
    long now = System.currentTimeMillis();
    try {
        userId = request.getParameter(CCommonWallet.PARAM_USERID);
        gameId = request.getParameter(CCommonWallet.PARAM_GAMEID);
        if (gameId == null) gameId = "12";
        roundId = request.getParameter(CCommonWallet.PARAM_ROUNDID);
        if (roundId == null) roundId = "12345";

        if ("betError".equals(userId)) {
            response.getWriter().print("<EXTSYSTEM>\n" +
                    "    <REQUEST>\n" +
                    "        <USERID>12345</USERID>\n" +
                    "        <BET>1234.07|12344546</BET>\n" +
                    "        <WIN>1222.17|12344547</WIN>\n" +
                    "        <ROUNDID>12345</ROUNDID>\n" +
                    "        <GAMEID>12</GAMEID>\n" +
                    "    </REQUEST>\n" +
                    "    <TIME>12 Jan 2004 15:15:15</TIME>\n" +
                    "    <RESPONSE>\n" +
                    "        <RESULT>FAILED</RESULT>\n" +
                    "        <CODE>300</CODE>\n" +
                    "    </RESPONSE>\n" +
                    "</EXTSYSTEM>");
            return;
        }

        String strBet = request.getParameter(CCommonWallet.PARAM_BET);
        int delimIndex = strBet == null ? 0 : strBet.indexOf("|");
        strBet1 = strBet == null ? "" : strBet.substring(0, delimIndex);
        strBet2 = strBet == null ? "" : strBet.substring(delimIndex + 1);
        long bet;
        try {
            bet = StringUtils.isTrimmedEmpty(strBet1) ? 0 : Long.parseLong(strBet1);
        } catch (NumberFormatException e) {
            bet = (long) (Double.parseDouble(strBet1) * 100);
        }
        long betId = StringUtils.isTrimmedEmpty(strBet2) ? 0 : Long.parseLong(strBet2);

        String strWin = request.getParameter(CCommonWallet.PARAM_WIN);
        delimIndex = strWin == null ? 0 : strWin.indexOf("|");
        strWin1 = strWin == null ? "" : strWin.substring(0, delimIndex);
        strWin2 = strWin == null ? "" : strWin.substring(delimIndex + 1);

        long win;
        try {
            win = StringUtils.isTrimmedEmpty(strWin1) ? 0 : Long.parseLong(strWin1);
        } catch (NumberFormatException e) {
            win = (long) (Double.parseDouble(strWin1) * 100);
        }
        long winId = StringUtils.isTrimmedEmpty(strWin2) ? 0 : Long.parseLong(strWin2);

        String strNegativeBet = request.getParameter(CCommonWallet.PARAM_NEGATIVE_BET);
        String strPromoWinAmount = request.getParameter(CCommonWallet.PROMO_WIN_AMOUNT);
        String promoId = request.getParameter(CCommonWallet.PROMO_ID);
        String promoCampaignType = request.getParameter(CCommonWallet.PROMO_CAMPAIGN_TYPE);
        long negativeBet = StringUtils.isTrimmedEmpty(strNegativeBet) ? 0 : Long.parseLong(strNegativeBet);
        long promoWin = StringUtils.isTrimmedEmpty(strPromoWinAmount) ? 0 : Long.parseLong(strPromoWinAmount);
        if (bet > 0) {
            //if(true) throw new Exception("Not error: Failed bet test");
            RemoteClientStubHelper.getInstance().makeRecordedBet(userId, bet, betId);
        }
        if ((win > 0) || (negativeBet > 0) || (promoWin > 0)) {
            //if(true) throw new Exception("Not error: Failed win test");
            if (userId.contains("sleep")) {
                String pauseStr = userId.split("_")[2];
                sleep = StringUtils.isTrimmedEmpty(pauseStr) ? 0 : Long.parseLong(pauseStr) * 1000;
            }
            RemoteClientStubHelper.getInstance().makeRecordedWin(userId, win + negativeBet + promoWin, winId);
        }

        String unjContr = request.getParameter("unjContribution");
        //delimIndex = strUnjContr == null ? 0 : strUnjContr.indexOf("|");
        //String unjContr = strUnjContr == null ? "" : strWin.substring(delimIndex + 1);
        //double unjContrAmount = StringUtils.isTrimmedEmpty(unjContr) ? 0 : Double.parseDouble(unjContr);
        if (!StringUtils.isTrimmedEmpty(unjContr)) {
            RemoteClientStubHelper.getInstance().makeRecordedUnjContr(userId, unjContr, betId + 1000000000);
        }

        cookieString = "no";
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

        if (!StringUtils.isTrimmedEmpty(strNegativeBet)) {
            addParams.append("<NEGATIVEBET>").append(strNegativeBet).append("</NEGATIVEBET>\n");
        }

        String isRoundFinished = request.getParameter(CCommonWallet.PARAM_ROUND_FINISHED);
        if (!StringUtils.isTrimmedEmpty(isRoundFinished)) {
            addParams.append("<ISROUNDFINISHED>").append(isRoundFinished).append("</ISROUNDFINISHED>\n");
        }

        String add_str = "";
        if (request.getParameter("BONUS_WIN") != null) {
            add_str = "<BONUSWIN>" + request.getParameter("BONUS_WIN") + "</BONUSWIN>";
        } else if (request.getParameter("BONUS_BET") != null) {
            add_str = "<BONUSBET>" + request.getParameter("BONUS_BET") + "</BONUSBET>";
        }
        if (!StringUtils.isTrimmedEmpty(strPromoWinAmount)) {
            addParams.append("<PROMOWINAMOUNT>").append(strPromoWinAmount).append("</PROMOWINAMOUNT>");
            addParams.append("<PROMOID>").append(promoId).append("</PROMOID>");
            addParams.append("<PROMOCAMPAIGNTYPE>").append(promoCampaignType).append("</PROMOCAMPAIGNTYPE>\n");
        }
        ExtAccountInfoStub extAccountInfo = RemoteClientStubHelper.getInstance().getExtAccountInfo(userId);
        long resultBalance = extAccountInfo.getBalance();
        RemoteCallHelper.getInstance().updateStubBalance(userId, resultBalance);

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
                "<TIME>" + new Date() + "</TIME>\n" +
                "<RESPONSE>\n" +
                "<RESULT>OK</RESULT>" + add_str + "\n" +
                "<EXTSYSTEMTRANSACTIONID>" + userId + "_" + roundId + "_" + resultBalance + "</EXTSYSTEMTRANSACTIONID>\n" +
                "<BALANCE>" + resultBalance + "</BALANCE>\n" +
                "</RESPONSE>\n" +
                "</EXTSYSTEM>");

        ThreadLog.debug("wager.jsp: bet:" + bet + " win: " + win + " userId=" + request.getParameter("userId"));
    } catch (NotEnoughMoneyException e) {
        response.getWriter().print("<EXTSYSTEM>\n" +
                "<REQUEST>\n" +
                "<USERID>" + userId + "</USERID>\n" +
                "<BET>" + strBet1 + "|" + strBet2 + "</BET>\n" +
                "<WIN>" + strWin1 + "|" + strWin2 + "</WIN>\n" +
                "<ROUNDID>" + roundId + "</ROUNDID>\n" +
                "<GAMEID>" + gameId + "</GAMEID>\n" +
                "<COOKIES>" + cookieString + "</COOKIES>\n" +
                "</REQUEST>\n" +
                "<TIME>" + new Date() + "</TIME>\n" +
                "<RESPONSE>\n" +
                "<RESULT>FAILED</RESULT>" + "\n" +
                "<CODE>300</CODE>" + "\n" +
                "</RESPONSE>\n" +
                "</EXTSYSTEM>");
        ThreadLog.error("wager.jsp error", e);
    } catch (Throwable e) {
        ThreadLog.error("wager.jsp error", e);
    }
    response.flushBuffer();
    long runTime = System.currentTimeMillis() - now;
    if (sleep - runTime > 0) {
        Thread.sleep(sleep - runTime);
    }
%>