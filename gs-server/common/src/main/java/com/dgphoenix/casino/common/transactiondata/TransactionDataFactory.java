package com.dgphoenix.casino.common.transactiondata;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.util.ReflectionUtils;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Grien
 * Date: 30.05.2014 18:33
 */
public class TransactionDataFactory {
    private static final String DEFAULT_TRANSACTION_DATA_STORAGE_CLASS =
            "com.dgphoenix.casino.transactiondata.BasicTransactionDataStorageHelper";
    private static final Logger LOG = LogManager.getLogger(TransactionDataFactory.class);
    private final Map<Long, ITransactionDataStorageHelper> managers = new HashMap<>();
    private final Map<String, ITransactionDataStorageHelper> managersByClassName = new HashMap<>();

    private static TransactionDataFactory instance = new TransactionDataFactory();

    public static TransactionDataFactory getInstance() {
        return instance;
    }

    private TransactionDataFactory() {
    }

    public ITransactionData createTransactionData(String lockId, Map<String, ByteBuffer> map, int lastLockerId) {
        Integer bankId = StringIdGenerator.extractBankAndExternalUserIdFromUserHash(lockId).getKey();
        ITransactionDataStorageHelper helper = getTransactionDataManager(bankId);
        if (helper == null) {
            throw new NullPointerException("TransactionDataStorageHelper is null, for bankId=" + bankId);
        }
        return helper.create(lockId, map, lastLockerId);
    }

    public Map<String, ByteBuffer> getStoredData(ITransactionData transactionData) {
        return getTransactionDataManager(transactionData.getBankId()).getStoredData(transactionData);
    }

    public ITransactionDataStorageHelper getTransactionDataManager(long bankId) {
        ITransactionDataStorageHelper manager = managers.get(bankId);
        if (manager == null) {
            synchronized (this) {
                manager = managers.get(bankId);
                if (manager == null) {
                    managers.put(bankId, manager = instantiate(bankId));
                }
            }
        }
        return manager;
    }

    private ITransactionDataStorageHelper instantiate(long bankId) {
        LOG.info("instantiate: manager for bankId=" + bankId);
        ITransactionDataStorageHelper manager;
        try {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo == null) {
                LOG.warn("instantiate: bankInfo is not found, bankId=" + bankId);
            }
            String className = bankInfo == null ? null : bankInfo.getTransactionDataClass();
            if (StringUtils.isTrimmedEmpty(className)) {
                className = DEFAULT_TRANSACTION_DATA_STORAGE_CLASS;
            }
            manager = managersByClassName.get(className);
            if (manager == null) {
                Class<?> aClass = ReflectionUtils.forNameWithCompatibilityAliases(className);
                Constructor<?> constructor = aClass.getConstructor();
                manager = (ITransactionDataStorageHelper) constructor.newInstance();
                managersByClassName.put(className, manager);
            }
        } catch (Exception e) {
            LOG.error("TransactionDataFactory::instantiate error:", e);
            manager = null;//add default
        }
        return manager;
    }
}
