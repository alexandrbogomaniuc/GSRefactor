package com.betsoft.casino.mp.web.service;

import com.betsoft.casino.mp.service.CurrencyRateService;
import com.dgphoenix.casino.common.currency.CurrencyRate;
import com.hazelcast.spring.context.SpringAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import java.io.Serializable;
import java.util.Set;

@SpringAware
public class UpdateCurrencyTask implements Runnable, Serializable, ApplicationContextAware {
    private Set<CurrencyRate> unknownCurrenciesRates;

    private transient ApplicationContext context;
    private static final Logger LOG = LogManager.getLogger(UpdateCurrencyTask.class);

    public UpdateCurrencyTask(Set<CurrencyRate> unknownCurrencies) {
        this.unknownCurrenciesRates = unknownCurrencies;
    }

    @Override
    public void run() {
        if (context != null) {
            LOG.debug("UpdateCurrencyTask unknownCurrencies " + unknownCurrenciesRates);
            if (!unknownCurrenciesRates.isEmpty()) {
                CurrencyRateService currencyRateService = context.getBean("currencyRateService", CurrencyRateService.class);
                currencyRateService.updateCurrencyToCache(unknownCurrenciesRates);
            }
        } else {
            LOG.error("ApplicationContext not found");
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpdateCurrencyTask [");
        sb.append("unknownCurrencies=").append(unknownCurrenciesRates);
        sb.append(']');
        return sb.toString();
    }
}
