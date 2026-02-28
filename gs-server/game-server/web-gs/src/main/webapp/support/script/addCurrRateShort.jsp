<%@ page import="com.abs.casino.cassandra.persist.CassandraCurrencyRatesPersister" %>
<%@ page import="com.abs.casino.common.currency.CurrencyRate" %>
<%@ page import="com.abs.casino.tracker.CurrencyUpdateProcessor" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%
    String startData = "RUB;EUR;0.0176\n" +
            "EUR;EUR;1.0\n" +
            "USD;EUR;0.9444\n" +
            "GPB;EUR;1.0\n" +
            "CNY;EUR;0.1519\n" +
            "CAD;EUR;0.7484";

    String[] records = startData.split("\n");
    long expiredDate = 0;
    ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
    CassandraPersistenceManager persistenceManager = applicationContext.getBean(CassandraPersistenceManager.class);
    CassandraCurrencyRatesPersister currencyRatesPersister = persistenceManager.getPersister(CassandraCurrencyRatesPersister.class);
    for (String record : records) {
        String[] recordParams = record.split(";");
        String from = recordParams[0];
        String to = recordParams[1];
        double rate = Double.parseDouble(recordParams[2]);
        CurrencyRate currencyRate = new CurrencyRate(from, to, rate, expiredDate);
        currencyRatesPersister.createOrUpdate(currencyRate);
    }
    CurrencyUpdateProcessor currencyUpdateProcessor = applicationContext.getBean(CurrencyUpdateProcessor.class);
    currencyUpdateProcessor.updateRates();
%>