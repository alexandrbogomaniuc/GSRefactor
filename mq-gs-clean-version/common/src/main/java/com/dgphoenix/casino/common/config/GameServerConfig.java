package com.dgphoenix.casino.common.config;

public class GameServerConfig {
    private int serverId;
    private String name;
    private boolean isOnline;
    private boolean isMaster;
    private GameServerConfigTemplate template;

    public GameServerConfig() {}

    public GameServerConfig(int serverId, String name, boolean isOnline,
                            boolean isMaster, GameServerConfigTemplate template) {
        this.serverId = serverId;
        this.name = name;
        this.isOnline = isOnline;
        this.isMaster = isMaster;
        this.template = template;
    }

    public int getId() {
        return getServerId();
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GameServerConfigTemplate getTemplate() {
        return template;
    }

    public void setTemplate(GameServerConfigTemplate template) {
        this.template = template;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean isMaster) {
        this.isMaster = isMaster;
    }
}
