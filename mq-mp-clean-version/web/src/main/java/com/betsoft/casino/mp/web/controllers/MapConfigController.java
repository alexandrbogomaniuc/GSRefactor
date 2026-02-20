package com.betsoft.casino.mp.web.controllers;

import com.betsoft.casino.mp.common.GameMapStore;
import com.betsoft.casino.mp.data.persister.MapConfigPersister;
import com.betsoft.casino.mp.model.IMapConfigEntity;
import com.betsoft.casino.mp.model.MapConfigEntity;
import com.betsoft.casino.mp.model.gameconfig.IMapConfig;
import com.betsoft.casino.mp.service.IMapConfigService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.web.service.RefreshMapConfigTask;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/support/mapConfig")
@CrossOrigin(origins = "*")
public class MapConfigController {
    private static final Logger LOG = LogManager.getLogger(MapConfigController.class);

    private final IMapConfigService configService;
    private final GameMapStore mapStore;
    private final RoomPlayerInfoService playerInfoService;

    public MapConfigController(CassandraPersistenceManager cpm, RoomPlayerInfoService playerInfoService,
                               GameMapStore mapStore) {
        this.configService = cpm.getPersister(MapConfigPersister.class);
        this.mapStore = mapStore;
        this.playerInfoService = playerInfoService;
    }

    @GetMapping(value = "/{mapId}", produces = "application/json")
    public ResponseEntity<Object> getConfig(@PathVariable int mapId) {
        return new ResponseEntity<>(mapStore.getMapConfig(mapId), HttpStatus.OK);
    }

    @GetMapping("/{mapId}/date")
    public ResponseEntity<String> getUploadDate(@PathVariable int mapId) {
        IMapConfigEntity entity = configService.load(mapId);
        return new ResponseEntity<>(entity == null ? "Default" : entity.getUploadDate(), HttpStatus.OK);
    }

    @PostMapping("/{mapId}/add")
    public ResponseEntity<Void> addMapConfig(@PathVariable int mapId, @RequestBody IMapConfig config) {
        LOG.info("Adding map config for map: {}", mapId);
        LocalDateTime date = LocalDateTime.now();
        MapConfigEntity configEntity = new MapConfigEntity(date.toString(), config);
        configService.save(mapId, configEntity);
        playerInfoService.getNotifyService().submitToAllMembers(new RefreshMapConfigTask(mapId));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{mapId}/restore")
    public ResponseEntity<Void> restoreConfig(@PathVariable int mapId) {
        configService.removeConfig(mapId);
        playerInfoService.getNotifyService().submitToAllMembers(new RefreshMapConfigTask(mapId));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
