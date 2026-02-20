package com.betsoft.casino.mp.model.gameconfig;

import java.util.List;

public class BossSetting {
    int id;
    double probability;
    List<EnemyParams> head;
    List<EnemyParams> tail;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public List<EnemyParams> getHead() {
        return head;
    }

    public void setHead(List<EnemyParams> head) {
        this.head = head;
    }

    public List<EnemyParams> getTail() {
        return tail;
    }

    public void setTail(List<EnemyParams> tail) {
        this.tail = tail;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BossSetting{");
        sb.append("id=").append(id);
        sb.append(", probability=").append(probability);
        sb.append(", head=").append(head);
        sb.append(", tail=").append(tail);
        sb.append('}');
        return sb.toString();
    }
}
