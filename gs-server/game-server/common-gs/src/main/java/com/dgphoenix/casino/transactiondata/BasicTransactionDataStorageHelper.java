package com.dgphoenix.casino.transactiondata;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.account.LasthandInfo;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusNotification;
import com.dgphoenix.casino.common.cache.data.payment.bonus.FRBonusWin;
import com.dgphoenix.casino.common.cache.data.payment.transfer.PaymentTransaction;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.promo.PromoCampaignMemberInfos;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.transactiondata.ITransactionDataStorageHelper;
import com.dgphoenix.casino.common.transactiondata.TransactionData;
import com.dgphoenix.casino.common.web.statistics.IStatisticsGetter;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.esotericsoftware.kryo.util.UnsafeUtil;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.dgphoenix.casino.cassandra.KeyspaceConfiguration.PROTOCOL_VERSION;
import static com.dgphoenix.casino.cassandra.persist.CassandraTransactionDataPersister.*;
import static com.dgphoenix.casino.common.util.FastKryoHelper.*;

/**
 * User: Grien
 * Date: 02.06.2014 13:20
 */
public class BasicTransactionDataStorageHelper implements ITransactionDataStorageHelper {
    private static final Logger LOG = LogManager.getLogger(BasicTransactionDataStorageHelper.class);
    private static final AtomicLong matchedLockerCount = new AtomicLong();
    private static final AtomicLong misMatchedLockerCount = new AtomicLong();
    private final Map<String, ITDFieldSerializeHelper> helpers = ImmutableMap.<String, ITDFieldSerializeHelper>builder().
            put(PLAYER_SESSION_FIELD, new WithClassKryoSerializeHelper()).
            put(WALLET_FIELD, new WithClassKryoSerializeHelper()).

            put(ACCOUNT_FIELD, new SimpleKryoSerializeHelper<>(AccountInfo.class)).
            put(GAME_SESSION_FIELD, new SimpleKryoSerializeHelper<>(GameSession.class)).
            put(LAST_HAND_FIELD, new SimpleKryoSerializeHelper<>(LasthandInfo.class)).
            put(LAST_BET_FIELD, new SimpleKryoSerializeHelper<>(PlayerBet.class)).
            put(BONUS_FIELD, new SimpleKryoSerializeHelper<>(Bonus.class)).
            put(FRBONUS_FIELD, new SimpleKryoSerializeHelper<>(FRBonus.class)).
            put(FRBWIN_FIELD, new SimpleKryoSerializeHelper<>(FRBonusWin.class)).
            put(FRBNOTIFY_FIELD, new SimpleKryoSerializeHelper<>(FRBonusNotification.class)).
            put(PAYMENT_TRANSACTION_FIELD, new SimpleKryoSerializeHelper<>(PaymentTransaction.class)).
            put(PROMO_MEMBERS_FIELD, new SimpleKryoSerializeHelper<>(PromoCampaignMemberInfos.class)).

            put(LAST_UPDATE_ID_FIELD, new CassandraSerializeHelper<String>(com.datastax.driver.core.DataType.ascii())).
            put(TRACKING_INFO, new CassandraSerializeHelper<String>(com.datastax.driver.core.DataType.ascii())).
            put(VERSION_FIELD, new CassandraSerializeHelper<Long>(com.datastax.driver.core.DataType.bigint())).

            build();

    static {
        StatisticsManager.getInstance().registerStatisticsGetter("BasicTransactionDataStorageHelper", new IStatisticsGetter() {
            @Override
            public String getStatistics() {
                return "matchedLockerCount=" + matchedLockerCount.get() +
                        ", misMatchedLockerCount=" + misMatchedLockerCount.get();
            }
        });
    }

    public BasicTransactionDataStorageHelper() {
    }

    @Override
    public ITransactionData create(String lockId, Map<String, ByteBuffer> map, int lastLockerId) {
        TransactionData data = new TransactionData(lockId);
        AccountInfo accountInfo = deserialize(ACCOUNT_FIELD, map);
        if (accountInfo != null) {
            data.setAccount(accountInfo);
        }
        SessionInfo playerSession = deserialize(PLAYER_SESSION_FIELD, map);
        if (playerSession != null) {
            data.setPlayerSession(playerSession);
        }

        GameSession gameSession = deserialize(GAME_SESSION_FIELD, map);
        if (gameSession != null) {
            data.setGameSession(gameSession);
        }

        LasthandInfo lasthand = deserialize(LAST_HAND_FIELD, map);
        if (lasthand != null) {
            data.setLasthand(lasthand);
        }
        String lastUpdateInfo = deserialize(LAST_UPDATE_ID_FIELD, map);
        if (lastUpdateInfo != null) {
            data.setLastUpdateInfo(lastUpdateInfo);
        }

        Long version = deserialize(VERSION_FIELD, map);
        if (version != null) {
            data.setVersion(version);
        }

        IWallet wallet = deserialize(WALLET_FIELD, map);
        if (wallet != null) {
            data.setWallet(wallet);
        }
        PaymentTransaction paymentTransaction = deserialize(PAYMENT_TRANSACTION_FIELD, map);
        if (paymentTransaction != null) {
            data.setPaymentTransaction(paymentTransaction);
        }
        PlayerBet lastBet = deserialize(LAST_BET_FIELD, map);
        if (lastBet != null) {
            data.setLastBet(lastBet);
        }
        Bonus bonus = deserialize(BONUS_FIELD, map);
        if (bonus != null) {
            data.setBonus(bonus);
        }
        FRBonus frBonus = deserialize(FRBONUS_FIELD, map);
        if (frBonus != null) {
            data.setFrBonus(frBonus);
        }
        FRBonusWin frbWin = deserialize(FRBWIN_FIELD, map);
        if (frbWin != null) {
            data.setFrbWin(frbWin);
        }
        FRBonusNotification frbNotify = deserialize(FRBNOTIFY_FIELD, map);
        if (frbNotify != null) {
            data.setFrbNotification(frbNotify);
        }
        PromoCampaignMemberInfos promoMembers = deserialize(PROMO_MEMBERS_FIELD, map);
        if (promoMembers != null) {
            data.setPromoMemberInfos(promoMembers);
        }
        String lastTrackingInfo = deserialize(TRACKING_INFO, map);
        data.setTrackingState(lastTrackingInfo);
        if (data.getTrackingState() != null) {
            if (data.getTrackingState().getGameServerId() != lastLockerId) {
                LOG.warn("Select lastLockerId mismatch, expected=" + lastLockerId + ", actual=" +
                        data.getTrackingState().getGameServerId());
                misMatchedLockerCount.incrementAndGet();
            } else {
                matchedLockerCount.incrementAndGet();
            }
        }
        return data;
    }

    @Override
    public Map<String, ByteBuffer> getStoredData(ITransactionData data) {
        Map<String, ByteBuffer> result = new HashMap<>(20);
        try {
            serializeAndAdd(ACCOUNT_FIELD, data.getAccount(), result);
            serializeAndAdd(PLAYER_SESSION_FIELD, data.getPlayerSession(), result);
            serializeAndAdd(GAME_SESSION_FIELD, data.getGameSession(), result);
            serializeAndAdd(LAST_HAND_FIELD, data.getLasthand(), result);
            serializeAndAdd(WALLET_FIELD, data.getWallet(), result);
            serializeAndAdd(LAST_BET_FIELD, data.getLastBet(), result);
            serializeAndAdd(BONUS_FIELD, data.getBonus(), result);
            serializeAndAdd(FRBONUS_FIELD, data.getFrBonus(), result);
            serializeAndAdd(FRBWIN_FIELD, data.getFrbWin(), result);
            serializeAndAdd(FRBNOTIFY_FIELD, data.getFrbNotification(), result);
            serializeAndAdd(PAYMENT_TRANSACTION_FIELD, data.getPaymentTransaction(), result);
            serializeAndAdd(PROMO_MEMBERS_FIELD, data.getPromoMemberInfos(), result);
            serializeAndAdd(VERSION_FIELD, data.getVersion(), result);
            serializeAndAdd(LAST_UPDATE_ID_FIELD, data.getLastUpdateInfo(), result);
            if (data.isTrackingStateChanged()) {
                serializeAndAdd(TRACKING_INFO, data.getTrackingStateAsString(), result);
            }
            return result;
        } catch (Throwable t) {
            for (ByteBuffer byteBuffer : result.values()) {
                try {
                    UnsafeUtil.releaseBuffer(byteBuffer);
                } catch (Throwable ignore) {
                }
            }
            throw t;
        }
    }

    public <T> T deserialize(String field, Map<String, ByteBuffer> fromMap) {
        return getHelper(field).deserialize(fromMap.get(field));
    }

    public void serializeAndAdd(String field, Object value, Map<String, ByteBuffer> toMap) {
        toMap.put(field, getHelper(field).serialize(value));
    }

    public ITDFieldSerializeHelper getHelper(String field) {
        return helpers.get(field);
    }

    protected class SimpleKryoSerializeHelper<T> implements ITDFieldSerializeHelper {
        private Class<T> aClass;

        public SimpleKryoSerializeHelper(Class<T> aClass) {
            this.aClass = aClass;
        }

        @Override
        public ByteBuffer serialize(Object o) {
            return serializeToBytes(o);
        }

        @Override
        public T deserialize(ByteBuffer buffer) {
            return deserializeFrom(buffer, aClass);
        }
    }

    protected class WithClassKryoSerializeHelper implements ITDFieldSerializeHelper {
        public WithClassKryoSerializeHelper() {
        }

        @Override
        public ByteBuffer serialize(Object o) {
            return serializeWithClassToBytes(o);
        }

        @Override
        public <T> T deserialize(ByteBuffer buffer) {
            return deserializeWithClassFrom(buffer);
        }
    }

    protected class CassandraSerializeHelper<T> implements ITDFieldSerializeHelper {
        private com.datastax.driver.core.DataType dataType;

        public CassandraSerializeHelper(com.datastax.driver.core.DataType dataType) {
            this.dataType = dataType;
        }

        @Override
        public ByteBuffer serialize(Object o) {
            return getObjectTypeCodec().serialize(o, PROTOCOL_VERSION);
        }

        @Override
        public T deserialize(ByteBuffer buffer) {
            return buffer == null ? null : (T) getObjectTypeCodec().deserialize(buffer, PROTOCOL_VERSION);
        }

        private com.datastax.driver.core.TypeCodec<Object> getObjectTypeCodec() {
            com.datastax.driver.core.TypeCodec<Object> objectTypeCodec = com.datastax.driver.core.CodecRegistry.DEFAULT_INSTANCE.codecFor(dataType);
            if (objectTypeCodec == null) {
                throw new RuntimeException("Cannot find codec for data type: " + dataType);
            }
            return objectTypeCodec;
        }
    }
}
