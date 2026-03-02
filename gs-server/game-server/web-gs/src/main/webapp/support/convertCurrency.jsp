<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.gs.managers.payment.currency.CurrencyRatesManager" %>
<%
    String sourceCurrency = request.getParameter("source");
    if (StringUtils.isTrimmedEmpty(sourceCurrency)) {
        response.getWriter().println("source currency not found");
        return;
    }
    String destinationCurrency = request.getParameter("destination");
    if (StringUtils.isTrimmedEmpty(destinationCurrency)) {
        response.getWriter().println("destination currency not found");
        return;
    }
    Double value;
    try {
        value = Double.parseDouble(request.getParameter("value"));
    } catch (Exception e) {
        response.getWriter().println("Cannot parse value");
        return;
    }
    try {
        double converted = CurrencyRatesManager.getInstance().convert(value, sourceCurrency, destinationCurrency);
        response.getWriter().println(String.valueOf(converted));
    } catch (Exception e) {
        ThreadLog.error("Currency conversion error, value=" + value +
                ", sourceCurrency=" + sourceCurrency +
                ", destinationCurrency=" + destinationCurrency, e);
        response.getWriter().println("Conversion error: " + e.getMessage());
    }
%>