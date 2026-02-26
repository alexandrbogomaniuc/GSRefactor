package com.dgphoenix.casino.util;

import java.util.Set;

import com.abs.casino.ats.BotConfigInfo;
import com.dgphoenix.casino.kafka.dto.BotConfigInfoDto;
import com.dgphoenix.casino.kafka.dto.TimeFrameDto;

public class BotConfigInfoUtil {
    public static BotConfigInfo fromTBotConfigInfo(BotConfigInfoDto tBotConfigInfo) {

        if (tBotConfigInfo == null) {
            return null;
        }

        return new BotConfigInfo(
                tBotConfigInfo.getId(),
                tBotConfigInfo.getBankId(),
                tBotConfigInfo.getAllowedGames(),
                tBotConfigInfo.isActive(),
                tBotConfigInfo.getUsername(),
                tBotConfigInfo.getPassword(),
                tBotConfigInfo.getMqNickname(),
                tBotConfigInfo.getMqcBalance(),
                tBotConfigInfo.getMmcBalance(),
                TimeFrameUtil.fromTTimeFrames(tBotConfigInfo.getTimeFrames()),
                tBotConfigInfo.getAllowedBankIds()
        );

    }

    public static BotConfigInfoDto toTBotConfigInfo(BotConfigInfo botConfigInfo){
        BotConfigInfoDto tBotConfigInfo = new BotConfigInfoDto();

        long id = botConfigInfo.getId() == null ? 0 : botConfigInfo.getId();
        tBotConfigInfo.setId(id);
        tBotConfigInfo.setBankId(botConfigInfo.getBankId());
        tBotConfigInfo.setAllowedGames(botConfigInfo.getAllowedGames());
        tBotConfigInfo.setActive(botConfigInfo.isActive());
        tBotConfigInfo.setUsername(botConfigInfo.getUsername());
        tBotConfigInfo.setPassword(botConfigInfo.getPassword());
        tBotConfigInfo.setMqNickname(botConfigInfo.getMqNickname());
        tBotConfigInfo.setMmcBalance(botConfigInfo.getMmcBalance());
        tBotConfigInfo.setMqcBalance(botConfigInfo.getMqcBalance());
        Set<TimeFrameDto> tTimeFrames = TimeFrameUtil.toTTimeFrames(botConfigInfo.getTimeFrames());
        tBotConfigInfo.setTimeFrames(tTimeFrames);
        tBotConfigInfo.setAllowedBankIds(botConfigInfo.getAllowedBankIds());

        return tBotConfigInfo;
    }
}
