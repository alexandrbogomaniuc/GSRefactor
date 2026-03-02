<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%
  long bankId = Long.parseLong(request.getParameter("bankId"));
  BankInfoCache.getInstance().invalidateCurrencyRateMultipliers(bankId);
%>
