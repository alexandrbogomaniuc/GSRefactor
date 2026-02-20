package com.dgphoenix.casino.entities.lobby;

/**
 * User: Grien
 * Date: 13.10.2011 16:06
 */
public class ShortGameInfo {
    private String name;
    private long id;
    private long pcGameId;

    public ShortGameInfo(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public ShortGameInfo(String name, long id, long pcGameId) {
        this.name = name;
        this.id = id;
        this.pcGameId = pcGameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPcGameId() {
        return pcGameId;
    }

    public void setPcGameId(long pcGameId) {
        this.pcGameId = pcGameId;
    }

    @Override
    public String toString() {
        return "ShortGameInfo[" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", pcGameId=" + pcGameId +
                ']';
    }
}
