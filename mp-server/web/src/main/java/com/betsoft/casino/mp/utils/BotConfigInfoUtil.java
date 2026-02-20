package com.betsoft.casino.mp.utils;

import java.util.HashSet;
import java.util.Set;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.bots.BotConfigInfo;
import com.betsoft.casino.mp.model.bots.TimeFrame;
import com.dgphoenix.casino.kafka.dto.BotConfigInfoDto;
import com.dgphoenix.casino.kafka.dto.TimeFrameDto;

public class BotConfigInfoUtil {
    public static BotConfigInfoDto toTBotConfigInfo(BotConfigInfo info){
        BotConfigInfoDto tBotConfigInfo = new BotConfigInfoDto();

        tBotConfigInfo.setId(info.getId());
        tBotConfigInfo.setBankId(info.getBankId());
        Set<Long> allowedGames = toGameIds(info.getAllowedGames());
        tBotConfigInfo.setAllowedGames(allowedGames);
        tBotConfigInfo.setActive(info.isActive());
        tBotConfigInfo.setUsername(info.getUsername());
        tBotConfigInfo.setPassword(info.getPassword());
        tBotConfigInfo.setMqNickname(info.getMqNickname());
        tBotConfigInfo.setMmcBalance(info.getMmcBalance());
        tBotConfigInfo.setMqcBalance(info.getMqcBalance());
        Set<TimeFrameDto> tTimeFrames = toTTimeFrames(info.getTimeFrames());
        tBotConfigInfo.setTimeFrames(tTimeFrames);
        Set<Long> allowedBankIds = new HashSet<>(info.getAllowedBankIds());
        tBotConfigInfo.setAllowedBankIds(allowedBankIds);

        return tBotConfigInfo;
    }

    public static BotConfigInfo fromTBotConfigInfo(BotConfigInfoDto tBotConfigInfo) {

        if (tBotConfigInfo == null) {
            return null;
        }

        Set<TimeFrame> timeFrames = TimeFrameUtil.fromTTimeFrames(tBotConfigInfo.getTimeFrames());
        Set<GameType> allowedGames = fromGameIds(tBotConfigInfo.getAllowedGames());
        Set<Long> allowedBankIds = new HashSet<>(tBotConfigInfo.getAllowedBankIds());

        BotConfigInfo botConfigInfo = new BotConfigInfo(
                tBotConfigInfo.getId(),
                tBotConfigInfo.getBankId(),
                allowedGames,
                tBotConfigInfo.isActive(),
                tBotConfigInfo.getUsername(),
                tBotConfigInfo.getPassword(),
                tBotConfigInfo.getMqNickname(),
                null,
                timeFrames,
                allowedBankIds,
                null,
                null,
                null
        );

        botConfigInfo.setMqcBalance(tBotConfigInfo.getMqcBalance());
        botConfigInfo.setMmcBalance(tBotConfigInfo.getMmcBalance());

        return botConfigInfo;
    }

    private static Set<GameType> fromGameIds(Set<Long> gameIds) {

        if(gameIds == null) {
            return null;
        }

        Set<GameType> gameTypes = new HashSet<>();

        for(Long gameId : gameIds) {
            if(gameId != null) {
                GameType gameType = GameType.getByGameId((int) (long) gameId);
                if(gameType != null && BotConfigInfo.allowedMQGames.contains(gameType)) {
                    gameTypes.add(gameType);
                }
            }
        }

        return gameTypes;
    }

    private static Set<Long> toGameIds(Set<GameType> gameTypes) {
        if(gameTypes == null) {
            return null;
        }

        Set<Long> gameIds = new HashSet<>();

        for(GameType gameType : gameTypes) {
            if(gameType != null) {
                int gameId = (int)gameType.getGameId();
                if(gameId > 0) {
                    gameIds.add((long)gameId);
                }
            }
        }

        return gameIds;
    }

    public static Set<TimeFrameDto> toTTimeFrames(Set<TimeFrame> timeFrames) {
        if(timeFrames == null) {
            return null;
        }

        Set<TimeFrameDto> tTimeFrames = new HashSet<>();

        for(TimeFrame timeFrame : timeFrames) {
            if(timeFrame != null) {
                TimeFrameDto tTimeFrame = TimeFrameUtil.toTTimeFrame(timeFrame);
                if(tTimeFrame != null) {
                    tTimeFrames.add(tTimeFrame);
                }
            }
        }

        return tTimeFrames;
    }
}
