package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IAvatar;
import com.betsoft.casino.mp.model.bots.BotConfigInfo;
import com.betsoft.casino.mp.model.bots.TimeFrame;
import com.abs.casino.common.exception.AlreadyExistsException;
import com.dgphoenix.casino.common.exception.ObjectNotFoundException;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * User: flsh
 * Date: 10.11.17.
 */
@SuppressWarnings("rawtypes")
@Service
public class BotConfigInfoService {
    public static final String BOT_CONFIG_INFO_STORE = "botConfigInfoStore";
    public static final String BOT_SERVICE_CONFIG = "botServiceConfig";
    public static final String BOT_SERVICE_ENABLED_KEY = "botServiceEnabled";
    private static final Logger LOG = LogManager.getLogger(BotConfigInfoService.class);
    private static final Long GLOBAL_LOCK = 0L;

    private final HazelcastInstance hazelcast;
    private final IdGenerator idGenerator;
    private final INicknameService nicknameService;

    private IMap<Long, BotConfigInfo> botConfigInfoStore; //key=botId, value=BotConfigInfo
    private IMap<String, String> botServiceConfig; //key,value

    public BotConfigInfoService(HazelcastInstance hazelcast, IdGenerator idGenerator, INicknameService nicknameService) {
        this.hazelcast = hazelcast;
        this.idGenerator = idGenerator;
        this.nicknameService = nicknameService;
    }

    @PostConstruct
    private void init() {
        botConfigInfoStore = hazelcast.getMap(BOT_CONFIG_INFO_STORE);
        botConfigInfoStore.addIndex("id", false);
        botConfigInfoStore.addIndex("bankId", false);
        botConfigInfoStore.addIndex("username", false);
        botConfigInfoStore.addIndex("mqNickname", false);

        botServiceConfig = hazelcast.getMap(BOT_SERVICE_CONFIG);

        LOG.info("init: completed");
    }

    public boolean isBotServiceEnabled() {

        boolean isEnabled = false;

        try {
            if (botServiceConfig != null) {
                String botServiceEnabled = botServiceConfig.get(BOT_SERVICE_ENABLED_KEY);
                if(!StringUtils.isTrimmedEmpty(botServiceEnabled)) {
                    isEnabled = Boolean.parseBoolean(botServiceEnabled);
                }
            }
        } catch (Exception exception) {
            LOG.error("isBotServiceEnabled: Exception", exception);
        }

        return isEnabled;
    }


    public boolean setBotServiceEnabled(String enableStr) {

        LOG.debug("setBotServiceEnabled: enableStr={}", enableStr);

        boolean enable = false;

        if (!StringUtils.isTrimmedEmpty(enableStr)) {
            enable = Boolean.parseBoolean(enableStr);
        }

        return setBotServiceEnabled(enable);
    }

    public boolean setBotServiceEnabled(boolean enabled) {

        LOG.debug("setBotServiceEnabled: enabled={}", enabled);

        boolean enabledOld = false;

        try {
            if (botServiceConfig != null) {
                String botServiceEnabledOld = botServiceConfig.put(BOT_SERVICE_ENABLED_KEY, String.valueOf(enabled));
                LOG.debug("setBotServiceEnabled: botServiceEnabledOld={}", botServiceEnabledOld);
                if(!StringUtils.isTrimmedEmpty(botServiceEnabledOld)) {
                    enabledOld = Boolean.parseBoolean(botServiceEnabledOld);
                }
            }
        } catch (Exception exception) {
            LOG.error("setBotServiceEnabled: enabled={}, Exception", enabled, exception);
        }

        LOG.debug("setBotServiceEnabled: return enabledOld={}", enabledOld);

        return enabledOld;
    }

    public BotConfigInfo get(long botId) {
        return botConfigInfoStore.get(botId);
    }

    public Collection<BotConfigInfo> getAll() {
        return botConfigInfoStore.values();
    }

    public BotConfigInfo getByUserName(String username) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("username").equal(username);
        Collection<BotConfigInfo> values = botConfigInfoStore.values(predicate);
        BotConfigInfo botConfigInfo = CollectionUtils.isEmpty(values) ? null : values.iterator().next();
        LOG.debug("getByUserName: username={}, botConfigInfo={}", username, botConfigInfo);
        return botConfigInfo;
    }

    public BotConfigInfo getByMqNickName(String mqNickname) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("mqNickname").equal(mqNickname);
        Collection<BotConfigInfo> values = botConfigInfoStore.values(predicate);
        BotConfigInfo botConfigInfo = CollectionUtils.isEmpty(values) ? null : values.iterator().next();
        LOG.debug("getByMqNickName: getByMqNickName={}, botConfigInfo={}", mqNickname, botConfigInfo);
        return botConfigInfo;
    }

    public Collection<BotConfigInfo> getByBankId(long bankId) {
        EntryObject object = new PredicateBuilder().getEntryObject();
        final Predicate predicate = object.get("bankId").equal(bankId);
        return botConfigInfoStore.values(predicate);
    }

    private boolean lock() {
        botConfigInfoStore.lock(GLOBAL_LOCK);
        return true;
    }

    private void unlock() {
        botConfigInfoStore.unlock(GLOBAL_LOCK);
    }

    private void assertBotConfigInfo(BotConfigInfo botConfigInfo) throws AlreadyExistsException {
        if (StringUtils.isTrimmedEmpty(botConfigInfo.getUsername())) {
            throw new IllegalArgumentException("username empty");
        }

        if (StringUtils.isTrimmedEmpty(botConfigInfo.getMqNickname())) {
            throw new IllegalArgumentException("mqNickName empty");
        }

        if (botConfigInfo.getBankId() <= 0) {
            throw new IllegalArgumentException("bankId is empty");
        }

        if (CollectionUtils.isEmpty(botConfigInfo.getAllowedGames())) {
            throw new IllegalArgumentException("allowedGames is empty");
        }

        boolean available = nicknameService.isNicknameAvailableGlobally(botConfigInfo.getMqNickname());
        if (!available) {
            throw new AlreadyExistsException("mqNickname already exist=" + botConfigInfo.getMqNickname());
        }

        if (CollectionUtils.isEmpty(botConfigInfo.getAllowedBankIds())) {
            throw new IllegalArgumentException("allowedBankIds is empty");
        }
    }

    public BotConfigInfo create(BotConfigInfo botConfigInfo) throws AlreadyExistsException {
        LOG.debug("create: to create new botConfigInfo={}", botConfigInfo);
        boolean locked = false;
        try {

            assertBotConfigInfo(botConfigInfo);

            locked = lock();

            BotConfigInfo exist = getByUserName(botConfigInfo.getUsername());
            if (exist != null) {
                throw new AlreadyExistsException("Bot with same username already exist, id=" + exist.getId());
            }

            exist = getByMqNickName(botConfigInfo.getMqNickname());
            if (exist != null) {
                throw new AlreadyExistsException("Bot with same username already exist, id=" + exist.getId());
            }

            long newBotId = idGenerator.getNext(BotConfigInfo.class);
            botConfigInfo.setId(newBotId);

            BotConfigInfo botConfigInfoPrev = botConfigInfoStore.put(newBotId, botConfigInfo);
            LOG.debug("create: created successfully botConfigInfoPrev={}, botConfigInfo={}",
                    botConfigInfoPrev, botConfigInfo);

            return botConfigInfo;

        } finally {
            if (locked) {
                unlock();
            }
        }
    }

    public void updateBalance(long id, long mmcBalance, long mqcBalance) throws ObjectNotFoundException {
        LOG.debug("updateBalance: id={}, mmcBalance={}, mqcBalance={}", id, mmcBalance, mqcBalance);
        if (mmcBalance < 0) {
            throw new IllegalArgumentException("mmcBalance must be positive");
        }
        if (mqcBalance < 0) {
            throw new IllegalArgumentException("mqcBalance must be positive");
        }
        boolean locked = false;
        try {
            lock();
            locked = true;
            BotConfigInfo botConfigInfo = get(id);
            if (botConfigInfo == null) {
                throw new ObjectNotFoundException("Bot not found, id=" + id);
            }

            botConfigInfo.setMmcBalance(mmcBalance);
            botConfigInfo.setMqcBalance(mqcBalance);
            botConfigInfoStore.set(id, botConfigInfo);

        } finally {
            if (locked) {
                unlock();
            }
        }
    }

    public void updateTmpExpiresAt(long id, long tmpExpiresAt) throws ObjectNotFoundException {
        LOG.debug("updateTmpExpiresAt: id={}, tmpExpiresAt={}", id, tmpExpiresAt);
        if (tmpExpiresAt < 0) {
            throw new IllegalArgumentException("tmpExpiresAt must be positive");
        }

        boolean locked = false;
        try {
            lock();
            locked = true;
            BotConfigInfo botConfigInfo = get(id);
            if (botConfigInfo == null) {
                throw new ObjectNotFoundException("Bot not found, id=" + id);
            }

            botConfigInfo.setTmpExpiresAt(tmpExpiresAt);
            botConfigInfoStore.set(id, botConfigInfo);

        } finally {
            if (locked) {
                unlock();
            }
        }
    }

    public BotConfigInfo update(long id, Set<GameType> allowedGames, boolean active, String password, String mqNickName,
                                IAvatar avatar, Set<TimeFrame> timeFrames, Set<Long> allowedBankIds,
                                Map<Long, Double> shootsRates, Map<Long, Double> bulletsRates,  Map<Long, Set<Long>> allowedRoomValues) throws ObjectNotFoundException {

        LOG.debug("update: to update id={}, allowedGames={}, active={}, password=*******, mqNickName={}, " +
                        "avatar={}, timeFrames={}, allowedBankIds={}, shootsRates={}, bulletsRates={}, allowedRoomValues={}",
                id, allowedGames, active, mqNickName, avatar, timeFrames, allowedBankIds, shootsRates, bulletsRates, allowedRoomValues);

        boolean locked = false;
        try {

            lock();
            locked = true;

            BotConfigInfo botConfigInfo = get(id);
            if (botConfigInfo == null) {
                throw new ObjectNotFoundException("Bot not found, id=" + id);
            }

            botConfigInfo.setAllowedGames(allowedGames);
            botConfigInfo.setActive(active);
            botConfigInfo.setPassword(password);
            botConfigInfo.setMqNickname(mqNickName);
            botConfigInfo.setAvatar(avatar);
            botConfigInfo.setTimeFrames(timeFrames);
            botConfigInfo.setAllowedBankIds(allowedBankIds);
            botConfigInfo.setShootsRates(shootsRates);
            botConfigInfo.setBulletsRates(bulletsRates);
            if(allowedRoomValues != null) {
                for(Long bankId : allowedRoomValues.keySet()) {
                    botConfigInfo.putAllowedRoomValuesSet(bankId, allowedRoomValues.get(bankId));
                }
            }

            assertBotConfigInfo(botConfigInfo);

            BotConfigInfo botConfigInfoPrev = botConfigInfoStore.put(id, botConfigInfo);

            LOG.debug("create: updated successfully botConfigInfoPrev={} to botConfigInfo={}",
                    botConfigInfoPrev, botConfigInfo);

            return botConfigInfo;

        } catch(Exception e) {
            LOG.error("update: bot id={}, exception:{}", id, e.getMessage(), e);
        } finally {
            if (locked) {
                unlock();
            }
        }

        return null;
    }

    public void remove(Long id) {
        LOG.debug("remove: to remove botConfigInfo with id={}", id);
        botConfigInfoStore.delete(id);
    }
}
