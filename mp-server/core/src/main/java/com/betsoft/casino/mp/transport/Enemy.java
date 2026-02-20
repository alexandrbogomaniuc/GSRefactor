package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.ITransportEnemy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: flsh
 * Date: 09.06.17.
 */
public class Enemy implements ITransportEnemy, Serializable {
    private long id;
    private int width;
    private int height;
    private float speed;
    private int prizes;
    //sumAward > 0 for regular, 0 for Boss
    private double sumAward;
    //energy > 0 for Boss, 0 for regular
    private int skins;
    private boolean boss;

    public Enemy(long id, int width, int height, float speed, int prizes, double sumAward, int skins, boolean boss) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.prizes = prizes;
        this.sumAward = sumAward;
        this.skins = skins;
        this.boss = boss;
    }

    public static List<Enemy> convert(List<ITransportEnemy> enemies) {
        List<Enemy> result = new ArrayList<>();
        for (ITransportEnemy enemy : enemies) {
            if (enemy instanceof Enemy) {
                result.add((Enemy) enemy);
            } else {
                result.add(new Enemy(enemy.getId(), enemy.getWidth(), enemy.getHeight(), enemy.getSpeed(),
                        enemy.getPrizes(), enemy.getSumAward(), enemy.getSkins(), enemy.isBoss()));
            }
        }
        return result;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public int getPrizes() {
        return prizes;
    }

    @Override
    public double getSumAward() {
        return sumAward;
    }

    @Override
    public int getSkins() {
        return skins;
    }

    @Override
    public boolean isBoss() {
        return boss;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enemy enemy = (Enemy) o;
        return id == enemy.id &&
                width == enemy.width &&
                height == enemy.height &&
                Float.compare(enemy.speed, speed) == 0 &&
                prizes == enemy.prizes &&
                Double.compare(enemy.sumAward, sumAward) == 0 &&
                skins == enemy.skins &&
                boss == enemy.boss;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "Enemy[" +
                "id=" + id +
                ", width=" + width +
                ", height=" + height +
                ", speed=" + speed +
                ", prizes=" + prizes +
                ", sumAward=" + sumAward +
                ", skins=" + skins +
                ", boss=" + boss +
                ']';
    }
}
