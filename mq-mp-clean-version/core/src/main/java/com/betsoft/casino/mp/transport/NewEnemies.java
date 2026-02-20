package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.INewEnemies;
import com.betsoft.casino.mp.model.IRoomEnemy;
import com.betsoft.casino.utils.TObject;

import java.util.ArrayList;
import java.util.List;

public class NewEnemies extends TObject implements INewEnemies {
    private List<RoomEnemy> enemies;

    public NewEnemies(long date, List<IRoomEnemy> enemies) {
        super(date, SERVER_RID);
        this.enemies = RoomEnemy.convert(enemies);
    }

    @Override
    public List<IRoomEnemy> getEnemies() {
        return new ArrayList<>(enemies);
    }

    @Override
    public String toString() {
        return "NewEnemies[" +
                "enemies=" + enemies +
                ", rid=" + rid +
                ", date=" + date +
                ']';
    }
}
