package com.dgphoenix.casino.support.configuration;

public class ServerStatus {
    private String name;
    private boolean online;
    private boolean master;

    public ServerStatus(String name, boolean online, boolean master) {
        this.name = name;
        this.online = online;
        this.master = master;
    }

    public String getName() { return name; }
    public boolean isOnline() { return online; }
    public boolean isMaster() { return master; }
}
