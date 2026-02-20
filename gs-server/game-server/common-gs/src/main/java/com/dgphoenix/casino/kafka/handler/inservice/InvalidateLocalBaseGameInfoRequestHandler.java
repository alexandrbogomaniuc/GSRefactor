package com.dgphoenix.casino.kafka.handler.inservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.dgphoenix.casino.kafka.dto.InvalidateLocalBaseGameInfoRequest;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaInServiceAsyncRequestHandler;

@Component
public class InvalidateLocalBaseGameInfoRequestHandler implements KafkaInServiceAsyncRequestHandler<InvalidateLocalBaseGameInfoRequest> {

    private InServiceServiceHandler serviceHandler;

    @Autowired
    public InvalidateLocalBaseGameInfoRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(InvalidateLocalBaseGameInfoRequest request) {
        Currency currency = CurrencyCache.getInstance().get(request.getCurrencyCode());
        serviceHandler.invalidateLocalBaseGameInfo(request.getBankId(), request.getGameId(), currency);
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<InvalidateLocalBaseGameInfoRequest> getRequestClass() {
        return InvalidateLocalBaseGameInfoRequest.class;
    }

}
