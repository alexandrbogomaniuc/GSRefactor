<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.abs.casino.common.cache.CurrencyCache" %>
<%@ page import="com.abs.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.abs.casino.gs.managers.payment.currency.CurrencyRatesManager" %>
<%@ page import="com.abs.casino.common.exception.CommonException" %>
<%@ page import="java.util.List" %>
<%@ page import="com.fasterxml.jackson.databind.ObjectMapper" %>
<%@ page import="com.fasterxml.jackson.annotation.JsonInclude" %>
<%@ page import="static org.apache.http.entity.ContentType.APPLICATION_JSON" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collection" %>
<%@ page import="com.abs.casino.common.util.ApplicationContextHelper" %>

<%!
    static class ConvertedCurrency {

        private String sourceCurrency;
        private String destinationCurrency;
        private double rate;
        private String error;

        public ConvertedCurrency(String sourceCurrency, String destinationCurrency, double rate) {
            this.sourceCurrency = sourceCurrency;
            this.destinationCurrency = destinationCurrency;
            this.rate = rate;
        }

        public ConvertedCurrency(String sourceCurrency, String destinationCurrency, String error) {
            this.sourceCurrency = sourceCurrency;
            this.destinationCurrency = destinationCurrency;
            this.error = error;
        }

        public String getSourceCurrency() {
            return sourceCurrency;
        }

        public String getDestinationCurrency() {
            return destinationCurrency;
        }

        public double getRate() {
            return rate;
        }

        public String getError() {
            return error;
        }
    }
%>
<%
    List<ConvertedCurrency> currencyRateList = new ArrayList<>();
    CurrencyRatesManager manager = ApplicationContextHelper.getBean(CurrencyRatesManager.class);
    for (Currency currency : (Collection<Currency>) CurrencyCache.getInstance().getAllObjects().values()) {
        ConvertedCurrency convertedCurrency;
        String sourceCurrency = currency.getCode();
        try {
            double value = manager.convert(1.0, sourceCurrency, "EUR");
            convertedCurrency = new ConvertedCurrency(sourceCurrency, "EUR", value);
        } catch (CommonException e) {
            String error = e.getMessage();
            convertedCurrency = new ConvertedCurrency(sourceCurrency, "EUR", error);
        }
        currencyRateList.add(convertedCurrency);
    }
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    response.setContentType(APPLICATION_JSON.toString());
    response.getWriter().write(mapper.writeValueAsString(currencyRateList));
%>