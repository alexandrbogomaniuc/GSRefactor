package com.betsoft.casino.mp.web.handlers.kafka.privateroom;

import com.betsoft.casino.mp.web.handlers.kafka.KafkaMultiPlayerResponseService;
import com.dgphoenix.casino.kafka.dto.privateroom.request.ParticipantGameSessionDto;
import com.dgphoenix.casino.kafka.dto.privateroom.response.RoomInfoResultDto;
import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.stereotype.Component;


@Component
public class CurrentBattlegroundRoomInfoForPlayerHandler implements KafkaOuterRequestHandler<ParticipantGameSessionDto, RoomInfoResultDto> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public CurrentBattlegroundRoomInfoForPlayerHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public RoomInfoResultDto handle(ParticipantGameSessionDto request) {
        return serviceHandler.loadCurrentBattlegroundRoomInfoForPlayer(request);
    }

    @Override
    public Class<ParticipantGameSessionDto> getRequestClass() {
        return ParticipantGameSessionDto.class;
    }
}
