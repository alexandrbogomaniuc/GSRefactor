package com.betsoft.casino.mp.web.handlers.kafka.privateroom;

import com.betsoft.casino.mp.web.handlers.kafka.KafkaMultiPlayerResponseService;
import com.dgphoenix.casino.kafka.dto.StringResponseDto;
import com.dgphoenix.casino.kafka.dto.privateroom.request.PrivateRoomURLDto;
import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.stereotype.Component;


@Component
public class PrivateRoomURLHandler implements KafkaOuterRequestHandler<PrivateRoomURLDto, StringResponseDto> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public PrivateRoomURLHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public StringResponseDto handle(PrivateRoomURLDto request) {
        return serviceHandler.getBGPrivateRoomUrl(request);
    }

    @Override
    public Class<PrivateRoomURLDto> getRequestClass() {
        return PrivateRoomURLDto.class;
    }
}
