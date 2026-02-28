<%@ page import="java.util.*" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="com.abs.casino.common.util.StreamUtils" %>
<%@ page import="com.google.common.base.Splitter" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page import="com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%

    Splitter splitter = Splitter.on("|").trimResults().omitEmptyStrings();
    List<Long> bankIds = null;
    String value = request.getParameter("bankId");
    if (value != null) {
        bankIds = StreamUtils.asStream(splitter.split(value))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    for (long bankId : bankIds) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo == null) continue;
        Map<Long, IBaseGameInfo> allGameInfosAsMap;

        if (bankInfo.getMasterBankId() != null) {
            BankInfo masterBank = BankInfoCache.getInstance().getBankInfo(bankInfo.getMasterBankId());
            allGameInfosAsMap = BaseGameCache.getInstance()
                    .getAllGameInfosAsMap(masterBank.getId(), masterBank.getDefaultCurrency());
        } else {
            allGameInfosAsMap = BaseGameCache.getInstance()
                    .getAllGameInfosAsMap(bankInfo.getId(), bankInfo.getDefaultCurrency());
        }

        try {
            RemoteCallHelper.getInstance().saveAndSendNotification(
                    allGameInfosAsMap.entrySet().iterator().next().getValue());
        } catch (CommonException e) {
            e.printStackTrace(response.getWriter());
        }
    }

%>