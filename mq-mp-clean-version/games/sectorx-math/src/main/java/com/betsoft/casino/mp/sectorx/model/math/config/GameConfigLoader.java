package com.betsoft.casino.mp.sectorx.model.math.config;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class GameConfigLoader {
    private static final String DEFAULT_CONFIG = "classpath:models/sectorx/math.json";

    private final Gson gson = new Gson();

    private static final Logger logger = LogManager.getLogger(GameConfigLoader.class);

    public GameConfig loadDefaultConfig() {
        Resource resource = new PathMatchingResourcePatternResolver().getResource(DEFAULT_CONFIG);

        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, GameConfig.class);
        } catch (IOException e) {
            logger.error("Failed to read the GameConfig", e);
            return null;
        }
    }

    public GameConfig parseConfig(String configString) {
        return gson.fromJson(configString, GameConfig.class);
    }
}
