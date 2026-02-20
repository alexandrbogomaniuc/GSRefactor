package com.dgphoenix.casino.common.vault;

import java.util.List;

/**
 * Created by nkurtushin on 30.05.17.
 */
public class GameEntry {
    private final long id;
    private final String externalId;
    private final String name;
    private final String icon;
    private final String platform;
    private final String location;
    private final List<String> languages;

    public GameEntry(long id, String externalId, String name, String icon, String platform, String location,
                     List<String> languages) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.icon = icon;
        this.platform = platform;
        this.location = location;
        this.languages = languages;
    }

    public long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getPlatform() {
        return platform;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getLanguages() {
        return languages;
    }

}
