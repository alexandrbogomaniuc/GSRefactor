package com.betsoft.casino.mp.server;

public class ZookeeperProperties {
    private String connect;
    private int poolSize;
    private long ttlMillis;
    private long heartbeatInterval;
    private String servicePath;
    private String poolPath;

    public ZookeeperProperties(String connect,
                               int poolSize,
                               long ttlMillis,
                               long heartbeatInterval,
                               String servicePath,
                               String poolPath) {
        this.connect = connect;
        this.poolSize = poolSize;
        this.ttlMillis = ttlMillis;
        this.heartbeatInterval = heartbeatInterval;
        this.servicePath = servicePath;
        this.poolPath = poolPath;
    }

    // Getters & Setters
    public String getConnect() { return connect; }
    public void setConnect(String connect) { this.connect = connect; }

    public int getPoolSize() { return poolSize; }
    public void setPoolSize(int poolSize) { this.poolSize = poolSize; }

    public long getTtlMillis() { return ttlMillis; }
    public void setTtlMillis(long ttlMillis) { this.ttlMillis = ttlMillis; }

    public long getHeartbeatInterval() { return heartbeatInterval; }
    public void setHeartbeatInterval(long heartbeatInterval) { this.heartbeatInterval = heartbeatInterval; }

    public String getServicePath() { return servicePath; }
    public void setServicePath(String servicePath) { this.servicePath = servicePath; }

    public String getPoolPath() { return poolPath; }
    public void setPoolPath(String poolPath) { this.poolPath = poolPath; }
}
