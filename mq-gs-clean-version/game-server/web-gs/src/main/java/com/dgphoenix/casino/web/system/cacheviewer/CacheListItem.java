package com.dgphoenix.casino.web.system.cacheviewer;

/**
 * User: turubarov
 * Date: 28.02.2018
 */
public class CacheListItem {
    private String key;
    private String value;
    private String description;

    public CacheListItem(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}