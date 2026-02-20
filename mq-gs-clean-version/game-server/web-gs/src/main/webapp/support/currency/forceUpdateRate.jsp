<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraCurrencyRatesPersister" %>
<%@ page import="com.dgphoenix.casino.common.currency.CurrencyRate" %>
<%@ page import="com.dgphoenix.casino.common.currency.ICurrencyRateManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.dgphoenix.casino.tracker.CurrencyUpdateProcessor" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="java.io.PrintWriter" %>
<%
    PrintWriter writer = response.getWriter();
    String currency = request.getParameter("currency");
    ApplicationContext context = ApplicationContextHelper.getApplicationContext();
    CassandraPersistenceManager persistenceManager = context.getBean(CassandraPersistenceManager.class);
    CassandraCurrencyRatesPersister persister = persistenceManager.getPersister(CassandraCurrencyRatesPersister.class);
    CurrencyRate currencyRate = persister.getCurrencyRate(currency, ICurrencyRateManager.DEFAULT_CURRENCY);
    if (currencyRate != null) {
        currencyRate.setUpdateDate(0);
        persister.createOrUpdate(currencyRate);
        CurrencyUpdateProcessor updateProcessor = context.getBean(CurrencyUpdateProcessor.class);
        updateProcessor.updateRates();
        writer.println("Rate for " + currency + " successfully updated.");
    } else {
        writer.println("Unknown rate pair source: " + currency + " target: " + ICurrencyRateManager.DEFAULT_CURRENCY);
    }
%>
