package com.betsoft.casino.mp.config;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.MoneyType;
import com.betsoft.casino.mp.model.RoomTemplate;
import com.betsoft.casino.mp.service.IdGenerator;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.RoomTemplateService;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.betsoft.casino.mp.model.GameType.*;

/**
 * User: flsh
 * Date: 21.11.17.
 */
public class SampleDataInitializer {
    private static final Logger LOG = LogManager.getLogger(SampleDataInitializer.class);
    private final static int NORMAL_DURATION = 290;

    @Autowired
    private ServerConfigService serverConfigService;

    @Autowired
    private RoomPlayerInfoService playerInfoService;

    @Autowired
    private RoomServiceFactory roomServiceFactory;

    @Autowired
    private RoomTemplateService roomTemplateService;

    @Autowired
    private SocketService socketService;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private CassandraPersistenceManager persistenceManager;

    @PostConstruct
    private void init() throws CommonException {
        LOG.debug("**************************** initSampleData: serverConfigService=" + serverConfigService);
        LOG.debug("**************************** initSampleData: playerInfoService=" + playerInfoService);
        LOG.debug("**************************** initSampleData: roomTemplateService=" + roomTemplateService);
        Collection<RoomTemplate> templates = initDefaultRoomTemplates();
/*
        Collection<IRoomInfo> allRooms = initDemoRoomInfo(templates);
        Collection<IRoom> rooms = roomServiceFactory.getRooms(GameType.UNDISCOVERED_EGYPT);
        if (rooms.isEmpty()) {
            initDemoRoomsForEgypt(allRooms);
        }
*/
    }

    private void create(GameType gameType, MoneyType moneyType, int minBuyIn, int initialRooms, int minFreeRooms,
                        int maxRooms, String name, int roundDuration) {
        roomTemplateService.put(new RoomTemplate(idGenerator.getNext(RoomTemplate.class),
                RoomTemplate.DEFAULT_BANK_ID, gameType, gameType.getMaxSeats(),
                gameType.getMinSeats(), moneyType,
                gameType.getScreenWidth(), gameType.getScreenHeight(),
                minBuyIn, initialRooms, minFreeRooms, maxRooms, name, roundDuration));
    }

    private void createTemplates(GameType gameType) {
        String mpStress = System.getProperty("mp_stress");
        boolean stressMode = !StringUtils.isTrimmedEmpty(mpStress) && mpStress.equalsIgnoreCase(Boolean.TRUE.toString());
        int initialRooms = gameType.isCrashGame() ? 1 : 5;
        int minFreeRooms = 2;
        int maxRooms = 200;
        if (stressMode) {
            LOG.warn("Server in stress mode, try load property 'mp_stress_room_multiplier'");
            String mpStressRoomMultiplier = System.getProperty("mp_stress_room_multiplier");
            if (!StringUtils.isTrimmedEmpty(mpStressRoomMultiplier)) {
                int multiplier = Integer.parseInt(mpStressRoomMultiplier);
                initialRooms = initialRooms * multiplier;
                minFreeRooms = minFreeRooms * multiplier;
                maxRooms = maxRooms * multiplier;
                LOG.warn("Found 'mp_stress_room_multiplier', multiplier={}, initialRooms={}, minFreeRooms={}, " +
                        "maxRooms={}", multiplier, initialRooms, minFreeRooms, maxRooms);
            }
        }
        create(gameType, MoneyType.REAL, 1, initialRooms, minFreeRooms, maxRooms, "Normal room",
                NORMAL_DURATION);
        create(gameType, MoneyType.FREE, 1, initialRooms, minFreeRooms, maxRooms, "Normal room",
                NORMAL_DURATION);
    }

    private Collection<RoomTemplate> initDefaultRoomTemplates() {
        Collection<RoomTemplate> templates = roomTemplateService.getDefault(MoneyType.REAL);
        if (templates.isEmpty()) {
            LOG.info("initDefaultRoomTemplates: templates not found, create");
            createTemplates(PIRATES);
            createTemplates(AMAZON);
            createTemplates(PIRATES_POV);
            createTemplates(REVENGE_OF_RA);
            createTemplates(DRAGONSTONE);
            createTemplates(CLASH_OF_THE_GODS);
            createTemplates(DMC_PIRATES);
            createTemplates(BG_DRAGONSTONE);
            createTemplates(MISSION_AMAZON);
            createTemplates(MAXCRASHGAME);
            createTemplates(BG_MISSION_AMAZON);
            createTemplates(SECTOR_X);
            createTemplates(TRIPLE_MAX_BLAST);
            createTemplates(LUNARCASH);
            createTemplates(BG_MAXCRASHGAME);
            createTemplates(BG_SECTOR_X);

            templates = roomTemplateService.getDefault(MoneyType.REAL);
            LOG.info("initDefaultRoomTemplates: created {} templates", templates.size());
        } else {
            LOG.info("initDefaultRoomTemplates: found {} templates", templates.size());
            List<GameType> allGames = new ArrayList<>(Arrays.asList(values()));
            templates.forEach(roomTemplate -> allGames.remove(roomTemplate.getGameType()));
            allGames.forEach(this::createTemplates);
        }

        return templates;
    }
}
