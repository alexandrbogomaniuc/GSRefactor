package com.betsoft.casino.mp.web.handlers.kafka;

import com.abs.casino.kafka.dto.TournamentEndedDto;
import com.dgphoenix.casino.kafka.dto.VoidKafkaResponse;
import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.stereotype.Component;


@Component
public class TournamentEndedHandler  implements KafkaOuterRequestHandler<TournamentEndedDto, VoidKafkaResponse> {
    private final KafkaMultiPlayerResponseService serviceHandler;

    public TournamentEndedHandler(KafkaMultiPlayerResponseService serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public VoidKafkaResponse handle(TournamentEndedDto request) {
        serviceHandler.sendTournamentEnded(request);
        return VoidKafkaResponse.success();
    }

    @Override
    public Class<TournamentEndedDto> getRequestClass() {
        return TournamentEndedDto.class;
    }
}
