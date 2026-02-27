package com.dgphoenix.casino.gs.persistance;

import com.abs.casino.gs.persistance.ILasthandPersister;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraLasthandPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BonusSystemType;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfo;
import com.dgphoenix.casino.common.cache.data.game.GameGroup;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItem;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItemType;
import com.abs.casino.common.transactiondata.storeddate.identifier.LasthandStoredInfo;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 4/3/12
 */
public class LasthandPersister implements ILasthandPersister {
    private static LasthandPersister instance = new LasthandPersister();
    private static final Logger LOG = LogManager.getLogger(LasthandPersister.class);

    private final CassandraLasthandPersister lasthandPersister;

    private LasthandPersister() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        lasthandPersister = persistenceManager.getPersister(CassandraLasthandPersister.class);
    }

    public static LasthandPersister getInstance() {
        return instance;
    }

    public LasthandInfo loadIntoTransactionData(long accountId, long gameId, Long bonusId,
                                                BonusSystemType bonusSystemType) {
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        LasthandInfo lasthandInfo = transactionData.getLasthand();
        if (lasthandInfo == null) {
            StoredItem<LasthandInfo, LasthandStoredInfo> item = transactionData.get(StoredItemType.LASTHAND);
            if (item != null && item.getIdentifier() != null &&
                    item.getIdentifier().equals(accountId, gameId, bonusId, bonusSystemType)) {
                lasthandInfo = item.getItem();
            } else {
                String data = lasthandPersister.get(accountId, gameId, bonusId, bonusSystemType);
                lasthandInfo = new LasthandInfo(gameId, data);
            }
            transactionData.setLasthand(lasthandInfo);
        } else {
            assertCorrectGameId(lasthandInfo, gameId);
        }
        return lasthandInfo;
    }

    @Override
    public LasthandInfo get(long accountId, long gameId) {
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        LasthandInfo lasthandInfo = transactionData.getLasthand();
        if (lasthandInfo != null && lasthandInfo.getId() == gameId) {
            return lasthandInfo;
        }
        StoredItem<LasthandInfo, LasthandStoredInfo> item = transactionData.get(StoredItemType.LASTHAND);
        if (item != null && item.getIdentifier() != null &&
                item.getIdentifier().equals(accountId, gameId, null, null)) {
            lasthandInfo = item.getItem();
        } else {
            String data = lasthandPersister.get(accountId, gameId, null, null);
            lasthandInfo = new LasthandInfo(gameId, data);
        }
        return lasthandInfo;
    }

    public LasthandInfo get(long accountId, long gameId, Long bonusId, BonusSystemType type) {
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        LasthandInfo lasthandInfo = transactionData.getLasthand();
        if (lasthandInfo != null && lasthandInfo.getId() == gameId) {
            return lasthandInfo;
        }
        StoredItem<LasthandInfo, LasthandStoredInfo> item = transactionData.get(StoredItemType.LASTHAND);
        if (item != null && item.getIdentifier() != null &&
                item.getIdentifier().equals(accountId, gameId, bonusId, type)) {
            lasthandInfo = item.getItem();
        } else {
            String data = lasthandPersister.get(accountId, gameId, bonusId, type);
            lasthandInfo = new LasthandInfo(gameId, data);
        }
        return lasthandInfo;
    }

    public LasthandInfo forcedGet(long accountId, long gameId) {
        String data = lasthandPersister.get(accountId, gameId, null, null);
        return StringUtils.isTrimmedEmpty(data) ? null : new LasthandInfo(gameId, data);
    }

    public LasthandInfo forcedGet(long accountId, long gameId, Long bonusId, BonusSystemType type) {
        String data = lasthandPersister.get(accountId, gameId, bonusId, type);
        return StringUtils.isTrimmedEmpty(data) ? null : new LasthandInfo(gameId, data);
    }

    public void forceSaveStoredItemLastHand() {
        ITransactionData td = SessionHelper.getInstance().getTransactionData();
        StoredItem<LasthandInfo, LasthandStoredInfo> item = td.get(StoredItemType.LASTHAND);
        if (item != null && item.getItem() != null) {
            LasthandInfo lasthandInfo = item.getItem();
            LOG.debug("forceSaveStoredItemLastHand: {}", lasthandInfo);
            forcedSave(lasthandInfo, td.getAccountId(), lasthandInfo.getId(), null, null);
        }
    }

    public void forcedSave(LasthandInfo lasthandInfo, long accountId, long gameId, Long bonusId, BonusSystemType type) {
        if (lasthandInfo != null) {
            lasthandPersister.persist(accountId, gameId, bonusId, lasthandInfo.getLasthandData(), type);
        }
    }

    public void save(LasthandInfo lasthandInfo) {
        SessionHelper.getInstance().getTransactionData().setLasthand(lasthandInfo);
    }

    public void save(long gameId, String lasthand) {
        LasthandInfo lasthandInfo = SessionHelper.getInstance().getTransactionData().getLasthand();
        if (lasthandInfo == null) {
            lasthandInfo = new LasthandInfo(gameId, lasthand);
            save(lasthandInfo);
        } else {
            assertCorrectGameId(lasthandInfo, gameId);
            lasthandInfo.setLasthandData(lasthand);
        }
    }

    private void assertCorrectGameId(LasthandInfo lasthandInfo, long gameId) {
        if (lasthandInfo.getId() > 0 && lasthandInfo.getId() != gameId && gameId > 0) {
            BaseGameInfo defaultGameInfo = BaseGameInfoTemplateCache.getInstance().getDefaultGameInfo(lasthandInfo.getId());
            if (defaultGameInfo.getGroup() != GameGroup.ACTION_GAMES || gameId != 1) {
                LOG.error("assertCorrectGameId: failed, gameId=" + gameId + ", lastHand.gameId=" +
                        lasthandInfo.getId());
                throw new RuntimeException("Cannot save lastHand, gameId mismatch");
            }
        }
    }

    public void clearCached() {
        SessionHelper.getInstance().getTransactionData().setLasthand(null);
    }

    public void delete(long id, long accountId, long gameId, Long bonusId, BonusSystemType bonusSystemType) {
        SessionHelper.getInstance().getTransactionData().setLasthand(null);
    }

    public void clearAllForBonus(AccountInfo accountInfo, Long bonusId, BonusSystemType bonusSystemType,
                                 boolean isLiveGameSession) {
        lasthandPersister.delete(accountInfo.getId(), bonusSystemType, bonusId);
        if (isLiveGameSession) {
            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
            if (transactionData != null) {
                transactionData.setLasthand(null);
                StoredItem<LasthandInfo, LasthandStoredInfo> item = transactionData.get(StoredItemType.LASTHAND);
                LasthandStoredInfo identifier;
                if (item != null &&
                        (identifier = item.getIdentifier()) != null &&
                        identifier.getAccountId() == accountInfo.getId() &&
                        (identifier.getBonusId() != null ?
                                identifier.getBonusId().equals(bonusId) :
                                bonusId == null) &&
                        identifier.getBonusSystemType() == bonusSystemType) {
                    transactionData.getAtomicallyStoredData().remove(StoredItemType.LASTHAND);
                }
            }
        }
    }

    public void saveOnClose(long accountId, long gameId, Long bonusId, BonusSystemType bonusSystemType,
                            LasthandInfo lasthandInfo) {
        SessionHelper.getInstance().getTransactionData().add(StoredItemType.LASTHAND,
                lasthandInfo, new LasthandStoredInfo(accountId, gameId, bonusId, bonusSystemType));
/*
        if (lasthandInfo == null || StringUtils.isTrimmedEmpty(lasthandInfo.getLasthandData())) {
            lastHandPersister.delete(accountId, gameId, bonusId, bonusSystemType);
        } else {
            lastHandPersister.persist(accountId, gameId, bonusId, lasthandInfo.getLasthandData(),
                    bonusSystemType);
        }
*/
        SessionHelper.getInstance().getTransactionData().setLasthand(null);
    }
}
