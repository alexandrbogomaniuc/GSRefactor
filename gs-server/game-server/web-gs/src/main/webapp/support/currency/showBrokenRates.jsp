<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraCurrencyRatesPersister" %>
<%@ page import="com.abs.casino.common.currency.CurrencyRate" %>
<%@ page import="com.abs.casino.tracker.ExternalSourceCurrencyRateExtractor" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.time.Duration" %>
<%@ page import="org.springframework.http.MediaType" %>
<%@ page import="com.abs.casino.cassandra.persist.CassandraCurrencyRatesConfigPersister" %>
<%@ page import="java.util.Set" %>
<%
    ApplicationContext context = ApplicationContextHelper.getApplicationContext();
    CassandraPersistenceManager persistenceManager = context.getBean(CassandraPersistenceManager.class);
    CassandraCurrencyRatesPersister ratePersister = persistenceManager.getPersister(CassandraCurrencyRatesPersister.class);
    CassandraCurrencyRatesConfigPersister rateConfigPersister = persistenceManager.getPersister(CassandraCurrencyRatesConfigPersister.class);
    ExternalSourceCurrencyRateExtractor rateExtractor = new ExternalSourceCurrencyRateExtractor();

    long start = System.currentTimeMillis();
    Set<String> calculatedRates = rateConfigPersister.getCalculatedCurrenciesConfig().keySet();
    Map<String, String> brokenRates = new HashMap<>();
    for (CurrencyRate currencyRate : ratePersister.getRates()) {
        if (calculatedRates.contains(currencyRate.getSourceCurrency())) {
            continue;
        }
        double rate = rateExtractor.getRate(currencyRate.getSourceCurrency(), currencyRate.getDestinationCurrency());
        if (rate <= 0) {
            brokenRates.put(currencyRate.getSourceCurrency(), currencyRate.getDestinationCurrency());
        }
    }

    response.setContentType(MediaType.TEXT_PLAIN_VALUE);
    PrintWriter writer = response.getWriter();
    writer.println("Broken rates:");
    for (Map.Entry<String, String> entry : brokenRates.entrySet()) {
        writer.println(entry.getKey() + " -> " + entry.getValue());
    }
    writer.println("Total time: " + Duration.ofMillis(System.currentTimeMillis() - start));
%>