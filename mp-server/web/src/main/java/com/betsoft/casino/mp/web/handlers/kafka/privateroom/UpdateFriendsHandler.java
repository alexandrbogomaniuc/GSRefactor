package com.betsoft.casino.mp.web.handlers.kafka.privateroom;

import com.betsoft.casino.mp.web.handlers.kafka.KafkaMultiPlayerResponseService;
import com.abs.casino.kafka.dto.privateroom.request.UpdateFriendsDto;
import com.abs.casino.kafka.dto.privateroom.response.UpdateFriendsResultDto;
import com.abs.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.stereotype.Component;


@Component
public class UpdateFriendsHandler implements KafkaOuterRequestHandler<UpdateFriendsDto, UpdateFriendsResultDto> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public UpdateFriendsHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public UpdateFriendsResultDto handle(UpdateFriendsDto request) {
        return serviceHandler.updateFriends(request);
    }

    @Override
    public Class<UpdateFriendsDto> getRequestClass() {
        return UpdateFriendsDto.class;
    }
}
