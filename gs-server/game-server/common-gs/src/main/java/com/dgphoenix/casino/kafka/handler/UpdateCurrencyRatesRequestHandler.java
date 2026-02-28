package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.UpdateCurrencyRatesRequestResponse;

@Component
public class UpdateCurrencyRatesRequestHandler implements
        KafkaOuterRequestHandler<UpdateCurrencyRatesRequestResponse, UpdateCurrencyRatesRequestResponse> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public UpdateCurrencyRatesRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public UpdateCurrencyRatesRequestResponse handle(UpdateCurrencyRatesRequestResponse request) {
        return new UpdateCurrencyRatesRequestResponse(
                mqServiceHandler.updateCurrencyRates(request.getRates()));
    }

    @Override
    public Class<UpdateCurrencyRatesRequestResponse> getRequestClass() {
        return UpdateCurrencyRatesRequestResponse.class;
    }

}
