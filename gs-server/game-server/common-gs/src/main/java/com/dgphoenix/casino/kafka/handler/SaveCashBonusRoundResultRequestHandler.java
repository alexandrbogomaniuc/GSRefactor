package com.abs.casino.kafka.handler;

import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dgphoenix.casino.gs.socket.mq.MQServiceHandler;
import com.dgphoenix.casino.kafka.dto.CashBonusDto;
import com.dgphoenix.casino.kafka.dto.SaveCashBonusRoundResultRequest;

@Component
public class SaveCashBonusRoundResultRequestHandler 
       implements KafkaOuterRequestHandler<SaveCashBonusRoundResultRequest, CashBonusDto> {

    private MQServiceHandler mqServiceHandler;

    @Autowired
    public SaveCashBonusRoundResultRequestHandler(MQServiceHandler mqServiceHandler) {
        this.mqServiceHandler = mqServiceHandler;
    }

    @Override
    public CashBonusDto handle(SaveCashBonusRoundResultRequest request) {
        return mqServiceHandler.saveCashBonusRoundResult(request.getAccountId(), request.getSessionId(), request.getGameSessionId(), request.getBonusId(), request.getBalance(), request.getBetSum(), request.getData(), request.getRoundInfo(), request.getRoundId());
    }

    @Override
    public Class<SaveCashBonusRoundResultRequest> getRequestClass() {
        return SaveCashBonusRoundResultRequest.class;
    }

}
