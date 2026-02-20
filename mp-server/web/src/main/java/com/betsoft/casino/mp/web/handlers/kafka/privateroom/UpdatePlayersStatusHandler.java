package com.betsoft.casino.mp.web.handlers.kafka.privateroom;

import com.betsoft.casino.mp.web.handlers.kafka.KafkaMultiPlayerResponseService;
import com.dgphoenix.casino.kafka.dto.privateroom.request.UpdateRoomDto;
import com.dgphoenix.casino.kafka.dto.privateroom.response.UpdateRoomResultDto;
import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.stereotype.Component;


@Component
public class UpdatePlayersStatusHandler implements KafkaOuterRequestHandler<UpdateRoomDto, UpdateRoomResultDto> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public UpdatePlayersStatusHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public UpdateRoomResultDto handle(UpdateRoomDto request) {
        return serviceHandler.updatePlayersStatusInPrivateRoom(request);
    }

    @Override
    public Class<UpdateRoomDto> getRequestClass() {
        return UpdateRoomDto.class;
    }
}
