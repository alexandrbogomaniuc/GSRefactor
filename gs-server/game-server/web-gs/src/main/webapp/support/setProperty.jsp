<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    List<String> whiteList = new ArrayList<>(10);

    {
        whiteList.add(BankInfo.KEY_CUSTOMER_SETTINGS_URL);
        whiteList.add(BankInfo.KEY_CUSTOMER_SETTINGS_HTML5PC);
        whiteList.add(BankInfo.KEY_ADDITIONAL_FLASHVARS);
    }
%>

<%
    response.getWriter().println("<pre>");
    String subCasinoIdStr = request.getParameter("subCasinoId");
    String bankIdStr = request.getParameter("bankId");
    if (StringUtils.isTrimmedEmpty(subCasinoIdStr) && StringUtils.isTrimmedEmpty(bankIdStr)) {
        response.getWriter().println("subCasinoId or bankId parameter not found");
        return;
    }

    String key = request.getParameter("key");
    String value = request.getParameter("value");
    if (StringUtils.isTrimmedEmpty(key) || StringUtils.isTrimmedEmpty(value)) {
        response.getWriter().println("key or value parameter not found");
        return;
    }


    if (!whiteList.contains(key)) {
        response.getWriter().println("key=" + key + " is not in whiteList: " + whiteList);
        return;
    }

    if (!StringUtils.isTrimmedEmpty(subCasinoIdStr)) {
        long subCasinoId;
        try {
            subCasinoId = Long.parseLong(subCasinoIdStr);
        } catch (NumberFormatException ex) {
            response.getWriter().println("incorrect subCasinoId " + subCasinoIdStr);
            return;
        }
        SubCasino subCasino = SubCasinoCache.getInstance().get(subCasinoId);
        if (subCasino == null) {
            response.getWriter().println("subCasino " + subCasinoId + " not found");
            return;
        }
        ThreadLog.warn("set property " + key + " : " + value + " for subCasino " + subCasinoId);
        response.getWriter().println("set property " + key + " : " + value + " for subCasino " + subCasinoId);
        for (Long bankId : subCasino.getBankIds()) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            ThreadLog.warn("set property " + key + " : " + value + " for bank " + bankId
                    + " oldValue " + bankInfo.getStringProperty(key));
            response.getWriter().println("set property " + key + " : " + value + " for bank " + bankId
                    + " oldValue " + bankInfo.getStringProperty(key));
            bankInfo.setProperty(key, value);
            RemoteCallHelper.getInstance().saveAndSendNotification(bankInfo);
        }
    } else {
        long bankId;
        try {
            bankId = Long.parseLong(bankIdStr);
        } catch (NumberFormatException ex) {
            response.getWriter().println("incorrect bankId " + bankIdStr);
            return;
        }
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo == null) {
            response.getWriter().println("bank " + bankId + " not found");
            return;
        }

        ThreadLog.warn("set property " + key + " : " + value + " for bank " + bankId
                + " oldValue " + bankInfo.getStringProperty(key));
        response.getWriter().println("set property " + key + " : " + value + " for bank " + bankId
                + " oldValue " + bankInfo.getStringProperty(key));
        bankInfo.setProperty(key, value);
        RemoteCallHelper.getInstance().saveAndSendNotification(bankInfo);
    }
    response.getWriter().println("ok");
    response.getWriter().println("</pre>");
%>

