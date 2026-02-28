package com.abs.casino.kafka.dto;

import com.abs.casino.kafka.dto.CurrencyRateDto;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

import java.util.Set;

public class UpdateCurrencyRatesRequestResponse extends BasicKafkaResponse implements KafkaRequest {
    private Set<CurrencyRateDto> rates;

    public UpdateCurrencyRatesRequestResponse(Set<CurrencyRateDto> rates) {
        super(true, 0, "");
        this.rates = rates;
    }

    public UpdateCurrencyRatesRequestResponse(boolean success, int statusCode, String reasonPhrases) {
        super(success, statusCode, reasonPhrases);
    }

    public Set<CurrencyRateDto> getRates() {
        return rates;
    }

    public void setRates(Set<CurrencyRateDto> rates) {
        this.rates = rates;
    }
}
