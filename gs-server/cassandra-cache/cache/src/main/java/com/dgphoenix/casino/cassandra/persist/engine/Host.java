package com.abs.casino.cassandra.persist.engine;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Objects;

public final class Host {
    private final com.datastax.driver.core.Host host;

    private Host(com.datastax.driver.core.Host host) {
        this.host = host;
    }

    public static Host wrap(com.datastax.driver.core.Host host) {
        return host == null ? null : new Host(host);
    }

    public boolean isUp() {
        return host.isUp();
    }

    public String getDatacenter() {
        return host.getDatacenter();
    }

    public SocketAddress getSocketAddress() {
        return host.getSocketAddress();
    }

    public InetAddress getAddress() {
        return host.getAddress();
    }

    com.datastax.driver.core.Host unwrap() {
        return host;
    }

    @Override
    public String toString() {
        return String.valueOf(host);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(host);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Host)) {
            return false;
        }
        Host other = (Host) obj;
        return Objects.equals(host, other.host);
    }
}
