<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RefreshConfigCall" %>
<%@ page import="com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>Remove bank page</title></head>
<body>

<%
    long scId = Long.parseLong(request.getParameter("scId"));
    long bankId = Long.parseLong(request.getParameter("bankId"));
    BankInfo bank = BankInfoCache.getInstance().getBankInfo(bankId);
    for (Currency currency : bank.getCurrencies()) {
        for (Long gameId : BaseGameCache.getInstance().getAllGamesSet(bankId, currency)) {
            BaseGameCache.getInstance().remove(bankId, gameId, currency);
        }
    }

    BankInfoCache.getInstance().remove(bankId);
    SubCasinoCache.getInstance().remove(scId, bankId);
    RemoteCallHelper.getInstance().sendCallToAllServers(new RefreshConfigCall(
            BankInfoCache.class.getCanonicalName(), String.valueOf(bankId)));
%>


</body>
</html>