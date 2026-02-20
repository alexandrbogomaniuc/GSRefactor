<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%
  long bankId = Long.parseLong(request.getParameter("bankId"));
  BankInfoCache.getInstance().invalidateCurrencyRateMultipliers(bankId);
%>
