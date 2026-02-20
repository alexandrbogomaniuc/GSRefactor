<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraCurrencyRatesConfigPersister" %>
<%@ page import="java.util.concurrent.TimeUnit" %>
<%
    ApplicationContext context = ApplicationContextHelper.getApplicationContext();
    CassandraPersistenceManager persistenceManager = context.getBean(CassandraPersistenceManager.class);
    CassandraCurrencyRatesConfigPersister rateConfigPersister = persistenceManager.getPersister(CassandraCurrencyRatesConfigPersister.class);

    rateConfigPersister.persist("MBC", "EUR", "BTC*0.001");
    rateConfigPersister.persist("UBC", "EUR", "BTC*0.000001");
    rateConfigPersister.persist("GMR", "EUR", "RUB*30");
    rateConfigPersister.persist("GRU", "EUR", "RUB*30");
    rateConfigPersister.persist("GMK", "EUR", "KZT*50");
    rateConfigPersister.persist("GKZ", "EUR", "KZT*50");
    rateConfigPersister.persist("WDW", "EUR", "KRW*1000");
    rateConfigPersister.persist("RVP", "EUR", "KRW*1000");
    rateConfigPersister.persist("RMB", "EUR", "CNY*1");
    rateConfigPersister.persist("NTD", "EUR", "TWD*1");
    rateConfigPersister.persist("IDX", "EUR", "IDR*1");
    rateConfigPersister.persist("VNX", "EUR", "VND*1");
    rateConfigPersister.persist("MCH", "EUR", "BCH*0.001");
    rateConfigPersister.persist("UCH", "EUR", "BCH*0.000001");
    rateConfigPersister.persist("MEH", "EUR", "ETH*0.001");
    rateConfigPersister.persist("UEH", "EUR", "ETH*0.000001");
    rateConfigPersister.persist("MLC", "EUR", "LTC*0.001");
    rateConfigPersister.persist("ULC", "EUR", "LTC*0.000001");
    rateConfigPersister.persist("IPS", "EUR", "USD*0.140056022");
    rateConfigPersister.persist("MZC", "EUR", "ZEC*0.001");
    rateConfigPersister.persist("UZC", "EUR", "ZEC*0.000001");

    //sample
    rateConfigPersister.persist("BTC", TimeUnit.HOURS.toMillis(2));
%>