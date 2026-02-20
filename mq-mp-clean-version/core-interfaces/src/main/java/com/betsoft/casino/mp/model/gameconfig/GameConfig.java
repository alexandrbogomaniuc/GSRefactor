package com.betsoft.casino.mp.model.gameconfig;

import java.util.List;
import java.util.Map;

public class GameConfig {
    Map<String, List<String>> enemies_wins;
    List<String> treasures_list;
    List<String> weapons_list;
    Map<String, Map<String, Map<String, Double>>> free_weapons;
    Map<String, Map<String, Map<Integer, Double>>> dropped_weapons;
    List<RoomModel> rooms;
    Map<String, Map<Integer, EnemyParams>> enemies;
    Map<String, Map<String, Map<Double, Integer>>> weapons;
    Map<String, QuestParam> quests;
    Boss boss;
    BuySpecialWeapons buy_weapons;
    Map<String, Integer> new_player_bullets_bonus;

    public List<RoomModel> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomModel> rooms) {
        this.rooms = rooms;
    }

    public Map<String, Map<Integer, EnemyParams>> getEnemies() {
        return enemies;
    }

    public void setEnemies(Map<String, Map<Integer, EnemyParams>> enemies) {
        this.enemies = enemies;
    }

    public Map<String, Map<String, Map<Double, Integer>>> getWeapons() {
        return weapons;
    }

    public void setWeapons(Map<String, Map<String, Map<Double, Integer>>> weapons) {
        this.weapons = weapons;
    }

    public Map<String, QuestParam> getQuests() {
        return quests;
    }

    public void setQuests(Map<String, QuestParam> quests) {
        this.quests = quests;
    }

    public Boss getBoss() {
        return boss;
    }

    public void setBoss(Boss boss) {
        this.boss = boss;
    }

    public Map<String, List<String>> getEnemies_wins() {
        return enemies_wins;
    }

    public void setEnemies_wins(Map<String, List<String>> enemies_wins) {
        this.enemies_wins = enemies_wins;
    }


    public Map<String, Map<String, Map<String, Double>>> getFree_weapons() {
        return free_weapons;
    }

    public void setFree_weapons(Map<String, Map<String, Map<String, Double>>> free_weapons) {
        this.free_weapons = free_weapons;
    }

    public List<String> getTreasures_list() {
        return treasures_list;
    }

    public void setTreasures_list(List<String> treasures_list) {
        this.treasures_list = treasures_list;
    }

    public List<String> getWeapons_list() {
        return weapons_list;
    }

    public void setWeapons_list(List<String> weapons_list) {
        this.weapons_list = weapons_list;
    }

    public Map<String, Map<String, Map<Integer, Double>>> getDropped_weapons() {
        return dropped_weapons;
    }

    public void setDropped_weapons(Map<String, Map<String, Map<Integer, Double>>> dropped_weapons) {
        this.dropped_weapons = dropped_weapons;
    }

    public BuySpecialWeapons getBuy_weapons() {
        return buy_weapons;
    }

    public void setBuy_weapons(BuySpecialWeapons buy_weapons) {
        this.buy_weapons = buy_weapons;
    }

    public Map<String, Integer> getNew_player_bullets_bonus() {
        return new_player_bullets_bonus;
    }

    public void setNew_player_bullets_bonus(Map<String, Integer> new_player_bullets_bonus) {
        this.new_player_bullets_bonus = new_player_bullets_bonus;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameConfig[");
        sb.append("treasures_list=").append(treasures_list);
        sb.append(", weapons_list=").append(weapons_list);
        sb.append(", dropped_weapons=").append(dropped_weapons);
        sb.append(", rooms=").append(rooms);
        sb.append(", enemies=").append(enemies);
        sb.append(", weapons=").append(weapons);
        sb.append(", quests=").append(quests);
        sb.append(", boss=").append(boss);
        sb.append(", buy_weapons=").append(buy_weapons);
        sb.append(", new_player_bullets_bonus").append(new_player_bullets_bonus);
        sb.append(", enemies_wins=").append(enemies_wins);
        sb.append(", free_weapons=").append(free_weapons);
        sb.append(']');
        return sb.toString();
    }
}
