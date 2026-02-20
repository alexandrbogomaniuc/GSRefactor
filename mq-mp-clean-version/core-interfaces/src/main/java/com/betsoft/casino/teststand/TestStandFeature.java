package com.betsoft.casino.teststand;

import com.betsoft.casino.mp.model.GameType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TestStandFeature {
    int id;
    String name;
    Map<Integer, AtomicInteger> featuresAppeared;
    List<Long> gameIds;

    public TestStandFeature(int id, String name) {
        this.id = id;
        this.name = name;
        this.featuresAppeared = new HashMap<>();
        this.gameIds = GameType.getAllGameIds(); // all
    }

    public TestStandFeature(int id, String name, Map<Integer, AtomicInteger> featuresAppeared, List<Long> gameIds) {
        this.id = id;
        this.name = name;
        this.featuresAppeared = new HashMap<>();
        featuresAppeared.forEach((integer, atomicInteger) -> {
            this.featuresAppeared.put(integer, new AtomicInteger(atomicInteger.get()));
        });
        this.gameIds = gameIds;
    }


    public List<Long> getGameIds() {
        return gameIds;
    }

    public void setGameIds(List<Long> gameIds) {
        this.gameIds = gameIds;
    }

    public TestStandFeature copy() {
        return new TestStandFeature(id, name, featuresAppeared, gameIds);
    }

    public Map<Integer, AtomicInteger> getFeaturesAppeared() {
        return featuresAppeared;
    }

    public void setFeaturesAppeared(Map<Integer, AtomicInteger> featuresAppeared) {
        this.featuresAppeared = featuresAppeared;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TestStandFeature{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", featuresAppeared='" + featuresAppeared + '\'' +
                ", gameIds='" + gameIds + '\'' +
                '}';
    }

}
