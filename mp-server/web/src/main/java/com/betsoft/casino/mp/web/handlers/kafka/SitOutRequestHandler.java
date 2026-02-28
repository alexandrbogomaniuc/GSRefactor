package com.betsoft.casino.mp.web.handlers.kafka;

import org.springframework.stereotype.Component;

import com.abs.casino.kafka.dto.SitOutRequest2;
import com.abs.casino.kafka.dto.VoidKafkaResponse;
import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;


@Component
public class SitOutRequestHandler implements KafkaOuterRequestHandler<SitOutRequest2, VoidKafkaResponse> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public SitOutRequestHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(SitOutRequest2 request) {
        serviceHandler.sitOut(request);
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<SitOutRequest2> getRequestClass() {
        return SitOutRequest2.class;
    }
}
