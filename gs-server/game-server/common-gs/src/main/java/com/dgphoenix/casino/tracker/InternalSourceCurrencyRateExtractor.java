package com.abs.casino.tracker;

import com.abs.casino.common.currency.CurrencyRate;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.google.common.base.Splitter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InternalSourceCurrencyRateExtractor implements CurrencyRateExtractor {
    private static final Logger LOG = LogManager.getLogger(InternalSourceCurrencyRateExtractor.class);
    private static final String CURRENCY_RATES_SERVLET = "/api/currency-rates?rates=";
    private static final String RATE_NOT_FOUND_MSG = "Currency exchange rate from %s to %s not found";
    private final Splitter.MapSplitter mapSplitter = Splitter.on(";").withKeyValueSeparator(":");
    private Map<String, Double> ratesToEurMap;

    @Override
    public double getRate(String sourceCurrencyCode, String targetCurrencyCode) {
        if (!"EUR".equals(targetCurrencyCode)) {
            return -1.0;
        }

        return Optional.ofNullable(ratesToEurMap.get(sourceCurrencyCode)).orElseGet(() -> {
            LOG.warn(String.format(RATE_NOT_FOUND_MSG, sourceCurrencyCode, targetCurrencyCode));
            return -1.0;
        });
    }

    @Override
    public void prepare(Collection<CurrencyRate> currencyRates) {
        ratesToEurMap = new HashMap<>();
        if (CollectionUtils.isEmpty(currencyRates)) {
            return;
        }

        String requiredRates = currencyRates.stream()
                .filter(currencyRate -> "EUR".equalsIgnoreCase(currencyRate.getDestinationCurrency()))
                .map(CurrencyRate::getSourceCurrency)
                .collect(Collectors.joining(","));
        List<String> urlSet = getServletUrls(requiredRates);
        if (urlSet.isEmpty()) {
            LOG.error("Cannot obtain url list of currency exchange rate providers");
            return;
        }
        ratesToEurMap = urlSet.stream()
                .map(retrieveRates())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(new HashMap<>());
    }

    private List<String> getServletUrls(String requiredRates) {
        //internal source removed
        throw new RuntimeException("Internal source removed");
    }

    private Function<String, Map<String, Double>> retrieveRates() {
        return url -> {
            try {
                String response = HttpClientConnection.newInstance().doRequest(url, new HashMap<>(), false, false);
                if (response == null || response.startsWith("ERROR")) {
                    LOG.error("Unable retrieve rates. Provider (url={}) returns error response: {}", url, response);
                }
                return parseRates(response);
            } catch (Exception e) {
                LOG.error("Failed to retrieve currency exchange rates from provider, url={}", url, e);
                return null;
            }
        };
    }

    private Map<String, Double> parseRates(String response) {
        Map<String, Double> ratesMap = new HashMap<>();
        Map<String, String> ratesRawMap = mapSplitter.split(response);
        for (Map.Entry<String, String> entry : ratesRawMap.entrySet()) {
            try {
                Double rate = Double.parseDouble(entry.getValue());
                ratesMap.put(entry.getKey(), rate);
            } catch (NumberFormatException e) {
                LOG.debug("Invalid rate format: {}", entry.getValue());
            }
        }
        return ratesMap;
    }
}
