package com.betsoft.casino.mp.model.gameconfig;

import java.util.List;
import java.util.Objects;

public class RoomModel {
    int room_id;
    double probability;
    List<EnemyRoomParam> enemies;

    public int getRoom_id() {
        return room_id;
    }

    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public List<EnemyRoomParam> getEnemies() {
        return enemies;
    }

    public void setEnemies(List<EnemyRoomParam> enemies) {
        this.enemies = enemies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomModel roomModel = (RoomModel) o;
        return room_id == roomModel.room_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(room_id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RoomModel{");
        sb.append("room_id=").append(room_id);
        sb.append(", probability=").append(probability);
        sb.append(", enemies=").append(enemies);
        sb.append('}');
        return sb.toString();
    }
}
