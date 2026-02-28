<%@ page import="com.dgphoenix.casino.common.cache.CurrencyCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="java.util.List" %>
<%
    long bankId = 271;
    String code = request.getParameter("code");
    String action = request.getParameter("action");
    boolean actionIsDone = false;
    if (code == null || CurrencyCache.getInstance().get(code) == null) {
        response.getWriter().write(" need parameter code=currencyCode or currency is wrong <br>");
    } else if (action == null || (!action.equals("remove") && !action.equals("add"))) {
        response.getWriter().write(" need parameter code=currencyCode. <br>");
    } else {
        Currency currency = CurrencyCache.getInstance().get(code);
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);

        if (action.equals("remove")) {
            if (bankInfo.getCurrencies().contains(currency)) {
                bankInfo.removeCurrency(currency);
                actionIsDone = true;
            } else {
                response.getWriter().write(" currency is not exists.");
            }
        } else {
            if (bankInfo.getCurrencies().contains(currency)) {
                response.getWriter().write(" currency is exists.");
            } else {
                bankInfo.addCurrency(currency);
                actionIsDone = true;
            }
        }
        try {
            RemoteCallHelper.getInstance().saveAndSendNotification(bankInfo);
        } catch (CommonException e) {
            e.printStackTrace();
            response.getWriter().write("error " + action);
        }
    }

    if (actionIsDone) {
        List<Currency> currencies = BankInfoCache.getInstance().getBankInfo(bankId).getCurrencies();
        response.getWriter().write("current currencies: <br>");
        for (Currency currency : currencies) {
            response.getWriter().write(currency.getCode() + "<br>");
        }
    }

%>