package com.betsoft.casino.mp.utils;

import com.betsoft.casino.mp.model.onlineplayer.Status;
import com.abs.casino.kafka.dto.BGOStatus;

public class BGOStatusUtil {
    public static Status fromBGOStatus(BGOStatus tbgoStatus) {
        switch (tbgoStatus) {
            case online:
                return Status.online;
            default:
                return Status.offline;
        }
    }

    public static BGOStatus toBGOStatus(Status status) {
        switch (status) {
            case online:
                return BGOStatus.online;
            default:
                return BGOStatus.offline;
        }
    }
}
