<%@ page import="com.abs.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="java.util.Set" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%@ page import="com.abs.casino.common.cache.ExternalGameIdsCache" %>
<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%

    String bankString = request.getParameter("bank");
    int bank = Integer.parseInt(bankString);

    Set<String> gamesKeySet = ExternalGameIdsCache.getInstance().getAllObjects().keySet();
    for (String key : gamesKeySet) {
        String[] split = key.split("\\+");
        if (split[1].equals(bankString)) {
            Long originalId = ExternalGameIdsCache.getInstance().getOriginalId(split[0], bank);
            BaseGameInfoTemplate baseGameInfoTemplateById = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(
                    originalId);
            response.getWriter().write("bank: " + bank + " bank name: " + BankInfoCache.getInstance().getBankInfo(bank).
                    getExternalBankIdDescription() + " , title: " + baseGameInfoTemplateById.getTitle() +
                    ",   originalId:" + originalId + ",  externalGameId:  " + split[0] + "<br>");
        }
    }

%>