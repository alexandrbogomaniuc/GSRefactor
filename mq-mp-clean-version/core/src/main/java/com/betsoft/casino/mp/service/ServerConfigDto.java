package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IServerConfig;

import java.util.Objects;

/**
 * User: flsh
 * Date: 20.11.17.
 */
public class ServerConfigDto implements IServerConfig {
    private int id;
    private String label;
    private String oldHost;
    private String domain;
    private String serverIdentifier;
    private String serverIP;
    private boolean online = false;
    private boolean isMaster = false;

    public ServerConfigDto() {}

    public ServerConfigDto(int id) {
        this.id = id;
        this.label = "MP_" + id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getHost() {
        return oldHost;
    }

    @Override
    public void setOldHost(String host) {
        this.oldHost = host;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getServerIdentifier() {
        return serverIdentifier;
    }

    public void setServerIdentifier(String serverIdentifier) {
        this.serverIdentifier = serverIdentifier;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    @Override
    public boolean isMaster() {
        return isMaster;
    }

    public void setIsMaster(boolean isMaster) {
        this.isMaster = isMaster;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerConfigDto that = (ServerConfigDto) o;
        return id == that.id &&
                Objects.equals(label, that.label) &&
                Objects.equals(oldHost, that.oldHost) &&
                Objects.equals(domain, that.domain);
    }

    @Override
    public boolean isLocalDevAllowed(){
        if (domain == null) {
            return false;
        }
        return domain.equals("-gp3.dgphoenix.com")
                || domain.equals(".local")
                || domain.endsWith(".local")
                || domain.endsWith(".mydomain");
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServerConfig [");
        sb.append("id=").append(id);
        sb.append(", label='").append(label).append('\'');
        sb.append(", host='").append(oldHost).append('\'');
        sb.append(", domain='").append(domain).append('\'');
        sb.append(']');
        return sb.toString();
    }

}
