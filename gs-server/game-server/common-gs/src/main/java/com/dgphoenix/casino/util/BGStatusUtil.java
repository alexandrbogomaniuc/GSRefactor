package com.abs.casino.util;

import com.abs.casino.common.client.canex.request.privateroom.Status;
import com.dgphoenix.casino.kafka.dto.BGStatus;

public class BGStatusUtil {
    public static Status fromBGStatus(BGStatus tbgStatus) {
        switch (tbgStatus) {
            case accepted:
                return Status.ACCEPTED;
            case rejected:
                return Status.REJECTED;
            case kicked:
                return Status.KICKED;
            case loading:
                return Status.LOADING;
            case ready:
                return Status.READY;
            case waiting:
                return Status.WAITING;
            case playing:
                return Status.PLAYING;
            default:
                return Status.INVITED;
        }
    }

    public static BGStatus toBGStatus(Status status) {
        switch (status) {
            case ACCEPTED:
                return BGStatus.accepted;
            case REJECTED:
                return BGStatus.rejected;
            case KICKED:
                return BGStatus.kicked;
            case LOADING:
                return BGStatus.loading;
            case READY:
                return BGStatus.ready;
            case WAITING:
                return BGStatus.waiting;
            case PLAYING:
                return BGStatus.playing;
            default:
                return BGStatus.invited;
        }
    }
}
