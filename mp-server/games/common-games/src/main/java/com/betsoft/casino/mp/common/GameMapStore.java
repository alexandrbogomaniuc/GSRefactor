package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IMapConfigEntity;
import com.betsoft.casino.mp.service.IMapConfigService;
import com.abs.casino.common.util.RNG;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kynosarges.tektosyne.geometry.PointD;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.annotation.PostConstruct;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class GameMapStore {
    private static final Logger LOG = LogManager.getLogger(GameMapStore.class);

    private final IMapConfigService configService;

    private final Gson gson = new Gson();
    private final Map<Integer, GameMapMeta> defaultConfigs = new HashMap<>();
    private final Map<Integer, GameMapShape> maps = new HashMap<>();

    public GameMapStore() {
        this.configService = null;
    }

    public GameMapStore(IMapConfigService configService) {
        this.configService = configService;
    }

    @PostConstruct
    public void init() {
        ClassLoader classLoader = GameMapStore.class.getClassLoader();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        try {
            for (Resource resource : resolver.getResources("classpath:maps/**/*.map")) {
                String filename = resource.getFilename();
                if (filename != null) {
                    try {
                        maps.put(getKey(filename), new GameMapShape(resource.getFilename(),
                                new DataInputStream(resource.getInputStream())));
                    } catch (Exception e) {
                        LOG.error("Failed to load map " + filename);
                    }
                }
            }
            for (Resource resource : resolver.getResources("classpath:maps/**/*.json")) {
                String filename = resource.getFilename();
                if (filename != null) {
                    try {
                        int mapId = getKey(filename);
                        GameMapShape map = maps.get(mapId);
                        if (map != null) {
                            GameMapMeta meta = loadMeta(resource);
                            defaultConfigs.put(mapId, meta);
                            if (configService != null) {
                                IMapConfigEntity entity = configService.load(mapId);
                                if (entity != null) {
                                    meta = (GameMapMeta) entity.getConfig();
                                }
                            }
                            setMapProperties(map, meta, mapId);
                        } else {
                            LOG.error("Found map meta without a shape {}", filename);
                        }
                    } catch (Exception e) {
                        LOG.error("Failed to parse meta for map {}", filename, e);
                    }
                }
            }
        } catch (IOException e) {
            LOG.error("Failed to load maps", e);
        }
    }

    private void setMapProperties(GameMapShape map, GameMapMeta meta, int mapId) {
        map.setBossSpawnPoint(meta.getBossSpawnPoint());
        map.setSwarmSpawnParams(meta.getSwarmSpawnParams());
        map.setTrajectories(meta.getTrajectories() != null
                ? meta.getTrajectories()
                : new HashMap<>());
        map.setPredefinedTrajectories(meta.getPredefinedTrajectories() != null
                ? meta.getPredefinedTrajectories()
                : new HashMap<>());
        map.setPredefinedTrajectoryIds(meta.getPredefinedTrajectoryIds() != null
                ? meta.getPredefinedTrajectoryIds()
                : new HashMap<>());
        map.setScenarios(meta.getScenarios() != null ? meta.getScenarios() : new ArrayList<>());
        map.setSwarmParams(meta.getSwarmParams() != null ? meta.getSwarmParams() : new ArrayList<>());
        map.setPortals(meta.getPortals() != null ? meta.getPortals() : new ArrayList<>());
        map.setDisableRandomTrajectories(meta.isDisableRandomTrajectories());
        map.setCenter(meta.getCenter() != null ? meta.getCenter() : new PointD());
        map.setId(mapId);
        map.setPoints(meta.getPoints() != null ? meta.getPoints() : new HashMap<>());
        map.setPredefinedGroups(meta.getPredefinedGroups() != null ? meta.getPredefinedGroups() : new HashMap<>());
    }

    public int getKey(String filename) {
        return Integer.parseInt(filename.substring(0, filename.indexOf('.')));
    }

    public GameMapMeta loadMeta(Resource resource) throws IOException {
        String content = Resources.toString(resource.getURL(), Charsets.UTF_8);
        return gson.fromJson(content, GameMapMeta.class);
    }

    public GameMapShape getStartMap(GameType gameType) {
        int mapId = gameType.getMaps().get(RNG.nextInt(gameType.getMaps().size()));
        return maps.get(mapId);
    }

    public GameMapShape getMap(int key) {
        return maps.get(key);
    }

    public void updateMeta(int mapId) {
        GameMapMeta meta = defaultConfigs.get(mapId);
        if (configService != null) {
            IMapConfigEntity entity = configService.load(mapId);
            if (entity != null) {
                meta = (GameMapMeta) entity.getConfig();
            }
        }
        setMapProperties(maps.get(mapId), meta, mapId);
    }

    public Set<Integer> getMapIds() {
        return maps.keySet();
    }

    public GameMapMeta getMapConfig(int mapId) {
        if (configService != null) {
            IMapConfigEntity entity = configService.load(mapId);
            if (entity != null) {
                return (GameMapMeta) entity.getConfig();
            }
        }
        return defaultConfigs.get(mapId);
    }
}
