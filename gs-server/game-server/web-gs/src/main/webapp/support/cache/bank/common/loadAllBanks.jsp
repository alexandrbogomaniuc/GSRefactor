<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.system.configuration.PlayerSessionConfiguration" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RefreshConfigCall" %>
<%--
  Created by IntelliJ IDEA.
  User: vik
  Date: 24.11.11
  Time: 10:42
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head><title>Loading Banks from SessionManagerConfiguration.properties</title></head>
<body>
<%
    PlayerSessionConfiguration psc = PlayerSessionConfiguration.getInstance();
    for (Long bankId : BankInfoCache.getInstance().getBankIds()) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo != null) {
            //if (StringUtils.isTrimmedEmpty(psc.getPSMClass(bankId)))
            bankInfo.setProperty(BankInfo.KEY_PSM_CLASS, psc.getPSMClass(bankId));
%>               <%="bankInfo=" + bankInfo.getPSMClass() + "      PlayerSessionConf=" + psc.getPSMClass(bankId) %>
<%
            RemoteCallHelper.getInstance().sendCallToAllServers(new RefreshConfigCall(
                    BankInfoCache.class.getCanonicalName(), String.valueOf(bankInfo.getId())));
        }
    }
%>
Ok
</body>
</html>
