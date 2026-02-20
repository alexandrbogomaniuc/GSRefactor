package com.betsoft.casino.mp.transport;

import com.betsoft.casino.mp.model.IDamage;

public class RageDamage implements IDamage {
    private long id;
    private int damage;

    public RageDamage(long id, int damage) {
        this.id = id;
        this.damage = damage;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    @Override
    public String toString() {
        return "RageDamage{" +
                "id=" + id +
                ", damage=" + damage +
                '}';
    }
}
