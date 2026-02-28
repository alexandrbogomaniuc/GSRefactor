<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.common.cache.data.bank.PlayerGameSettingsType" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfo" %>
<%@ page import="java.util.Collection" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameConstants" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    StringBuilder sb = new StringBuilder();
    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(121);
    //bankInfo.setProperty(BankInfo.KEY_REPLACE_START_GS_FROM, "lobby");
    //bankInfo.setProperty(BankInfo.KEY_REPLACE_START_GS_FROM, "games");
    //bankInfo.setProperty(BankInfo.KEY_REPLACE_START_GS_TO, "gss");
    //bankInfo.setPgsType(PlayerGameSettingsType.ACCOUNT);
/*
    Currency pmc = CurrencyCache.getInstance().get("PMC");
    List<SPGJackPot> allJackPots = JackPotCache.getInstance().getAllJackPots();
    for (SPGJackPot jackPot : allJackPots) {
            //JackPotCache.getInstance().validateJackpot(jackPot.getGameId(), 210, pmc);
            BaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(121, jackPot.getGameId(), pmc);
            List<Coin> coins = gameInfo.getCoins();
            for (Coin coin : coins) {
                ProgressiveJackPotManager.getInstance().createBetJackpot(jackPot, pmc, gameInfo.getJackpotMultiplier(), coin);
            }
        response.getWriter().print("jackPot.getGameId()=" + jackPot.getGameId() + "\n\n<br>");
    }
*/
    Collection<IBaseGameInfo> games = BaseGameCache.getInstance().getAllObjects().values();
    for (IBaseGameInfo game : games) {
        game.setProperty(BaseGameConstants.KEY_CDN_URL, "87.233.24.12");
        RemoteCallHelper.getInstance().saveAndSendNotification(game);
    }
    response.getWriter().print("OK" + "\n\n<br>");
%>
