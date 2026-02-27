package com.dgphoenix.casino.gs.managers.payment.bonus;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraBonusArchivePersister;
import com.dgphoenix.casino.cassandra.persist.CassandraBonusPersister;
import com.dgphoenix.casino.cassandra.persist.CassandraTrackingInfoPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.*;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.GameType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.FRBException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.transactiondata.TransactionData;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.IdGenerator;
import com.dgphoenix.casino.common.util.ReflectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.bonus.BonusError;
import com.dgphoenix.casino.common.web.bonus.BonusErrors;
import com.dgphoenix.casino.gs.GameServer;
import com.dgphoenix.casino.gs.managers.payment.bonus.mass.MassAwardBonusManager;
import com.abs.casino.gs.managers.payment.bonus.tracker.BonusTracker;
import com.dgphoenix.casino.gs.persistance.GameSessionPersister;
import com.dgphoenix.casino.gs.persistance.LasthandPersister;
import com.dgphoenix.casino.gs.persistance.PlayerSessionPersister;
import com.dgphoenix.casino.gs.persistance.remotecall.KafkaRequestMultiPlayer;
import com.dgphoenix.casino.kafka.dto.BonusStatusDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BonusManager implements IBonusManager {
    private static final Logger LOG = LogManager.getLogger(BonusManager.class);
    private ConcurrentMap<Long, IBonusClient> clients = new ConcurrentHashMap<>();
    private final CassandraBonusPersister bonusPersister;
    private final CassandraTrackingInfoPersister trackingInfoPersister;
    private final CassandraBonusArchivePersister bonusArchivePersister;
    private final KafkaRequestMultiPlayer kafkaRequestMultiPlayer;
    private final MassAwardBonusManager massAwardBonusManager;

    public BonusManager(CassandraPersistenceManager cpm,
                        KafkaRequestMultiPlayer kafkaRequestMultiPlayer,
                        MassAwardBonusManager massAwardBonusManager) {
        bonusPersister = cpm.getPersister(CassandraBonusPersister.class);
        trackingInfoPersister = cpm.getPersister(CassandraTrackingInfoPersister.class);
        bonusArchivePersister = cpm.getPersister(CassandraBonusArchivePersister.class);
        this.kafkaRequestMultiPlayer = kafkaRequestMultiPlayer;
        this.massAwardBonusManager = massAwardBonusManager;
    }

    /**
     * Only for backward compatibility
     *
     * @deprecated Use getting from context instead
     */
    @Deprecated
    public static BonusManager getInstance() {
        return ApplicationContextHelper.getApplicationContext().getBean(BonusManager.class);
    }

    protected synchronized IBonusClient instantiateClient(long bankId) throws BonusException {
        String klazz = BankInfoCache.getInstance().getBankInfo(bankId).getBonusRequestClientClass();
        if (StringUtils.isTrimmedEmpty(klazz)) {
            LOG.error("instantiateClient bankId:{} can't instantiate client", bankId);
            return null;
        }
        try {
            Class<?> aClass = ReflectionUtils.forNameWithCompatibilityAliases(klazz);
            Constructor<?> clientConstructor = aClass.getConstructor(long.class);
            IBonusClient client = (IBonusClient) clientConstructor.newInstance(bankId);
            IBonusClient existClient = clients.putIfAbsent(bankId, client);
            return existClient != null ? existClient : client;
        } catch (Exception e) {
            LOG.error(this.getClass().getSimpleName() + "::instantiateClient bankId:" + bankId +
                    " can't instantiate client", e);
            throw new BonusException(e);
        }
    }

    @Override
    public IBonusClient getClient(long bankId) throws BonusException {
        IBonusClient client = clients.get(bankId);
        return client == null ? instantiateClient(bankId) : client;
    }

    public Bonus composeBonus(long accountId, long bankId, BonusType type, long amount,
                              double rolloverMultiplier, String extId, List<Long> gameIds,
                              String description, String comment, long expirationDate,
                              long timeAwarded, BonusGameMode bonusGameMode, boolean autoRelease, Long startDate,
                              Double maxWinMultiplier) {
        long bonusId = GameServer.getInstance().getIdGenerator().getNext(Bonus.class);
        Long maxWinLimit = null;
        if (maxWinMultiplier != null && maxWinMultiplier > 0) {
            maxWinLimit = Math.round(maxWinMultiplier * amount);
        }

        return new Bonus(bonusId, accountId, bankId, type, amount, rolloverMultiplier, extId, gameIds, description,
                comment, expirationDate, timeAwarded, null, amount, 0, BonusStatus.ACTIVE,
                bonusGameMode, autoRelease, startDate, maxWinLimit);
    }

    public void flush(Bonus bonus) {
        bonus.incrementVersion();
        save(bonus);
    }

    protected void assertCheckBankProperty(long bankId) throws BonusException {
        if (!BankInfoCache.getInstance().getBankInfo(bankId).isBonusConfigurationValid()) {
            String message = "Bonus configuration is invalid for bankId=" + bankId;
            LOG.error(message);
            throw new BonusException(message);
        }

        try {
            instantiateClient(bankId);
        } catch (FRBException ex) {
            LOG.error("IBonusClient not found bankId={}", bankId, ex);
            throw new BonusException("IBonusClient not found", ex);
        }
    }

    @Override
    public Bonus awardBonus(AccountInfo accountInfo, BonusType type, long amount, double multiplier,
                            String extId, List<Long> gameIds, BonusGameMode bonusGameMode, String description,
                            String comment, long expirationDate, long timeAwarded, boolean internal, boolean mass,
                            boolean autoRelease, Long startDate, Double maxWinMultiplier)
            throws BonusException {
        try {
            assertCheckBankProperty(accountInfo.getBankId());
            Bonus bonus = composeBonus(accountInfo.getId(), accountInfo.getBankId(), type, amount, multiplier, extId,
                    gameIds, description, comment, expirationDate, timeAwarded, bonusGameMode, autoRelease, startDate,
                    maxWinMultiplier);
            bonus.setInternal(internal);
            if (!mass) {
                save(bonus);
            } else {
                save(bonus);
            }
            return bonus;
        } catch (Exception e) {
            throw new BonusException("failed awardBonus accountId=" + accountInfo.getId(), e);
        }
    }

    @Override
    public Bonus getById(long id) {
        ITransactionData td = SessionHelper.getInstance().getTransactionData();
        if (td != null && td.getBonus() != null && td.getBonus().getId() == id) {
            return td.getBonus();
        }
        return bonusPersister.get(id);
    }

    public Bonus getArchivedBonusById(long id) {
        return bonusArchivePersister.get(id);
    }

    private boolean internalChangeBonusStatus(Bonus bonus, BonusStatus status) throws BonusException {
        boolean result;
        boolean transactionAlreadyStarted;
        try {
            transactionAlreadyStarted = SessionHelper.getInstance().isTransactionStarted();
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().lock(bonus.getAccountId());
            }
        } catch (CommonException e) {
            throw new BonusException("failed cancelBonus bonus=" + bonus, e);
        }
        try {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().openSession();
            }
            Bonus changingBonus = bonus;
            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
            Bonus onlineBonus = transactionData == null ? null : transactionData.getBonus();
            if (onlineBonus != null && onlineBonus.getId() == bonus.getId()) {
                onlineBonus.setStatusAndEndTime(status, System.currentTimeMillis());
                changingBonus = onlineBonus;
                GameSession gameSession = transactionData.getGameSession();
                if (gameSession != null) {
                    long gameId = gameSession.getGameId();
                    BaseGameInfoTemplate templateById = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
                    if (templateById.getDefaultGameInfo().getGameType().equals(GameType.MP)) {
                        sentNotifyToMQ(bonus, status, transactionData);
                    }
                }
            } else if (transactionData != null) {
                sentNotifyToMQ(bonus, status, transactionData);
            }

            result = changeBonusStatus(changingBonus, status);
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            }
        } catch (Exception e) {
            throw new BonusException("failed change status for bonus=" + bonus.getId() + ", new status=" + status, e);
        } finally {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }
        return result;
    }

    private void sentNotifyToMQ(Bonus bonus, BonusStatus status, ITransactionData transactionData) throws Exception {
        if (transactionData == null) {
            return;
        }
        GameSession gameSession = transactionData.getGameSession();
        if (gameSession != null) {
            BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().
                    getBaseGameInfoTemplateById(gameSession.getGameId());
            if (template == null || !template.isMultiplayerGame()) {
                return;
            }
        }

        LOG.debug("sentNotifyToMQ, status: {}, bonus.getId(): {} ", status, bonus.getId());
        kafkaRequestMultiPlayer.sendChangeBonusStatusToMQ(new BonusStatusDto(
                status.name(),
                bonus.getId(),
                bonus.getAccountId()
        ));
    }

    @Override
    public boolean cancelBonus(Bonus bonus) throws BonusException {
        return internalChangeBonusStatus(bonus, BonusStatus.CANCELLED);
    }

    @Override
    public boolean lostBonus(Bonus bonus) throws BonusException {
        return internalChangeBonusStatus(bonus, BonusStatus.LOST);
    }

    @Override
    public boolean expireBonus(Bonus bonus) throws BonusException {
        return internalChangeBonusStatus(bonus, BonusStatus.EXPIRED);
    }

    protected boolean changeBonusStatus(Bonus bonus, BonusStatus status) {
        if (bonus != null) {
            bonus.setStatusAndEndTime(status, System.currentTimeMillis());
            AccountInfo account = AccountManager.getInstance().getAccountInfo(bonus.getAccountId());
            boolean isLive = markGameSession(bonus);
            removeLastHands(account, bonus.getId(), isLive);
            flush(bonus);
            return true;
        } else {
            LOG.warn("changeBonusStatus, bonus is null, status={}", status);
        }
        return false;
    }

    protected void removeLastHands(AccountInfo accountInfo, long bonusId, boolean isLive) {
        LasthandPersister.getInstance().clearAllForBonus(accountInfo, bonusId, BonusSystemType.ORDINARY_SYSTEM, isLive);
    }

    protected boolean markGameSession(Bonus bonus) {
        GameSession gameSession = findLiveGameSessionForBonus(bonus);
        if (gameSession != null) {
            gameSession.setEndBonusBalance(bonus.getBalance());
            gameSession.setBonusStatus(bonus.getStatus());
            return true;
        }
        return false;
    }

    protected GameSession findLiveGameSessionForBonus(Bonus bonus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findLiveGameSessionForBonus {}", bonus.getId());
        }
        SessionInfo sessionInfo = PlayerSessionPersister.getInstance().getSessionInfo();
        if (sessionInfo != null && sessionInfo.getGameSessionId() != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("findLiveGameSessionForBonus sessionInfo {}", sessionInfo);
            }
            GameSession gameSession = null;
            try {
                gameSession = GameSessionPersister.getInstance().getGameSession(sessionInfo.getGameSessionId());
            } catch (CommonException e) {
                LOG.error(e);
            }
            if (gameSession != null && gameSession.isBonusGameSession() && gameSession.getBonusId() == bonus.getId()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("findLiveGameSessionForBonus gameSession {}", gameSession);
                }
                return gameSession;
            }
        }
        return null;
    }

    @Override
    public void releaseBonus(Bonus bonus) throws BonusException {
        if (bonus == null) {
            throw new BonusException("Bonus is null");
        }
        long bonusId = bonus.getId();
        try {
            boolean isRegisteredForTracking = isRegisteredForTracking(bonusId);
            boolean autoReleased = bonus.isAutoReleased();
            if (!isRegisteredForTracking && autoReleased) {
                registerForTracking(bonusId);
            }
            AccountInfo account = AccountManager.getInstance().getAccountInfo(bonus.getAccountId());
            boolean bonusStatusChanged = !bonus.getStatus().equals(BonusStatus.RELEASING);
            if (bonusStatusChanged) {
                bonus.setStatus(BonusStatus.RELEASING);
                bonus.setEndTime(System.currentTimeMillis());
            }
            boolean isLive = markGameSession(bonus);
            if (bonusStatusChanged) {
                removeLastHands(account, bonusId, isLive);
            }
            if (autoReleased) {
                releaseBonus(bonus, account.getBankId(), account.getExternalId());
            }
            if (!isRegisteredForTracking && autoReleased) {
                unregisterFromTracking(bonusId);
            }
            flush(bonus);
        } catch (BonusException be) {
            BonusTracker.getInstance().addTask(bonusId);
            throw be;
        } catch (Throwable e) {
            BonusTracker.getInstance().addTask(bonusId);
            throw new BonusException("failed releaseBonus bonusId=" + bonusId, e);
        }
    }

    @Override
    public void releaseBonusManually(long bonusId) throws CommonException {
        Bonus bonus = getById(bonusId);
        if (bonus == null) {
            LOG.warn("Not found bonus with id: {}, possibly already released, just return", bonusId);
            return;
        }

        long accountId = bonus.getAccountId();
        AccountInfo account = AccountManager.getInstance().getAccountInfo(accountId);
        int bankId = account.getBankId();
        String externalId = account.getExternalId();
        SessionHelper.getInstance().lock(bankId, externalId);
        try {
            SessionHelper.getInstance().openSession();

            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
            Bonus currentBonus = transactionData.getBonus();
            boolean modifiedCurrentBonus = false;
            if (currentBonus != null && currentBonus.getId() == bonusId) {
                bonus = currentBonus;
                modifiedCurrentBonus = true;
            }

            releaseBonus(bonus, bankId, externalId);
            flush(bonus);

            if (modifiedCurrentBonus) {
                SessionHelper.getInstance().commitTransaction();
            }

            SessionHelper.getInstance().markTransactionCompleted();
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    }

    private void releaseBonus(Bonus bonus, long bankId, String externalAccountId) throws CommonException {
        cutOffLimitedAmount(bonus);
        getClient(bankId).bonusRelease(bonus, externalAccountId);
        bonus.setStatus(BonusStatus.RELEASED);
    }

    private void cutOffLimitedAmount(Bonus bonus) {
        if (bonus.getBonusEndBalance() == null) {
            bonus.setBonusEndBalance(bonus.getBalance());
        }
        Long maxWinLimit = bonus.getMaxWinLimit();
        if (maxWinLimit != null && maxWinLimit < bonus.getBalance()) {
            bonus.setBalance(bonus.getMaxWinLimit());
        }
    }

    protected boolean isRegisteredForTracking(long bonusId) {
        return trackingInfoPersister.isTracking(BonusTracker.TRACKER_NAME, bonusId);
    }

    protected void registerForTracking(long bonusId) {
        trackingInfoPersister.persist(BonusTracker.TRACKER_NAME, bonusId);
        LOG.debug("registerForTracking bonusId:{} registered for tracking", bonusId);
    }

    protected void unregisterFromTracking(long bonusId) {
        trackingInfoPersister.delete(BonusTracker.TRACKER_NAME, bonusId);
        LOG.debug("unregisterFromTracking bonusId:{} unregistered from tracking", bonusId);
    }

    @Override
    public Bonus get(long bankId, String extId) throws BonusException {
        try {
            return bonusPersister.getByCompositeKey(bankId, extId);
        } catch (Exception e) {
            throw new BonusException("failed getBonusByExtId extId=" + extId, e);
        }
    }

    public List<Bonus> getArchivedBonusesByExtId(long bankId, String bonusExtId) {
        return bonusArchivePersister.getByCompositeKey(bankId, bonusExtId);
    }

    @Override
    public List<Bonus> getFinishedBonuses(AccountInfo accountInfo) throws BonusException {
        try {
            return bonusPersister.getFinishedBonusList(accountInfo.getId());
        } catch (Exception e) {
            throw new BonusException("exception in db", e);
        }
    }

    @Override
    public List<Bonus> getActiveBonuses(AccountInfo accountInfo) {
        List<Bonus> bonuses = bonusPersister.getActiveBonuses(accountInfo.getId());
        List<Bonus> activeNotExpired = new ArrayList<>(bonuses.size());
        for (Bonus bonus : bonuses) {
            if (bonus.isExpired()) {
                try {
                    expireBonus(bonus);
                } catch (BonusException e) {
                    LOG.error("getActiveBonuses: cannot expire bonus={}", bonus, e);
                }
            } else if (bonus.getStatus() == BonusStatus.ACTIVE && bonus.isReady()) {
                activeNotExpired.add(bonus);
            }
        }
        return activeNotExpired;
    }

    public List<Long> getBonusIdsForAccount(long accountId) {
        List<Long> bonusIdsList = new LinkedList<>();
        List<Bonus> bonusList = bonusPersister.getActiveBonuses(accountId);
        for (Bonus bonus : bonusList) {
            if (bonus.isExpired()) {
                try {
                    expireBonus(bonus);
                } catch (BonusException e) {
                    LOG.error("getActiveBonuses: cannot expire bonus: " + bonus, e);
                }
            } else {
                bonusIdsList.add(bonus.getId());
            }
        }
        return bonusIdsList;
    }

    public void save(Bonus bonus) {
        bonusPersister.persist(bonus);
    }

    public List<Long> getInternalListGamesIds(String[] gameIds, long bankId) throws BonusException {
        BonusError bonusError;
        List<Long> internalGames = new ArrayList<>();
        long gameId;
        for (String strGameId : gameIds) {
            Long originalId = BaseGameCache.getInstance().getOriginalGameId(strGameId, bankId);
            if (originalId == null) {
                try {
                    gameId = Long.parseLong(strGameId);
                } catch (NumberFormatException e) {
                    bonusError = BonusErrors.INVALID_GAMES_ID;
                    throw new BonusException(bonusError.getDescription(), e);
                }
            } else {
                gameId = originalId;
                LOG.info("Game id was redefined from {}, to {}", strGameId, gameId);
            }
            internalGames.add(gameId);
        }
        return internalGames;
    }

    public String getExternalGameId(long gameId, long bankId) {
        String externalGameId = BaseGameCache.getInstance().getExternalGameId(gameId, bankId);
        if (externalGameId == null) {
            externalGameId = String.valueOf(gameId);
        } else {
            LOG.info("Conversation {} to {}", gameId, externalGameId);
        }
        return externalGameId;
    }

    public Bonus awardBonusOnMassAward(AccountInfo accountInfo, BaseMassAward massAward) throws BonusException {
        try {
            Bonus bonus = (Bonus) massAward.getBonusByAccountId(accountInfo,
                    IdGenerator.getInstance().getNext(Bonus.class));
            initBonus(true, bonus);
            return bonus;
        } catch (Exception e) {
            LOG.error("Failed award bonus for account={}", accountInfo.getId(), e);
            throw new BonusException("failed awardBonus accountId=" + accountInfo.getId(), e);
        }

    }

    private void initBonus(boolean internal, Bonus bonus) {
        bonus.setInternal(internal);
        save(bonus);
    }

    /**
     * Must be invoked under the {@link SessionHelper#lock(long) lock}
     * with {@link SessionHelper#openSession() opened session}.
     *
     * @param accountInfo The {@link TransactionData#getAccount() account }
     *                    from current {@link SessionHelper#getTransactionData() transactionData}
     */
    @Override
    public void checkMassAwardsForAccount(AccountInfo accountInfo) throws BonusException {
        try {
            List<Long> currentPlayerMassAwardIds = accountInfo.getBonusMassAwardIdsList();
            List<Long> newMassAwardIds = new ArrayList<>();
            List<Long> massAwardIds = massAwardBonusManager.getAwardsForBank(accountInfo.getBankId());
            boolean changed = false;
            if (massAwardIds != null) {
                massAwardIds = new ArrayList<>(massAwardIds);
                for (Long massAwardId : massAwardIds) {
                    BaseMassAward massAward = massAwardBonusManager.get(massAwardId);
                    if (massAward == null) {
                        LOG.warn("checkMassAwardsForAccount: found removed massAwardId={}, for bank={}",
                                massAwardId, accountInfo.getBankId());
                        currentPlayerMassAwardIds.remove(massAwardId);
                        changed = true;
                        continue;
                    }
                    if (massAward.getTemplate() instanceof BonusMassAwardBonusTemplate) {
                        if (massAward.isExpired()) {
                            massAwardBonusManager.remove(massAward.getId());
                            if (currentPlayerMassAwardIds.contains(massAward.getId())) {
                                currentPlayerMassAwardIds.remove(massAward.getId());
                                changed = true;
                            }
                            continue;
                        }
                        if (currentPlayerMassAwardIds.contains(massAward.getId())) {
                            continue;
                        }
                        if (massAward.isPlayerSuitable(accountInfo)) {
                            awardBonusOnMassAward(accountInfo, massAward);
                            newMassAwardIds.add(massAward.getId());
                            changed = true;
                        }
                    }
                }
            }
            currentPlayerMassAwardIds.addAll(newMassAwardIds);
            for (int i = 0; i < currentPlayerMassAwardIds.size(); i++) {
                Long massId = currentPlayerMassAwardIds.get(i);
                if (!newMassAwardIds.contains(massId) && massAwardBonusManager.get(massId) == null) {
                    if (currentPlayerMassAwardIds.contains(massId)) {
                        currentPlayerMassAwardIds.remove(massId);
                        changed = true;
                    }
                }
            }
            if (changed) {
                accountInfo.setBonusMassAwardIdsList(currentPlayerMassAwardIds);
                SessionHelper.getInstance().getDomainSession().persistAccount();
            }
        } catch (Exception e) {
            throw new BonusException("failed checkMassAwardsForAccount accountId=" + accountInfo.getId(), e);
        }
    }

    @Override
    public void invalidateClient(long bankId) {
        clients.remove(bankId);
    }

    public boolean isBonusShouldBeLost(Bonus bonus, AccountInfo accountInfo) {
        if (bonus == null || accountInfo == null) {
            return false;
        }
        Long bonusMinThreshold = getBonusMinThreshold(accountInfo);
        return bonus.getBalance() < bonusMinThreshold && bonus.getStatus() == BonusStatus.ACTIVE;
    }

    private Long getBonusMinThreshold(AccountInfo accountInfo) {
        int bankId = accountInfo.getBankId();
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        return bankInfo.getBonusThresholdMinKey();
    }

    public boolean bonusIsValidForGameId(Bonus bonus, AccountInfo accountInfo, long gameId) {
        long bankId = accountInfo.getBankId();
        Set<Long> allGames = BaseGameCache.getInstance().getAllGamesSet(bankId, BankInfoCache.getInstance().getBankInfo(
                accountInfo.getBankId()).getDefaultCurrency());
        if (!bonus.isGameIdIncluded(gameId, allGames)) {
            // for migration
            Set<Long> oldGames = GameServer.getInstance().getOldGames(bankId, gameId);
            Long newGame = GameServer.getInstance().getNewGame(bankId, gameId);
            boolean isMigration = false;
            if (newGame != null && bonus.isGameIdIncluded(newGame, allGames)) {
                isMigration = true;
            }

            if (oldGames != null && !isMigration) {
                for (Long old : oldGames) {
                    if (bonus.isGameIdIncluded(old, allGames)) {
                        isMigration = true;
                    }
                }
            }

            if (!isMigration) {
                return false;
            }
        }
        //mq check
        boolean isMultiplayerGame = BaseGameInfoTemplateCache.getInstance().
                getBaseGameInfoTemplateById(gameId).isMultiplayerGame();
        if (bonus.getBonusGameMode().equals(BonusGameMode.ALL) && bonus.getGameIds().isEmpty() && !isMultiplayerGame) {
            return true;
        }
        Set<Long> multiplayerGames = BaseGameInfoTemplateCache.getInstance().getMultiplayerGames();
        Collection<Long> bonusGames = bonus.getBonusGameMode().equals(BonusGameMode.ALL) &&
                !bonus.getGameIds().isEmpty() ? bonus.getGameIds() : bonus.getValidGameIds(allGames);
        for (Long bonusGameId : bonusGames) {
            boolean currentBonusIsMultiplayer = multiplayerGames.contains(bonusGameId);
            if (isMultiplayerGame && !currentBonusIsMultiplayer) {
                LOG.warn("bonusIsValidForGameId: multiplayer gameId={} but bonus contain not multiplayerGame={}",
                        gameId, bonusGameId);
                return false;
            }
            if (!isMultiplayerGame && currentBonusIsMultiplayer) {
                LOG.warn("bonusIsValidForGameId: not multiplayer gameId={} but bonus contain multiplayerGame={}",
                        gameId, bonusGameId);
                return false;
            }
        }
        return true;
    }
}
