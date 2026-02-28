package com.abs.casino.kafka.handler;

import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.InServiceServiceHandler;
import com.abs.casino.kafka.dto.GameServerInfoDto;
import com.abs.casino.kafka.dto.GameServerInfoResponseDto;
import com.abs.casino.kafka.dto.GetGameServersInfoRequest;

@Component
public class GetGameServerInfosRequestHandler implements KafkaOuterRequestHandler<GetGameServersInfoRequest, GameServerInfoResponseDto> {

    // technically this should be not "inService", but :(
    private InServiceServiceHandler serviceHandler;

    @Autowired
    public GetGameServerInfosRequestHandler(InServiceServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public GameServerInfoResponseDto handle(GetGameServersInfoRequest request) {
        Set<GameServerInfoDto> infos = serviceHandler.getGameServersInfo();
        return new GameServerInfoResponseDto(infos);
    }

    @Override
    public Class<GetGameServersInfoRequest> getRequestClass() {
        return GetGameServersInfoRequest.class;
    }

}
