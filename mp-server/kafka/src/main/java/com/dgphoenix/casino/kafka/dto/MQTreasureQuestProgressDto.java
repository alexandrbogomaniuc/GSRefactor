package com.abs.casino.kafka.dto;

public class MQTreasureQuestProgressDto {
    private int treasureId;
    private int collect;
    private int goal;

    public MQTreasureQuestProgressDto() {}

    public MQTreasureQuestProgressDto(int treasureId, int collect, int goal) {
        this.treasureId = treasureId;
        this.collect = collect;
        this.goal = goal;
    }

    public int getTreasureId() {
        return treasureId;
    }

    public int getCollect() {
        return collect;
    }

    public int getGoal() {
        return goal;
    }

    public void setTreasureId(int treasureId) {
        this.treasureId = treasureId;
    }

    public void setCollect(int collect) {
        this.collect = collect;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }
}
