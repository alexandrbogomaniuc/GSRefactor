package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.IEntityUpdateListener;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.FastKryoHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * User: flsh
 * Date: 4/11/12
 */
public class CassandraBankInfoPersister extends AbstractLongDistributedConfigEntryPersister<BankInfo> {
    private static final Logger LOG = LogManager.getLogger(CassandraBankInfoPersister.class);

    public static final String BANK_INFO_CF = "BankInfoCF";
    private boolean isNeedDebugSerialize;

    private final List<IEntityUpdateListener<Long, BankInfo>> bankInfoUpdateListeners = new CopyOnWriteArrayList<>();

    private CassandraBankInfoPersister() {
        super();
    }

    public void setNeedDebugSerialize(boolean isNeedDebugSerialize) {
        this.isNeedDebugSerialize = isNeedDebugSerialize;
    }

    //with notify other server use RemoteCallHelper.getInstance().saveAndSendNotification(bankInfo);
    public void save(BankInfo bankInfo) throws CommonException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Save: " + bankInfo);
        }
        persist(bankInfo.getId(), bankInfo);
    }

    @Override
    public void persist(Long bankId, BankInfo bankInfo) {
        //GSB-3182 test for kryo.KryoException
        if (isNeedDebugSerialize) {
            ByteBuffer byteBuffer = FastKryoHelper.serializeToBytes(bankInfo);
            try {
                FastKryoHelper.deserializeFrom(byteBuffer, BankInfo.class);
            } catch (Exception e) {
                LOG.error("DebugSerialize error, for bankInfo {} ", bankInfo, e);
                throw e;
            }
        }
        super.persist(bankId, bankInfo);
        notifyBankInfoUpdateListeners(bankId, bankInfo);
    }

    @Override
    public void refresh(String id) {
        super.refresh(id);
        notifyBankInfoUpdateListeners(Long.valueOf(id), get(id));
    }

    @Override
    public void remove(Long id) {
        super.remove(id);
        notifyBankInfoUpdateListeners(id, null);
    }

    @Override
    public BankInfoCache getCache() {
        return BankInfoCache.getInstance();
    }

    @Override
    public int loadAll() {
        final Map<Long, BankInfo> infos = loadAllAsMap(BankInfo.class);
        if (infos == null) {
            LOG.error("loadAllForLongKeys return null");
            return 0;
        }
        int count = 0;
        for (BankInfo info : infos.values()) {
            //fix Currency references
            final Currency defaultCurrency = info.getDefaultCurrency();
            if (defaultCurrency != null) {
                Currency cacheCurrency = CurrencyCache.getInstance().get(defaultCurrency.getCode());
                if (cacheCurrency != null) {
                    info.setDefaultCurrency(cacheCurrency);
                }
            }
            final List<Currency> currencies = info.getCurrencies();
            final List<Currency> fixedCurrencies = new ArrayList<Currency>(currencies.size());
            for (Currency currency : currencies) {
                Currency cacheCurrency = CurrencyCache.getInstance().get(currency.getCode());
                if (cacheCurrency != null) {
                    fixedCurrencies.add(cacheCurrency);
                } else {
                    getLog().error("Assigned currency: " + currency.getCode() + " not exist in currency cache, " +
                            "but assigned in BankInfo: " + info);
                    fixedCurrencies.add(currency);
                }
            }
            info.setCurrencies(fixedCurrencies);
            if (LOG.isTraceEnabled()) {
                LOG.trace("loadAll: " + info);
            }
            put(info);
            count++;
        }
        LOG.info("loadAll: count=" + count);
        return BankInfoCache.getInstance().size();
    }

    public void addBankInfoUpdateListener(IEntityUpdateListener<Long, BankInfo> updateListener) {
        bankInfoUpdateListeners.add(updateListener);
    }

    @Override
    public String getMainColumnFamilyName() {
        return BANK_INFO_CF;
    }

    public BankInfo get(String bankId) {
        return get(bankId, BankInfo.class);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    private void notifyBankInfoUpdateListeners(Long bankId, BankInfo updatedBankInfo) {
        try {
            for (IEntityUpdateListener<Long, BankInfo> updateListener : bankInfoUpdateListeners) {
                updateListener.notify(bankId, updatedBankInfo);
            }
        } catch (Exception e) {
            LOG.debug("Can't notify BankInfo update listeners", e);
        }
    }

}
