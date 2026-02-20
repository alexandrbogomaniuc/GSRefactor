package com.betsoft.casino.mp.web.handlers.kafka.privateroom;

import com.betsoft.casino.mp.web.handlers.kafka.KafkaMultiPlayerResponseService;
import com.dgphoenix.casino.kafka.dto.privateroom.request.DeactivateRoomDto;
import com.dgphoenix.casino.kafka.dto.privateroom.response.DeactivateRoomResultDto;
import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.stereotype.Component;


@Component
public class DeactivateRoomHandler implements KafkaOuterRequestHandler<DeactivateRoomDto, DeactivateRoomResultDto> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public DeactivateRoomHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public DeactivateRoomResultDto handle(DeactivateRoomDto request) {
        return serviceHandler.deactivate(request);
    }

    @Override
    public Class<DeactivateRoomDto> getRequestClass() {
        return DeactivateRoomDto.class;
    }
}
