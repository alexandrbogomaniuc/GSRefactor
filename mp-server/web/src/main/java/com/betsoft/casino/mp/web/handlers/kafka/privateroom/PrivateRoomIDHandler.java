package com.betsoft.casino.mp.web.handlers.kafka.privateroom;

import com.betsoft.casino.mp.web.handlers.kafka.KafkaMultiPlayerResponseService;
import com.abs.casino.kafka.dto.privateroom.request.PrivateRoomIdDto;
import com.abs.casino.kafka.dto.privateroom.response.PrivateRoomIdResultDto;
import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.stereotype.Component;


@Component
public class PrivateRoomIDHandler implements KafkaOuterRequestHandler<PrivateRoomIdDto, PrivateRoomIdResultDto> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public PrivateRoomIDHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public PrivateRoomIdResultDto handle(PrivateRoomIdDto request) {
        return serviceHandler.getPrivateRoomId(request);
    }

    @Override
    public Class<PrivateRoomIdDto> getRequestClass() {
        return PrivateRoomIdDto.class;
    }
}
