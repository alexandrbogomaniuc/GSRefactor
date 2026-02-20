package com.betsoft.casino.mp.dragonstone.model.math.config;

import com.google.gson.Gson;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class SpawnConfigLoader {
    private static final String DEFAULT_CONFIG = "classpath:models/dragonstone/spawn.json";

    private final Gson gson = new Gson();

    public SpawnConfig loadDefaultConfig() {
        Resource resource = new PathMatchingResourcePatternResolver().getResource(DEFAULT_CONFIG);
        try {
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            return gson.fromJson(reader, SpawnConfig.class);
        } catch (IOException e) {
            return null;
        }
    }

    public SpawnConfig parseConfig(String configString) {
        return gson.fromJson(configString, SpawnConfig.class);
    }
}
