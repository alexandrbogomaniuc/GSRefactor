package com.dgphoenix.casino.cassandra.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * User: flsh
 * Date: 19.10.11
 */
@XStreamAlias("Host")
public class Host {
    @XStreamAsAttribute
    private String name;
    @XStreamAsAttribute
    private int port;

    public Host() {
    }

    public Host(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAsPair() {
        return name + ":" + port;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Host");
        sb.append("[name='").append(name).append('\'');
        sb.append(", port=").append(port);
        sb.append(']');
        return sb.toString();
    }
}
