<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.google.common.base.Joiner" %>
<%@ page import="java.util.HashMap" %>

<%
    String btcFraction = request.getParameter("BTC");

    long bankId = Long.parseLong(request.getParameter("bankId"));
    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);

    PrintWriter writer = response.getWriter();
    writer.println("Temporary unavailable");
    /*if (StringUtils.isBlank(btcFraction)) {
        String currentFraction = bankInfo.getFractionCode("BTC");
        writer.println("Info: BTC=" + currentFraction);
    } else {
        if ("MBC".equals(btcFraction) || "UBC".equals(btcFraction)) {
            Map<String, String> fractionConfig = new HashMap<>(bankInfo.getFractionConfig());
            fractionConfig.put("BTC", btcFraction);
            String updatedConfig = Joiner.on(";").withKeyValueSeparator("=").join(fractionConfig);
            bankInfo.setProperty(BankInfo.KEY_CURRENCY_FRACTIONS_CONFIG, updatedConfig);
            RemoteCallHelper.getInstance().saveAndSendNotification(bankInfo);
            writer.println("Updated: BTC=" + btcFraction);
        } else {
            writer.println("Error: Wrong fraction code");
        }
    }*/
%>
