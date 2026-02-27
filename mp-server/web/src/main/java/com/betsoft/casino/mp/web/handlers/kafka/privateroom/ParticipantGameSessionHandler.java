package com.betsoft.casino.mp.web.handlers.kafka.privateroom;

import com.betsoft.casino.mp.web.handlers.kafka.KafkaMultiPlayerResponseService;
import com.abs.casino.kafka.dto.privateroom.request.GetParticipantAccountIdsInRoundDto;
import com.abs.casino.kafka.dto.privateroom.response.CollectionResponseDto;
import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.stereotype.Component;

@Component
public class ParticipantGameSessionHandler implements KafkaOuterRequestHandler<GetParticipantAccountIdsInRoundDto, CollectionResponseDto> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public ParticipantGameSessionHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public CollectionResponseDto handle(GetParticipantAccountIdsInRoundDto request) {
        return serviceHandler.getParticipantAccountIdsInRound(request);
    }

    @Override
    public Class<GetParticipantAccountIdsInRoundDto> getRequestClass() {
        return GetParticipantAccountIdsInRoundDto.class;
    }
}
