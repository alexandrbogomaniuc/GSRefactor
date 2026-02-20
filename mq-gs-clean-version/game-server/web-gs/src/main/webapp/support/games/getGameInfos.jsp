<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="java.util.*" %>

<%

    Map<Long, BankInfo> bankInfoMap = BankInfoCache.getInstance().getAllObjects();
    Collection<BankInfo> bankInfos = bankInfoMap.values();

    for (BankInfo bankInfo : bankInfos) {
        try {
            if (bankInfo != null) {

                response.getWriter().println("Bank:" + bankInfo);
                response.getWriter().println("</br>");

                Currency defaultCurrency = bankInfo.getDefaultCurrency();
                Map<Long, IBaseGameInfo> gameInfosAsMap =
                        BaseGameCache.getInstance().getAllGameInfosAsMap(bankInfo.getId(), defaultCurrency);

                Collection<IBaseGameInfo> gameInfos = gameInfosAsMap.values();


                for (IBaseGameInfo gameInfo : gameInfos) {
                    response.getWriter().println("GameInfo:" + gameInfo);
                    response.getWriter().println("</br>");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

%>