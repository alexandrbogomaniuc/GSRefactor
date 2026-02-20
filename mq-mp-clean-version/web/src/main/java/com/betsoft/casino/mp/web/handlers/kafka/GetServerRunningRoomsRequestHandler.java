package com.betsoft.casino.mp.web.handlers.kafka;

import com.dgphoenix.casino.kafka.dto.GetServerRunningRoomsRequest;
import com.dgphoenix.casino.kafka.dto.GetServerRunningRoomsResponse;
import com.dgphoenix.casino.kafka.handler.KafkaInServiceRequestHandler;
import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.stereotype.Component;


@Component
public class GetServerRunningRoomsRequestHandler implements KafkaInServiceRequestHandler<GetServerRunningRoomsRequest, GetServerRunningRoomsResponse>,
        KafkaOuterRequestHandler<GetServerRunningRoomsRequest, GetServerRunningRoomsResponse> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public GetServerRunningRoomsRequestHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public GetServerRunningRoomsResponse handle(GetServerRunningRoomsRequest request) {
        return serviceHandler.getServerRunningRoomsResponse(request);
    }

    @Override
    public Class<GetServerRunningRoomsRequest> getRequestClass() {
        return GetServerRunningRoomsRequest.class;
    }
}
