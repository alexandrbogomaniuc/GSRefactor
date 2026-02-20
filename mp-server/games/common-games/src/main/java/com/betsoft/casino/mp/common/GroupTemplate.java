package com.betsoft.casino.mp.common;

import java.util.List;

public class GroupTemplate {
    private int id;
    private List<GroupMember> enemies;

    public GroupTemplate(int id, List<GroupMember> enemies) {
        this.id = id;
        this.enemies = enemies;
    }

    public int getId() {
        return id;
    }

    public List<GroupMember> getEnemies() {
        return enemies;
    }

    @Override
    public String toString() {
        return "GroupTemplate{" +
                "id=" + id +
                ", enemies=" + enemies +
                '}';
    }
}
