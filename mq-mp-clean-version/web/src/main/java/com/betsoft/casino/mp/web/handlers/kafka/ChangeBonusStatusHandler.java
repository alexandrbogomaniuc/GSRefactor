package com.betsoft.casino.mp.web.handlers.kafka;

import com.dgphoenix.casino.kafka.dto.BonusStatusDto;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.stereotype.Component;


@Component
public class ChangeBonusStatusHandler implements KafkaOuterRequestHandler<BonusStatusDto, VoidKafkaResponse> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public ChangeBonusStatusHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(BonusStatusDto request) {
        serviceHandler.sendBonusStatus(request);
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<BonusStatusDto> getRequestClass() {
        return BonusStatusDto.class;
    }
}
