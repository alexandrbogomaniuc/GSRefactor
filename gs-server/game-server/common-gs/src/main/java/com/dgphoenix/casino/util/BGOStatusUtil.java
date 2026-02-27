package com.abs.casino.util;

import com.dgphoenix.casino.kafka.dto.BGOStatus;
import com.abs.casino.kafka.dto.privateroom.request.StatusOnlinePlayer;

public class BGOStatusUtil {
    public static StatusOnlinePlayer fromBGOStatus(BGOStatus tbgoStatus) {
        switch (tbgoStatus) {
            case online:
                return StatusOnlinePlayer.online;
            default:
                return StatusOnlinePlayer.offline;
        }
    }

    public static BGOStatus toBGOStatus(StatusOnlinePlayer status) {
        switch (status) {
            case online:
                return BGOStatus.online;
            default:
                return BGOStatus.offline;
        }
    }
}
