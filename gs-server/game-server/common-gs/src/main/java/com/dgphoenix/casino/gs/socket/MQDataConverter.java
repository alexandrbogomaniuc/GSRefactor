package com.dgphoenix.casino.gs.socket;

import com.abs.casino.common.mp.*;
import com.dgphoenix.casino.kafka.dto.MQDataDto;
import com.dgphoenix.casino.kafka.dto.MQQuestAmountDto;
import com.dgphoenix.casino.kafka.dto.MQQuestDataDto;
import com.dgphoenix.casino.kafka.dto.MQQuestPrizeDto;
import com.dgphoenix.casino.kafka.dto.MQTreasureQuestProgressDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MQDataConverter {

    public static MQData convert(MQDataDto data) {
        MQData mqData = new MQData();
        mqData.setAccountId(data.getAccountId());
        mqData.setGameId(data.getGameId());
        mqData.setNickname(data.getNickname());
        mqData.setExperience(data.getExperience());
        mqData.setRounds(data.getRounds());
        mqData.setKills(data.getKills());
        mqData.setTreasures(data.getTreasures());
        mqData.setBorderStyle(data.getBorderStyle());
        mqData.setHero(data.getHero());
        mqData.setBackground(data.getBackground());
        mqData.setBorders(data.getBorders());
        mqData.setHeroes(data.getHeroes());
        mqData.setBackgrounds(data.getBackgrounds());
        mqData.setDisableTooltips(data.isDisableTooltips());
        mqData.setWeapons(data.getWeapons());

        Set<MQQuestData> quests = new HashSet<>();
        for (MQQuestDataDto questData : data.getQuests()) {
            MQQuestData quest = new MQQuestData();
            quest.setId(questData.getId());
            quest.setType(questData.getType());
            quest.setNeedReset(questData.isNeedReset());
            quest.setCollectedAmount(questData.getCollectedAmount());
            quest.setName(questData.getName());
            quest.setRoomCoin(questData.getRoomCoin());

            MQQuestPrizeDto questPrize = questData.getQuestPrize();
            MQuestPrize mQuestPrize = new MQuestPrize(new MQuestAmount(questPrize.getAmount().getFromAmount(),
                    questPrize.getAmount().getToAmount()), questPrize.getSpecialWeaponId());
            quest.setQuestPrize(mQuestPrize);

            List<MQTreasureQuestProgress> treasures = new ArrayList<>();
            for (MQTreasureQuestProgressDto treasure : questData.getTreasures()) {
                treasures.add(new MQTreasureQuestProgress(
                        treasure.getTreasureId(), treasure.getCollect(), treasure.getGoal()));
            }
            quest.setTreasures(treasures);
            quests.add(quest);
        }
        mqData.setQuests(quests);

        return mqData;
    }

    public static MQDataDto convert(MQData mqData) {
        Set<MQQuestDataDto> quests = new HashSet<>();
        for (MQQuestData questData : mqData.getQuests()) {
            List<MQTreasureQuestProgressDto> treasures = new ArrayList<>();
            for (MQTreasureQuestProgress treasure : questData.getTreasures()) {
                treasures.add(new MQTreasureQuestProgressDto(
                        treasure.getTreasureId(), treasure.getCollect(), treasure.getGoal()));
            }
            MQuestPrize questPrize = questData.getQuestPrize();
            MQQuestPrizeDto tmQuestPrize = new MQQuestPrizeDto(new MQQuestAmountDto(questPrize.getAmount().getFrom(),
                    questPrize.getAmount().getTo()), questPrize.getSpecialWeaponId());
            quests.add(new MQQuestDataDto(questData.getId(),
                    questData.getType(), questData.getRoomCoin(), questData.isNeedReset(),
                    questData.getCollectedAmount(), questData.getName(), tmQuestPrize, treasures));
        }

        return new MQDataDto(mqData.getAccountId(),
                mqData.getGameId(),
                mqData.getNickname(),
                mqData.getExperience(),
                mqData.getRounds(),
                mqData.getKills(),
                mqData.getTreasures(),
                mqData.getBorderStyle(),
                mqData.getHero(),
                mqData.getBackground(),
                mqData.getBorders(),
                mqData.getHeroes(),
                mqData.getBackgrounds(),
                mqData.isDisableTooltips(),
                quests,
                mqData.getWeapons());
    }
}
