package com.abs.casino.kafka.dto;

import com.dgphoenix.casino.kafka.dto.KafkaRequest;

import java.util.Map;
import java.util.Set;

public class MQDataDto implements KafkaRequest {
    private long accountId;
    private long gameId;
    private String nickname;
    private double experience;
    private int rounds;
    private Map<Integer, Long> kills;
    private Map<Integer, Long> treasures;
    private int borderStyle;
    private int hero;
    private int background;
    private Set<Integer> borders;
    private Set<Integer> heroes;
    private Set<Integer> backgrounds;
    private boolean disableTooltips;
    private Set<MQQuestDataDto> quests;
    private Map<Long, Map<Integer, Integer>> weapons;

    public MQDataDto() {}

    public MQDataDto(long accountId,
            long gameId,
            String nickname,
            double experience,
            int rounds,
            Map<Integer, Long> kills,
            Map<Integer, Long> treasures,
            int borderStyle,
            int hero,
            int background,
            Set<Integer> borders,
            Set<Integer> heroes,
            Set<Integer> backgrounds,
            boolean disableTooltips,
            Set<MQQuestDataDto> quests,
            Map<Long, Map<Integer, Integer>> weapons) {
        super();
        this.accountId = accountId;
        this.gameId = gameId;
        this.nickname = nickname;
        this.experience = experience;
        this.rounds = rounds;
        this.kills = kills;
        this.treasures = treasures;
        this.borderStyle = borderStyle;
        this.hero = hero;
        this.background = background;
        this.borders = borders;
        this.heroes = heroes;
        this.backgrounds = backgrounds;
        this.disableTooltips = disableTooltips;
        this.quests = quests;
        this.weapons = weapons;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getGameId() {
        return gameId;
    }

    public String getNickname() {
        return nickname;
    }

    public double getExperience() {
        return experience;
    }

    public int getRounds() {
        return rounds;
    }

    public Map<Integer, Long> getKills() {
        return kills;
    }

    public Map<Integer, Long> getTreasures() {
        return treasures;
    }

    public int getBorderStyle() {
        return borderStyle;
    }

    public int getHero() {
        return hero;
    }

    public int getBackground() {
        return background;
    }

    public Set<Integer> getBorders() {
        return borders;
    }

    public Set<Integer> getHeroes() {
        return heroes;
    }

    public Set<Integer> getBackgrounds() {
        return backgrounds;
    }

    public boolean isDisableTooltips() {
        return disableTooltips;
    }

    public Set<MQQuestDataDto> getQuests() {
        return quests;
    }

    public Map<Long, Map<Integer, Integer>> getWeapons() {
        return weapons;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setExperience(double experience) {
        this.experience = experience;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public void setKills(Map<Integer, Long> kills) {
        this.kills = kills;
    }

    public void setTreasures(Map<Integer, Long> treasures) {
        this.treasures = treasures;
    }

    public void setBorderStyle(int borderStyle) {
        this.borderStyle = borderStyle;
    }

    public void setHero(int hero) {
        this.hero = hero;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public void setBorders(Set<Integer> borders) {
        this.borders = borders;
    }

    public void setHeroes(Set<Integer> heroes) {
        this.heroes = heroes;
    }

    public void setBackgrounds(Set<Integer> backgrounds) {
        this.backgrounds = backgrounds;
    }

    public void setDisableTooltips(boolean disableTooltips) {
        this.disableTooltips = disableTooltips;
    }

    public void setQuests(Set<MQQuestDataDto> quests) {
        this.quests = quests;
    }

    public void setWeapons(Map<Long, Map<Integer, Integer>> weapons) {
        this.weapons = weapons;
    }
}
