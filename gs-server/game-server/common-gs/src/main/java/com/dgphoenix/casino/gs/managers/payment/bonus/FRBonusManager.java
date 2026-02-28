package com.dgphoenix.casino.gs.managers.payment.bonus;

import com.abs.casino.common.cache.data.bonus.BonusSystemType;
import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraFrBonusArchivePersister;
import com.abs.casino.cassandra.persist.CassandraFrBonusPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.*;
import com.abs.casino.common.cache.data.bonus.restriction.MassAwardRestriction;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBWinOperation;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusNotification;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin;
import com.abs.casino.common.cache.data.payment.frb.FRBWinOperationStatus;
import com.abs.casino.common.cache.data.payment.frb.FRBonusNotificationStatus;
import com.abs.casino.common.cache.data.payment.frb.IFRBonusWin;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.currency.ICurrencyRateManager;
import com.abs.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.exception.FRBException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.abs.casino.common.transactiondata.TransactionData;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.IdGenerator;
import com.abs.casino.common.util.ReflectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.GameServer;
import com.abs.casino.gs.managers.payment.bonus.IFRBonusClient;
import com.abs.casino.gs.managers.payment.bonus.IFRBonusManager;
import com.abs.casino.gs.managers.payment.bonus.IFRBonusWinManager;
import com.abs.casino.gs.managers.payment.bonus.FRBonusWinRequestFactory;
import com.abs.casino.gs.managers.payment.bonus.mass.MassAwardBonusManager;
import com.abs.casino.gs.managers.payment.bonus.tracker.FRBonusNotificationTracker;
import com.abs.casino.gs.managers.payment.bonus.tracker.FRBonusNotificationTrackerTask;
import com.abs.casino.gs.managers.payment.bonus.tracker.FRBonusWinTracker;
import com.abs.casino.gs.managers.payment.bonus.tracker.FRBonusWinTrackerTask;
import com.abs.casino.gs.persistance.GameSessionPersister;
import com.abs.casino.gs.persistance.LasthandPersister;
import com.abs.casino.gs.persistance.PlayerSessionPersister;
import com.abs.casino.gs.persistance.remotecall.KafkaRequestMultiPlayer;
import com.abs.casino.kafka.dto.BonusStatusDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public class FRBonusManager implements IFRBonusManager {
    private static final Logger LOG = LogManager.getLogger(FRBonusManager.class);
    private ConcurrentMap<Long, IFRBonusClient> clients = new ConcurrentHashMap<>();

    private final CassandraFrBonusArchivePersister frBonusArchivePersister;
    private final CassandraFrBonusPersister frBonusPersister;
    private final MassAwardBonusManager massAwardBonusManager;
    private final KafkaRequestMultiPlayer kafkaRequestMultiPlayer;
    protected ICurrencyRateManager currencyConverter;

    public FRBonusManager(CassandraPersistenceManager cpm,
                          KafkaRequestMultiPlayer kafkaRequestMultiPlayer,
                          MassAwardBonusManager massAwardBonusManager,
                          ICurrencyRateManager currencyConverter) {
        frBonusArchivePersister = cpm.getPersister(CassandraFrBonusArchivePersister.class);
        frBonusPersister = cpm.getPersister(CassandraFrBonusPersister.class);
        this.kafkaRequestMultiPlayer = kafkaRequestMultiPlayer;
        this.massAwardBonusManager = massAwardBonusManager;
        this.currencyConverter = currencyConverter;
    }

    /**
     * Only for backward compatibility
     *
     * @deprecated Use getting from context instead
     */
    @Deprecated
    public static FRBonusManager getInstance() {
        return ApplicationContextHelper.getApplicationContext().getBean(FRBonusManager.class);
    }

    protected synchronized IFRBonusClient instantiateClient(long bankId) throws BonusException {
        String klazz = BankInfoCache.getInstance().getBankInfo(bankId).getBonusFRRequestClientClass();
        if (StringUtils.isTrimmedEmpty(klazz)) {
            LOG.error("instantiateClient bankId:" + bankId + " can't instantiate client");
            return null;
        }
        try {
            Class<?> aClass = ReflectionUtils.forNameWithCompatibilityAliases(klazz);
            Constructor<?> clientConstructor = aClass.getConstructor(long.class);
            IFRBonusClient client = (IFRBonusClient) clientConstructor.newInstance(bankId);
            IFRBonusClient existClient = clients.putIfAbsent(bankId, client);
            return existClient != null ? existClient : client;
        } catch (Exception e) {
            LOG.error("instantiateClient bankId:" + bankId + " can't instantiate client", e);
            throw new BonusException(e);
        }
    }

    protected void assertCheckBankProperty(long bankId) throws BonusException {
        if (!BankInfoCache.getInstance().getBankInfo(bankId).isFRBConfigurationValid()) {
            String message = "FreeRounds configuration is invalid for bankId=" + bankId;
            LOG.error(message);
            throw new FRBException(message);
        }

        try {
            FRBonusWinRequestFactory.getInstance().getFRBonusWinManager(bankId);
        } catch (FRBException ex) {
            LOG.error("FRBonusWinManager not found " + bankId + " " + ex);
            throw new BonusException("FRBonusWinManager not found", ex);
        }

        try {
            instantiateClient(bankId);
        } catch (FRBException ex) {
            LOG.error("IFRBonusClient not found bankId=" + bankId + " " + ex);
            throw new BonusException("IFRBonusClient not found", ex);
        }
    }

    @Override
    public FRBonus awardBonus(AccountInfo accountInfo, long rounds, String extId, List<Long> gameIds, String description,
                              String comment, long timeAwarded, boolean internal, Long startDate, Long expirationDate,
                              Long freeRoundValidity, Long frbTableRoundChips, Long coinValue, Long maxWinLimit) throws BonusException {
        return awardBonus(accountInfo, rounds, extId, gameIds, description, comment, timeAwarded, internal, startDate,
                expirationDate, freeRoundValidity, frbTableRoundChips, coinValue, maxWinLimit, null);
    }

    @Override
    public FRBonus awardBonus(AccountInfo accountInfo,
                              long rounds,
                              String extId,
                              List<Long> gameIds,
                              String description,
                              String comment,
                              long timeAwarded,
                              boolean internal,
                              Long startDate,
                              Long expirationDate,
                              Long freeRoundValidity,
                              Long frbTableRoundChips,
                              Long coinValue,
                              Long maxWinLimit,
                              Supplier<String> externalBonusIdComposer) throws BonusException {
        try {
            assertCheckBankProperty(accountInfo.getBankId());
            assertGameTypeValid(gameIds);
            FRBonus frBonus = composeBonus(accountInfo.getId(), accountInfo.getBankId(), rounds, extId, gameIds,
                    description, comment, timeAwarded, startDate, expirationDate, freeRoundValidity,
                    frbTableRoundChips, coinValue, maxWinLimit);
            initBonus(internal, frBonus, externalBonusIdComposer);
            return frBonus;
        } catch (Exception e) {
            throw new BonusException("failed awardBonus accountId=" + accountInfo.getId(), e);
        }

    }

    @Override
    public FRBonus awardBonusOnMassAward(AccountInfo accountInfo,
                                         BaseMassAward massAward) throws BonusException {
        try {
            FRBonus frBonus = (FRBonus) massAward.getBonusByAccountId(accountInfo, IdGenerator.getInstance().getNext(FRBonus.class));
            Long maxWinLimit = massAward.getMaxWinLimit();
            if (maxWinLimit != null && maxWinLimit > 0) {
                String defaultCurrency = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId()).getDefaultCurrency().getCode();
                String playerCurrency = accountInfo.getCurrency().getCode();
                if (!playerCurrency.equals(defaultCurrency)) {
                    maxWinLimit = (long) Math.ceil(currencyConverter.convert(maxWinLimit, defaultCurrency, playerCurrency));
                }
            }
            frBonus.setMaxWinLimit(maxWinLimit);
            initBonus(true, frBonus, null);
            return frBonus;
        } catch (Exception e) {
            throw new BonusException("failed ::awardBonus accountId=" + accountInfo.getId(), e);
        }

    }

    /**
     * @param externalBonusIdComposer provides external id which will be used to get this bonus
     */
    private void initBonus(boolean internal, FRBonus frBonus, Supplier<String> externalBonusIdComposer) throws CommonException {
        frBonus.setInternal(internal);
        save(frBonus, externalBonusIdComposer);
    }

    @Override
    public FRBonus getById(long id) {
        return getActualFRBonus(frBonusPersister.get(id));
    }

    public FRBonus getArchivedFRBonusById(long id) {
        return frBonusArchivePersister.get(id);
    }

    public List<FRBonus> getArchivedFRBonusesByExtId(long bankId, String bonusExtId) {
        return frBonusArchivePersister.getByExtId(frBonusArchivePersister.composeKey(bankId, bonusExtId));
    }

    private boolean internalChangeBonusStatus(FRBonus bonus, BonusStatus status) throws BonusException {
        boolean result = false;
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
            FRBonus onlineBonus = getOnlineFRBonus();
            if (onlineBonus != null && onlineBonus.getId() == bonus.getId()) {
                bonus = onlineBonus;
            }

            sentNotifyToMQ(bonus, status, SessionHelper.getInstance().getTransactionData());

            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bonus.getBankId());
            boolean isNeedToNotify = bankInfo.isSendSummarizedFrbNotification() && status != BonusStatus.EXPIRED;
            FRBonusNotification frbNotification = null;
            if (isNeedToNotify) {
                frbNotification = SessionHelper.getInstance().getTransactionData().getFrbNotification();
                if (frbNotification != null) {
                    throw new BonusException("Failed to change status for frBonus=" + bonus +
                            ", another notification is in progress: " + frbNotification);
                }
                frbNotification = new FRBonusNotification(
                        GameServer.getInstance().getIdGenerator().getNext(FRBonusNotification.class),
                        bonus.getAccountId(), bonus.getId(), bonus.getExtId(), bonus.getWinSum(), status,
                        FRBonusNotificationStatus.STARTED);
                SessionHelper.getInstance().getTransactionData().setFrbNotification(frbNotification);
            }

            result = changeBonusStatus(bonus, status);
            if (isNeedToNotify) {
                FRBonusNotificationTrackerTask notificationTask = new FRBonusNotificationTrackerTask(
                        bonus.getAccountId(), FRBonusNotificationTracker.getInstance());
                try {
                    notificationTask.process();
                } catch (CommonException e) {
                    LOG.error("Cannot synchronously process frbNotification: " + frbNotification, e);
                    //add to tracker for async reprocess
                    FRBonusNotificationTracker.getInstance().addTask(bonus.getAccountId());
                }
            }
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            }
        } catch (Exception e) {
            throw new BonusException("failed change status for frBonus=" + bonus.getId() + ", new status=" + status, e);
        } finally {
            if (!transactionAlreadyStarted) {
                SessionHelper.getInstance().clearWithUnlock();
            }
        }
        return result;
    }

    private void sentNotifyToMQ(FRBonus bonus, BonusStatus status, ITransactionData transactionData) throws Exception {
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

        LOG.debug("FRB sentNotifyToMQ, status: {}, bonus.getId(): {} ",  status, bonus.getId());
        kafkaRequestMultiPlayer.sendChangeBonusStatusToMQ(new BonusStatusDto(
            status.name(),
            bonus.getId(),
            bonus.getAccountId()
        ));
    }

    @Override
    public boolean cancelBonus(FRBonus bonus) throws BonusException {
        return internalChangeBonusStatus(bonus, BonusStatus.CANCELLED);
    }

    @Override
    public boolean expireBonus(FRBonus bonus) throws BonusException {
        return internalChangeBonusStatus(bonus, BonusStatus.EXPIRED);
    }

    @Override
    public void invalidateClient(long bankId) {
        clients.remove(bankId);
    }

    @Override
    public boolean closeBonus(FRBonus bonus) throws BonusException {
        return internalChangeBonusStatus(bonus, BonusStatus.CLOSED);
    }


    @Override
    public FRBonus get(long bankId, String extId) throws BonusException {
        try {
            return getActualFRBonus(frBonusPersister.getByExtId(bankId + "+" + extId));
        } catch (Exception e) {
            throw new BonusException("failed getBonusByExtId extId=" + extId, e);
        }
    }

    protected boolean changeBonusStatus(FRBonus bonus, BonusStatus status) throws CommonException {
        if (bonus != null) {
            bonus.setStatusAndEndTime(status, System.currentTimeMillis());
            AccountInfo account = AccountManager.getInstance().getAccountInfo(bonus.getAccountId());
            FRBonusWinRequestFactory.getInstance().getFRBonusWinManager(bonus.getBankId())
                    .handleFRBonusChangeStatus(account, bonus.getId(), status);
            boolean isLive = markGameSession(bonus);
            removeLastHands(account, bonus.getId(), isLive);
            if (isLive && (bonus.getStatus() == BonusStatus.CANCELLED || bonus.getStatus() == BonusStatus.EXPIRED)) {
                GameSession gameSession = SessionHelper.getInstance().getTransactionData().getGameSession();
                SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
                sessionInfo.setLastCloseGameReason("FRB " + bonus.getStatus().name().toLowerCase());
                LOG.debug("Found cancel or expire FRBonus for online GameSession, close game, gameSession=" +
                        gameSession);
                GameServer.getInstance().closeOnlineGame(gameSession.getId(), false,
                        GameServer.getInstance().getServerId(), sessionInfo, true);
            }
            flush(bonus);
            return true;
        }
        return false;
    }

    public void flush(FRBonus bonus) throws CommonException {
        if (bonus != null) {
            bonus.incrementVersion();
            save(bonus);
        }
    }

    protected boolean markGameSession(FRBonus bonus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("markGameSession " + bonus.getId());
        }
        SessionInfo sessionInfo = PlayerSessionPersister.getInstance().getSessionInfo();
        if (sessionInfo != null && sessionInfo.getGameSessionId() != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("markGameSession sessionInfo " + sessionInfo);
            }
            GameSession gameSession = null;
            try {
                gameSession = GameSessionPersister.getInstance().getGameSession(sessionInfo.getGameSessionId());
            } catch (CommonException e) {
                LOG.error("failed load GameSession", e);
            }
            if (gameSession != null && gameSession.isFRBonusGameSession() && gameSession.getFrbonusId() == bonus.getId()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("markGameSession gameSession " + gameSession);
                }
                gameSession.setFrbonusStatus(bonus.getStatus());
                return true;
            }
        }

        return false;
    }

    protected void removeLastHands(AccountInfo accountInfo, long bonusId, boolean isLive) {
        LasthandPersister.getInstance().clearAllForBonus(accountInfo, bonusId, BonusSystemType.FRB_SYSTEM, isLive);
    }

    public void assertGameTypeValid(List<Long> gameIds) throws CommonException {
        if (gameIds.contains(779L)) { // not allowed only 779 mq game
            throw new BonusException("FRBonus for multiplayer games not allowed");
        }
    }

    private FRBonus composeBonus(long accountId, long bankId, long rounds,
                                 String extId, List<Long> gameIds,
                                 String description, String comment,
                                 long timeAwarded, Long startDate, Long expirationDate,
                                 Long freeRoundValidity, Long frbTableRoundChips, Long coinValue, Long maxWinLimit) {
        long bonusId = IdGenerator.getInstance().getNext(FRBonus.class);
        return new FRBonus(bonusId, accountId, bankId, rounds, rounds, extId, gameIds, comment, description, 0, 0,
                timeAwarded, BonusStatus.ACTIVE, startDate, expirationDate, freeRoundValidity, null,
                frbTableRoundChips, coinValue, maxWinLimit);
    }

    @Override
    public List<FRBonus> getFinishedBonuses(AccountInfo accountInfo) throws BonusException {
        try {
            return frBonusPersister.getFinishedFRBonusList(accountInfo.getId());
        } catch (Exception e) {
            throw new BonusException("exception in db", e);
        }
    }

    @Override
    public List<FRBonus> getActiveBonuses(AccountInfo accountInfo) {
        return getActiveBonuses(accountInfo.getId());
    }

    public List<FRBonus> getActiveBonuses(long accountId) {
        List<FRBonus> dbBonuses = frBonusPersister.getActiveBonuses(accountId);
        List<FRBonus> activeBonuses = new ArrayList<>(dbBonuses.size());
        for (FRBonus frBonus : dbBonuses) {
            //bonus may be already expired, ExpiredFRBonusTracker started too later
            if (frBonus.isExpired()) {
                try {
                    expireBonus(frBonus);
                } catch (Exception e) {
                    LOG.error("getActiveBonuses: cannot expire bonus={}", frBonus, e);
                }
            } else if (frBonus.getStatus() == BonusStatus.ACTIVE) {
                activeBonuses.add(frBonus);
            }
        }
        FRBonus onlineFRBonus = getOnlineFRBonus();
        if (onlineFRBonus != null) {
            for (int i = 0; i < activeBonuses.size(); i++) {
                FRBonus frBonus = activeBonuses.get(i);
                if (frBonus.getId() == onlineFRBonus.getId()) {
                    if (onlineFRBonus.getStatus() != BonusStatus.ACTIVE ||
                            frBonus.getRoundsLeft() <= 0) {
                        activeBonuses.remove(i);
                    } else {
                        activeBonuses.set(i, onlineFRBonus);
                    }
                    break;
                }
            }
        }
        return activeBonuses;
    }

    public List<Long> getFRBonusIdsList(long accountId) {
        List<FRBonus> frBonusList = getActiveBonuses(accountId);
        List<Long> frBonusIdsList = new LinkedList<>();
        for (FRBonus frBonus : frBonusList) {
            if (!frBonus.isExpired()) {
                frBonusIdsList.add(frBonus.getId());
            }
        }
        return frBonusIdsList;
    }

    public Long getEarlestActiveFRBonusId(long accountId, long gameId) {
        List<FRBonus> bonuses = getActiveBonuses(accountId);
        if (!CollectionUtils.isEmpty(bonuses)) {
            FRBonus earlestBonus = null;
            for (FRBonus frBonus : bonuses) {
                if (frBonus.getMassAwardId() != null) {
                    MassAwardRestriction restriction = massAwardBonusManager.getMassAwardRestriction(frBonus.getMassAwardId());
                    AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(frBonus.getAccountId());
                    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(frBonus.getBankId());
                    if (!restriction.isValid(accountInfo, frBonus, bankInfo.getDefaultCurrency())) {
                        continue;
                    }
                }
                if (frBonus.isExpired()) {
                    try {
                        expireBonus(frBonus);
                    } catch (Exception e) {
                        LOG.error("getEarlestActiveFRBonusId: cannot expire bonus: " + frBonus, e);
                    }
                    continue;
                }
                if (frBonus.getGameIds().contains(gameId)) {
                    if (frBonus.isNewVersion() && !frBonus.isReady()) {
                        continue;
                    }
                    if (earlestBonus == null) {
                        earlestBonus = frBonus;
                    } else if (frBonus.getTimeAwarded() < earlestBonus.getTimeAwarded()) {
                        earlestBonus = frBonus;
                    }
                }
            }
            return earlestBonus == null ? null : earlestBonus.getId();
        } else {
            return null;
        }

    }

    @Override
    public IFRBonusClient getClient(long bankId) throws BonusException {
        IFRBonusClient client = clients.get(bankId);
        return client == null ? instantiateClient(bankId) : client;
    }

    public void save(FRBonus bonus) throws CommonException {
        frBonusPersister.persist(bonus, null);
    }

    public void save(FRBonus bonus, Supplier<String> externalBonusIdCompose) {
        frBonusPersister.persist(bonus, externalBonusIdCompose);
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
            long now = System.currentTimeMillis();
            List<Long> currentPlayerMassAwardIds = accountInfo.getFrbMassAwardIdsList();
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
                    if (massAward.getTemplate() instanceof FRBMassAwardBonusTemplate) {
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
            try {
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
            } catch (Exception e) {
                LOG.error("failed checkMassAwardsForAccount, clear currentPlayerMassAwardIds", e);
            }
            if (changed) {
                accountInfo.setFrbMassAwardIdsList(currentPlayerMassAwardIds);
                SessionHelper.getInstance().getDomainSession().persistAccount();
            }
            StatisticsManager.getInstance().updateRequestStatistics("checkMassAwardsForAccount", System.currentTimeMillis() - now,
                    accountInfo.getId());
        } catch (Exception e) {
            LOG.error("failed checkMassAwardsForAccount", e);
            throw new BonusException("failed checkMassAwardsForAccount accountId=" + accountInfo.getId(), e);
        }
    }

    private FRBonus getOnlineFRBonus() {
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        return transactionData == null ? null : transactionData.getFrBonus();
    }

    private FRBonus getActualFRBonus(FRBonus frBonus) {
        FRBonus onlineFRBonus = getOnlineFRBonus();
        return frBonus == null ||
                onlineFRBonus == null ||
                onlineFRBonus.getId() != frBonus.getId() ? frBonus : onlineFRBonus;
    }

    public void checkPendingOperation(IFRBonusWin frbonusWin, AccountInfo accountInfo, long gameId) throws FRBException {
        if (frbonusWin != null) {
            FRBWinOperation operation = ((FRBonusWin) frbonusWin).getFRBonusWinOperation(gameId);
            if (operation != null) {
                boolean exceptionalCondition = true;
                IFRBonusWinManager frBonusWinManager = FRBonusWinRequestFactory.getInstance()
                        .getFRBonusWinManager(accountInfo.getBankId());
                if (operation.getExternalStatus() == FRBWinOperationStatus.COMPLETED) {
                    try {
                        LOG.debug("Finalizing completed operation {}", operation);
                        new FRBonusWinTrackerTask(accountInfo.getId(), FRBonusWinTracker.getInstance()).process();
                        exceptionalCondition = false;
                    } catch (CommonException e) {
                        LOG.error("Can not finalize operation", e);
                    }
                } else if (frBonusWinManager != null && frBonusWinManager.isSendSingleFRBWin()) {
                    LOG.debug("Uses single frb win");
                    FRBonus bonus = FRBonusManager.getInstance().getById(operation.getBonusId());
                    if (bonus != null && bonus.getStatus() == BonusStatus.ACTIVE) {
                        LOG.debug("Bonus is active, can be continued to play");
                        exceptionalCondition = false;
                    }
                }
                if (exceptionalCondition) {
                    throw new FRBException("FRB previous operation is not completed, " +
                            "accountId: " + accountInfo.getId() + " , gameId: " + gameId);
                }
            }
        }
    }
}
