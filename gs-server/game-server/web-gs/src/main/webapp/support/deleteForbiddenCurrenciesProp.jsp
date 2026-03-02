<%@ page import="com.abs.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="com.abs.casino.common.cache.SubCasinoCache" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Delete property from BankInfo</title>
</head>
<body>
<%
    Map<Long, SubCasino> subCasinoMap = SubCasinoCache.getInstance().getAllObjects();
    for (SubCasino subCasino : subCasinoMap.values()) {
        List<Long> bankIds = subCasino.getBankIds();
        for (Long bankId : bankIds) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            bankInfo.removeProperty("AUTOPLAY_FORBIDDEN_CURRENCIES");
        }
    }
%>
</body>
</html>
