package com.abs.casino.cassandra.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * User: Grien
 * Date: 28.12.2012 11:15
 */
@XStreamAlias("ColumnFamily")
public class ColumnFamilyConfig {
    private String className;
    private Integer ttl;
    private boolean enabled;

    ColumnFamilyConfig(String className, Integer ttl, boolean enabled) {
        this.className = className;
        this.ttl = ttl;
        this.enabled = enabled;
    }

    public String getClassName() {
        return className;
    }

    public Integer getTtl() {
        return ttl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ColumnFamilyConfig");
        sb.append("[className='").append(className).append('\'');
        sb.append(", ttl=").append(ttl);
        sb.append(", enabled=").append(enabled);
        sb.append(']');
        return sb.toString();
    }
}
