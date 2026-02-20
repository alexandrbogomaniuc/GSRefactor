package com.dgphoenix.casino.kafka.dto;

public class GameServerInfoDto {
    public int id;
    public String host;
    public String domain;

    public GameServerInfoDto() {}

    public GameServerInfoDto(int id, String host, String domain) {
        this.id = id;
        this.host = host;
        this.domain = domain;
    }

    public int getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public String getDomain() {
        return domain;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
