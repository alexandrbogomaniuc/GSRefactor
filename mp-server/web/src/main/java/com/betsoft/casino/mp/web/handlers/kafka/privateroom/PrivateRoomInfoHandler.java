package com.betsoft.casino.mp.web.handlers.kafka.privateroom;

import com.betsoft.casino.mp.web.handlers.kafka.KafkaMultiPlayerResponseService;
import com.abs.casino.kafka.dto.GetPrivateRoomInfoRequest;
import com.abs.casino.kafka.dto.privateroom.response.PrivateRoomInfoResultDto;
import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.stereotype.Component;


@Component
public class PrivateRoomInfoHandler implements KafkaOuterRequestHandler<GetPrivateRoomInfoRequest, PrivateRoomInfoResultDto> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public PrivateRoomInfoHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public PrivateRoomInfoResultDto handle(GetPrivateRoomInfoRequest request) {
        return serviceHandler.getPrivateRoomInfo(request);
    }

    @Override
    public Class<GetPrivateRoomInfoRequest> getRequestClass() {
        return GetPrivateRoomInfoRequest.class;
    }
}
