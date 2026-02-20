package com.dgphoenix.casino.promo;

import com.dgphoenix.casino.common.exception.CommonException;

public interface IKafkaRequestMultiPlayer {
    void sendTournamentEnded(long campaignId, String oldStatus, String newStatus) throws CommonException;
}
