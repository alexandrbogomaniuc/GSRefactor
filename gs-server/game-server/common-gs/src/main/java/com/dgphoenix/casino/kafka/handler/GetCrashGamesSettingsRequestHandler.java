package com.dgphoenix.casino.kafka.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.CrashGameSettingsResponseDto;
import com.dgphoenix.casino.kafka.dto.GetCrashGamesSettingsRequest;

@Component
public class GetCrashGamesSettingsRequestHandler 
       implements KafkaOuterRequestHandler<GetCrashGamesSettingsRequest, CrashGameSettingsResponseDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public GetCrashGamesSettingsRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public CrashGameSettingsResponseDto handle(GetCrashGamesSettingsRequest request) {
        return new CrashGameSettingsResponseDto(mqServiceHandler.getCrashGameSettings(request.getBankIds(), request.getGameId()));
    }

    @Override
    public Class<GetCrashGamesSettingsRequest> getRequestClass() {
        return GetCrashGamesSettingsRequest.class;
    }

}
