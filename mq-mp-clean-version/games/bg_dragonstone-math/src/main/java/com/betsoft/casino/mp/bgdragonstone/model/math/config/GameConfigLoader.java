package com.betsoft.casino.mp.bgdragonstone.model.math.config;

import com.google.gson.Gson;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class GameConfigLoader {
    private static final String DEFAULT_CONFIG = "classpath:models/bg_dragonstone/math.json";

    private final Gson gson = new Gson();

    public GameConfig loadDefaultConfig() {
        Resource resource = new PathMatchingResourcePatternResolver().getResource(DEFAULT_CONFIG);
        try {
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            return gson.fromJson(reader, GameConfig.class);
        } catch (IOException e) {
            return null;
        }
    }

    public GameConfig parseConfig(String configString) {
        return gson.fromJson(configString, GameConfig.class);
    }
}
