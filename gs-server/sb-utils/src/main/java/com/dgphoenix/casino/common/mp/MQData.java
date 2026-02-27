package com.abs.casino.common.mp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MQData implements KryoSerializable, Serializable {
    private static final byte VERSION = 0;

    private long accountId;
    private long gameId;
    private String nickname;
    private double experience;
    private int rounds;
    private Map<Integer, Long> kills = new HashMap<Integer, Long>();
    private Map<Integer, Long> treasures = new HashMap<Integer, Long>();

    // PlayerProfile
    private int borderStyle;
    private int hero;
    private int background;

    private Set<Integer> borders;
    private Set<Integer> heroes;
    private Set<Integer> backgrounds;

    private boolean disableTooltips;

    // Quests
    private Set<MQQuestData> quests = new HashSet<MQQuestData>();
    private Map<Long, Map<Integer, Integer>> weapons;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = experience;
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    public Map<Integer, Long> getKills() {
        return kills;
    }

    public void setKills(Map<Integer, Long> kills) {
        this.kills = kills;
    }

    public Map<Integer, Long> getTreasures() {
        return treasures;
    }

    public void setTreasures(Map<Integer, Long> treasures) {
        this.treasures = treasures;
    }

    public int getBorderStyle() {
        return borderStyle;
    }

    public void setBorderStyle(int borderStyle) {
        this.borderStyle = borderStyle;
    }

    public int getHero() {
        return hero;
    }

    public void setHero(int hero) {
        this.hero = hero;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public Set<Integer> getBorders() {
        return borders;
    }

    public void setBorders(Set<Integer> borders) {
        this.borders = borders;
    }

    public Set<Integer> getHeroes() {
        return heroes;
    }

    public void setHeroes(Set<Integer> heroes) {
        this.heroes = heroes;
    }

    public Set<Integer> getBackgrounds() {
        return backgrounds;
    }

    public void setBackgrounds(Set<Integer> backgrounds) {
        this.backgrounds = backgrounds;
    }

    public boolean isDisableTooltips() {
        return disableTooltips;
    }

    public void setDisableTooltips(boolean disableTooltips) {
        this.disableTooltips = disableTooltips;
    }

    public Set<MQQuestData> getQuests() {
        return quests;
    }

    public void setQuests(Set<MQQuestData> quests) {
        this.quests = quests;
    }

    public Map<Long, Map<Integer, Integer>> getWeapons() {
        return weapons;
    }

    public void setWeapons(Map<Long, Map<Integer, Integer>> weapons) {
        this.weapons = weapons;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(accountId, true);
        output.writeLong(gameId, true);
        output.writeString(nickname);
        output.writeDouble(experience);
        output.writeInt(rounds, true);
        kryo.writeObject(output, kills);
        kryo.writeObject(output, treasures);
        output.writeInt(borderStyle, true);
        output.writeInt(hero, true);
        output.writeInt(background, true);
        kryo.writeObject(output, borders);
        kryo.writeObject(output, heroes);
        kryo.writeObject(output, backgrounds);
        output.writeBoolean(disableTooltips);
        kryo.writeObject(output, quests);
        kryo.writeObject(output, weapons);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        accountId = input.readLong(true);
        gameId = input.readLong(true);
        nickname = input.readString();
        experience = input.readDouble();
        rounds = input.readInt(true);
        kills = kryo.readObject(input, HashMap.class);
        treasures = kryo.readObject(input, HashMap.class);
        borderStyle = input.readInt(true);
        hero = input.readInt(true);
        background = input.readInt(true);
        borders = kryo.readObject(input, HashSet.class);
        heroes = kryo.readObject(input, HashSet.class);
        backgrounds = kryo.readObject(input, HashSet.class);
        disableTooltips = input.readBoolean();
        quests = kryo.readObject(input, HashSet.class);
        weapons = kryo.readObject(input, HashMap.class);
    }

    @Override
    public String toString() {
        return "MQData{" +
                "accountId=" + accountId +
                ", gameId=" + gameId +
                ", nickname='" + nickname + '\'' +
                ", experience=" + experience +
                ", rounds=" + rounds +
                ", kills=" + kills +
                ", treasures=" + treasures +
                ", borderStyle=" + borderStyle +
                ", hero=" + hero +
                ", background=" + background +
                ", borders=" + borders +
                ", heroes=" + heroes +
                ", backgrounds=" + backgrounds +
                ", disableTooltips=" + disableTooltips +
                ", quests=" + quests +
                ", weapons=" + weapons+
                '}';
    }
}
