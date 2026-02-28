package com.betsoft.casino.mp.web.handlers.kafka.privateroom;

import com.betsoft.casino.mp.web.handlers.kafka.KafkaMultiPlayerResponseService;
import com.abs.casino.kafka.dto.privateroom.request.UpdateOnlinePlayersDto;
import com.abs.casino.kafka.dto.privateroom.response.UpdateOnlinePlayersResultDto;
import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.stereotype.Component;


@Component
public class UpdateOnlinePlayersHandler implements KafkaOuterRequestHandler<UpdateOnlinePlayersDto, UpdateOnlinePlayersResultDto> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public UpdateOnlinePlayersHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }


    @Override
    public UpdateOnlinePlayersResultDto handle(UpdateOnlinePlayersDto request) {
        return serviceHandler.updateOnlinePlayers(request);
    }

    @Override
    public Class<UpdateOnlinePlayersDto> getRequestClass() {
        return UpdateOnlinePlayersDto.class;
    }
}
