package com.dgphoenix.casino.util;

import com.dgphoenix.casino.common.client.canex.request.friends.Status;
import com.dgphoenix.casino.kafka.dto.BGFStatus;

public class BGFStatusUtil {
    public static Status fromTBGFStatus(BGFStatus tbgfStatus) {
        switch (tbgfStatus) {
            case sent:
                return Status.sent;
            case received:
                return Status.received;
            case rejected:
                return Status.rejected;
            case blocked:
                return Status.blocked;
            default:
                return Status.friend;
        }
    }

    public static BGFStatus toTBGFStatus(Status status) {
        switch (status) {
            case sent:
                return BGFStatus.sent;
            case received:
                return BGFStatus.received;
            case rejected:
                return BGFStatus.rejected;
            case blocked:
                return BGFStatus.blocked;
            default:
                return BGFStatus.friend;
        }
    }    
}
