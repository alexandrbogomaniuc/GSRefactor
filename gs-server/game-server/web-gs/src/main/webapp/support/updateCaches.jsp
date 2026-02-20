<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.ImmutableBaseGameInfoWrapper" %>
<%@ page import="com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>

<%!
    //Set<Long> array_games = BaseGameInfoTemplateCache.getInstance().getAllGameIds();
    long[] array_games = {794};

    private String composeGameKey(long bankId, long gameId, Currency currency) {
        String defaultKey = bankId + "+" + gameId;
        return currency == null || currency.isDefault(bankId) ? defaultKey : defaultKey + "+" + currency.getCode();
    }
%>


<%
    Set<Long> bankIds = BankInfoCache.getInstance().getBankIds();
    for (Long bankId : bankIds) {

        for (Long gameId : array_games) {
            List<Currency> currencies = BankInfoCache.getInstance().getBankInfo(bankId).getCurrencies();
            for (Currency currency : currencies) {
                BaseGameCache.getInstance().invalidate(composeGameKey(bankId, gameId, currency));
                IBaseGameInfo destGame = BaseGameCache.getInstance().getGameInfoShared(bankId, gameId, currency);

                if (destGame != null && !(destGame instanceof ImmutableBaseGameInfoWrapper)) {
                    BaseGameCache.getInstance().invalidate(composeGameKey(bankId, gameId, currency));
                    RemoteCallHelper.getInstance().saveAndSendNotification(destGame);
                }
            }
        }
    }
%>
