package com.abs.casino.kafka.dto;

import java.util.List;

public class MQQuestDataDto {
    private long id;
    private int type;
    private long roomCoin;
    private boolean needReset;
    private long collectedAmount;
    private java.lang.String name;
    private MQQuestPrizeDto questPrize;
    private List<MQTreasureQuestProgressDto> treasures;

    public MQQuestDataDto() {}

    public MQQuestDataDto(long id,
            int type,
            long roomCoin,
            boolean needReset,
            long collectedAmount,
            String name,
            MQQuestPrizeDto questPrize,
            List<MQTreasureQuestProgressDto> treasures) {
        super();
        this.id = id;
        this.type = type;
        this.roomCoin = roomCoin;
        this.needReset = needReset;
        this.collectedAmount = collectedAmount;
        this.name = name;
        this.questPrize = questPrize;
        this.treasures = treasures;
    }

    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public long getRoomCoin() {
        return roomCoin;
    }

    public boolean isNeedReset() {
        return needReset;
    }

    public long getCollectedAmount() {
        return collectedAmount;
    }

    public String getName() {
        return name;
    }

    public MQQuestPrizeDto getQuestPrize() {
        return questPrize;
    }

    public List<MQTreasureQuestProgressDto> getTreasures() {
        return treasures;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setRoomCoin(long roomCoin) {
        this.roomCoin = roomCoin;
    }

    public void setNeedReset(boolean needReset) {
        this.needReset = needReset;
    }

    public void setCollectedAmount(long collectedAmount) {
        this.collectedAmount = collectedAmount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuestPrize(MQQuestPrizeDto questPrize) {
        this.questPrize = questPrize;
    }

    public void setTreasures(List<MQTreasureQuestProgressDto> treasures) {
        this.treasures = treasures;
    }
}
