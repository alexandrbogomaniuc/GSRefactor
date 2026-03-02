<%@ page import="com.abs.casino.cassandra.persist.CassandraCurrencyRatesPersister" %>
<%@ page import="com.abs.casino.common.currency.CurrencyRate" %>
<%@ page import="java.util.Collection" %>
<%@ page import="com.abs.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.abs.casino.common.util.ApplicationContextHelper" %>
<%
    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraCurrencyRatesPersister persister = persistenceManager.getPersister(CassandraCurrencyRatesPersister.class);
    Collection<CurrencyRate> rates = persister.getRates();
    for (CurrencyRate rate : rates) {
        response.getWriter().write(rate.getSourceCurrency() + "/" + rate.getDestinationCurrency() +
                "=" + String.format("%1.8f", rate.getRate()) + "</br>");
    }
%>