package com.abs.casino.gs.managers.payment.bonus.mass;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraAccountInfoPersister;
import com.abs.casino.cassandra.persist.CassandraDelayedMassAwardFailedDeliveryPersister;
import com.abs.casino.cassandra.persist.CassandraDelayedMassAwardHistoryPersister;
import com.abs.casino.cassandra.persist.CassandraDelayedMassAwardPersister;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.DelayedMassAward;
import com.dgphoenix.casino.common.cache.data.bonus.DelayedMassAwardDelivery;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.web.HttpClientConnection;
import com.dgphoenix.casino.gs.GameServer;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import one.util.streamex.StreamEx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

/**
 * User: flsh
 * Date: 13.10.12
 */
public class DelayedMassAwardManager {
    private static final Logger LOG = LogManager.getLogger(DelayedMassAwardManager.class);
    private static final long SLEEPTIME_PERIOD_IN_SECONDS = 60;
    private static final String PARAM_INTERNAL_IDS = "intIds";
    private static final String PARAM_ERROR_DESC = "errorDesc";
    private static final String PARAM_DELAYED_ID = "delayedAwardId";

    private boolean initialized;
    private ScheduledFuture<?> scheduledFuture;

    private final CassandraDelayedMassAwardPersister delayedMassAwardPersister;
    private final CassandraDelayedMassAwardHistoryPersister delayedMassAwardHistoryPersister;
    private final CassandraDelayedMassAwardFailedDeliveryPersister delayedMassAwardFailedDeliveryPersister;
    private final CassandraAccountInfoPersister accountInfoPersister;
    private final ScheduledExecutorService scheduler;

    public DelayedMassAwardManager(CassandraPersistenceManager cassandraPersistenceManager, ScheduledExecutorService scheduler) {
        delayedMassAwardPersister = cassandraPersistenceManager.getPersister(CassandraDelayedMassAwardPersister.class);
        delayedMassAwardHistoryPersister = cassandraPersistenceManager.getPersister(CassandraDelayedMassAwardHistoryPersister.class);
        delayedMassAwardFailedDeliveryPersister = cassandraPersistenceManager.getPersister(CassandraDelayedMassAwardFailedDeliveryPersister.class);
        accountInfoPersister = cassandraPersistenceManager.getPersister(CassandraAccountInfoPersister.class);
        this.scheduler = scheduler;
    }

    @PostConstruct
    private void init() {
        if (initialized) {
            LOG.info("Already initialized");
            return;
        }
        scheduledFuture = scheduler.scheduleAtFixedRate(this::process, SLEEPTIME_PERIOD_IN_SECONDS, SLEEPTIME_PERIOD_IN_SECONDS,
                TimeUnit.SECONDS);
        this.initialized = true;
    }

    @PreDestroy
    private void shutdown() {
        LOG.info("Shutdown");
        scheduledFuture.cancel(true);
        this.initialized = false;
    }

    public void createDelayedMassAward(long delayedMassAwardId, long bankId, String[] extIds) throws CommonException {
        LOG.info("createDelayedMassAward: {}", delayedMassAwardId);
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        checkNotNull(bankInfo, "BankInfo not found: %s", bankId);
        checkArgument(!bankInfo.isCheckAccountOnOldSystem(), "Migration from old system in progress");
        checkArgument(bankInfo.isFRBConfigurationValid() || bankInfo.isBonusConfigurationValid(),
                "Neither FRB nor Bonus configuration is valid for bankId=%s", bankId);
        checkArgument(isNotEmpty(extIds), "extIds must not be empty");

        DelayedMassAward delayedMassAward = new DelayedMassAward(delayedMassAwardId, bankId);
        Set<String> processedExternalIds = new HashSet<>();
        for (String extId : extIds) {
            LOG.info("createDelayedMassAward: {}", extId);
            if (isTrimmedEmpty(extId)) {
                continue;
            }
            AccountData accountData = AccountData.parseFromCsv(extId); //only to check format
            checkArgument(!isTrimmedEmpty(accountData.externalId),
                    "Empty extUserId, in string: %s", extId);
            boolean isDuplicatedExternalId = !processedExternalIds.add(accountData.externalId);
            if (isDuplicatedExternalId) {
                String errorMessage = String.format("Found duplicate external id: %s when try create delayed mass award: %s",
                        accountData.externalId, delayedMassAwardId);
                LOG.error(errorMessage);
                throw new CommonException(errorMessage);
            }
            LOG.debug("Parsed data: {}", accountData);
            delayedMassAward.addExtAccount(extId);
        }
        delayedMassAwardPersister.create(delayedMassAward, GameServer.getInstance().getServerId());
        //processing continued async in DelayedProcessor/process
        LOG.info("createDelayedMassAward, added: {}", delayedMassAward);
    }

    //Need create async callback to CM. because on createDelayedMassAward DB transaction is not commited on CM side
    private void process() {
        try {
            LOG.debug("Start task");
            Collection<DelayedMassAward> delayedMassAwards = delayedMassAwardPersister.getUncompleted(GameServer.getInstance().getServerId());
            for (DelayedMassAward delayedMassAward : delayedMassAwards) {
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(delayedMassAward.getBankId());
                DelayedMassAwardDelivery delivery = delayedMassAwardFailedDeliveryPersister.get(delayedMassAward.getId());
                boolean deliveryPersisted = true;
                if (delivery == null) {
                    deliveryPersisted = false;
                    delivery = buildDelivery(delayedMassAward, bankInfo);
                }
                sendDelivery(delayedMassAward, delivery, bankInfo.getSubCasinoId(), deliveryPersisted);
            }
        } catch (Exception e) {
            LOG.error("Cannot process", e);
        }
        LOG.debug("End task");
    }

    private void sendDelivery(DelayedMassAward delayedMassAward, DelayedMassAwardDelivery delivery, long subCasinoId, boolean deliveryPersisted) {
    }

    private DelayedMassAwardDelivery buildDelivery(DelayedMassAward delayedMassAward, BankInfo bankInfo) {
        DelayedMassAwardDelivery delivery;
        List<String> extAccounts = delayedMassAward.getExtAccounts();
        List<Long> successAccounts = processAccounts(delayedMassAward, extAccounts, bankInfo);
        String accountIds = StreamEx.of(successAccounts).joining(",");
        String errorDesc = processError(delayedMassAward, extAccounts.size(), successAccounts.size());
        delivery = new DelayedMassAwardDelivery(delayedMassAward.getId(), errorDesc, accountIds);
        return delivery;
    }

    private Map<String, String> getRequestParameters(DelayedMassAwardDelivery delivery) {
        return ImmutableMap.of(
                PARAM_DELAYED_ID, String.valueOf(delivery.getId()),
                PARAM_ERROR_DESC, delivery.getErrorDesc(),
                PARAM_INTERNAL_IDS, delivery.getAccountIds()
        );
    }

    private String processError(DelayedMassAward delayedMassAward, int allAccountSize, int successSize) {
        String errorDesc = successSize < allAccountSize ?
                ("Failed count: " + (allAccountSize - successSize)) :
                ("Created all, count=" + successSize);
        LOG.info("DelayedMassAward: {}: success={}, failed={}, desc={}",
                delayedMassAward.getId(), successSize, allAccountSize - successSize, errorDesc);
        return errorDesc;
    }

    private List<Long> processAccounts(DelayedMassAward delayedMassAward, List<String> extAccounts, BankInfo bankInfo) {
        List<Long> successAccounts = new ArrayList<>();
        for (String extAccount : extAccounts) {
            try {
                AccountData accountData = AccountData.parseFromCsv(extAccount);
                LOG.debug("Parsed data: {}", accountData);
                Long accountId = accountInfoPersister.getAccountIdByExtId(delayedMassAward.getBankId(), accountData.externalId);
                if (accountId == null) {
                    Currency currency = isTrimmedEmpty(accountData.currencyCode)
                            ? BankInfoCache.getInstance().getBankInfo(bankInfo.getId()).getDefaultCurrency()
                            : CurrencyCache.getInstance().get(accountData.currencyCode);
                    AccountInfo accountInfo = AccountManager.getInstance().createAccountInDbOnly(accountData.externalId,
                            bankInfo, (short) bankInfo.getSubCasinoId(), accountData.nickName, false,
                            false, accountData.email, ClientType.FLASH, accountData.firstName,
                            accountData.lastName, currency, accountData.countryCode);
                    accountId = accountInfo.getId();
                }
                successAccounts.add(accountId);
            } catch (Exception e) {
                LOG.error("Can't process data: {}, bankId: {}", extAccount, bankInfo.getId(), e);
            }
        }
        return successAccounts;
    }

    private String sendNotification(Map<String, String> paramsMap, String url) {
        String response = null;
        try {
            response = HttpClientConnection.newInstance().doRequest(url, paramsMap, true, false);
        } catch (Exception e) {
            LOG.error("Cannot send result, url={}", url, e);
        }
        return response;
    }

    private static class AccountData {
        static Splitter dataSplitter = Splitter.on(';');

        String externalId;
        String nickName;
        String firstName;
        String lastName;
        String email;
        String currencyCode;
        String countryCode;

        AccountData(String externalId, String nickName, String firstName, String lastName, String email,
                    String currencyCode, String countryCode) {
            this.externalId = externalId;
            this.nickName = nickName;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.currencyCode = currencyCode;
            this.countryCode = countryCode;
        }

        /**
         * @param csvWithData has format externalId;nickName;firstName;lastName;email;currencyCode;countryCode
         */
        static AccountData parseFromCsv(String csvWithData) {
            List<String> accountData = new ArrayList<>();
            for (String data : dataSplitter.split(csvWithData)) {
                String value = isTrimmedEmpty(data) || data.equals("null") ? null : data;
                accountData.add(value);
            }
            return new AccountData(accountData.get(0), accountData.get(1), accountData.get(2), accountData.get(3),
                    accountData.get(4), accountData.get(5), accountData.get(6));
        }

        @Override
        public String toString() {
            return "AccountData{" +
                    "externalId='" + externalId + '\'' +
                    ", nickName='" + nickName + '\'' +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", email='" + email + '\'' +
                    ", currencyCode='" + currencyCode + '\'' +
                    ", countryCode='" + countryCode + '\'' +
                    '}';
        }
    }
}