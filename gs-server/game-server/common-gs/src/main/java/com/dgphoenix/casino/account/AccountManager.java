package com.dgphoenix.casino.account;

import com.abs.casino.common.cache.AbstractExportableCache;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraAccountInfoPersister;
import com.dgphoenix.casino.cassandra.persist.CassandraExtendedAccountInfoPersister;
import com.dgphoenix.casino.common.SessionHelper;
import com.abs.casino.common.api.ICommonManager;
import com.dgphoenix.casino.common.cache.*;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.bank.PlayerGameSettingsType;
import com.abs.casino.common.cache.data.bonus.BonusSystemType;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.abs.casino.common.configuration.CasinoSystemType;
import com.abs.casino.common.exception.AccountException;
import com.abs.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItemType;
import com.abs.casino.common.util.AccountIdGenerator;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.gs.GameServer;
import com.abs.casino.gs.managers.dblink.FreeGameCalculator;
import com.dgphoenix.casino.gs.managers.payment.bonus.*;
import com.abs.casino.gs.managers.payment.bonus.client.BonusAccountInfoResult;
import com.dgphoenix.casino.gs.managers.payment.currency.CurrencyManager;
import com.abs.casino.sm.AbstractPlayerSessionManager;
import com.dgphoenix.casino.sm.IGetAccountInfoProvider;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

@com.abs.casino.common.cache.CacheKeyInfo(description = "accountInfo.id")
public class AccountManager extends AbstractExportableCache<AccountInfo> implements ICommonManager, IAccountManager {
    private static final Logger LOG = LogManager.getLogger(AccountManager.class);
    private static AccountManager instance;

    private CasinoSystemType casinoSystemType;
    private final CassandraAccountInfoPersister accountInfoPersister;
    private final CassandraExtendedAccountInfoPersister extendedAccountInfoPersister;

    public AccountManager(CasinoSystemType casinoSystemType, CassandraPersistenceManager persistenceManager) {
        this.casinoSystemType = casinoSystemType;
        extendedAccountInfoPersister = persistenceManager.getPersister(CassandraExtendedAccountInfoPersister.class);
        accountInfoPersister = persistenceManager.getPersister(CassandraAccountInfoPersister.class);
    }

    /**
     * @deprecated AccountManager is Spring bean now, should be injected from context.
     * This method should be used only for backward compatibility.
     */
    @Deprecated
    public static AccountManager getInstance() {
        if (instance == null) {
            instance = ApplicationContextHelper.getBean(AccountManager.class);
        }
        return instance;
    }

    public CasinoSystemType getCasinoSystemType() {
        return casinoSystemType;
    }

    public AccountInfo createAccount(Long subCasinoId, BankInfo bankInfo, String accountExtId,
                                     BonusSystemType bonusSystemType) throws BonusException {
        try {
            BonusAccountInfoResult result;
            if (bankInfo.isNoUseAccountInfoUrlForAuth()) {
                result = new BonusAccountInfoResult(accountExtId,
                        "", "", "", bankInfo.getDefaultCurrency().getCode(), true, "");
            } else {
                if (bonusSystemType == BonusSystemType.ORDINARY_SYSTEM) {
                    BonusManager bonusManager = BonusManager.getInstance();
                    IBonusClient client = bonusManager.getClient(bankInfo.getId());
                    result = client.getAccountInfo(accountExtId);

                } else { // FRB System
                    IFRBonusManager bonusManager = FRBonusManager.getInstance();
                    IFRBonusClient client = bonusManager.getClient(bankInfo.getId());
                    result = client.getAccountInfo(accountExtId);
                }
            }
            return saveAccount(null, accountExtId, bankInfo, result.getUserName(), false, false,
                    result.getEmail(), ClientType.FLASH, result.getFirstName(),
                    result.getLastName(), result.getCurrency(), result.getCountryCode(), true);
        } catch (Exception e) {
            throw new BonusException(e);
        }
    }

    public String getExtId(long accountId) {
        AccountInfo accountInfo = accountInfoPersister.get(accountId);
        if (accountInfo == null) {
            LOG.warn("getExtId: cannot find account by accountId: {}", accountId);
            return "UNKNOWN";
        }
        return accountInfo.getExternalId();
    }

    public boolean isPerfectAccountIdMode(long bankId) {
        return casinoSystemType.isSingleBank();
    }

    public AccountInfo getByCompositeKey(short subCasinoId, int bankId, String externalId) throws CommonException {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        return getByCompositeKey(subCasinoId, bankInfo, externalId);
    }

    public AccountInfo getByCompositeKey(long bankId, String externalId) throws CommonException {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        return getByCompositeKey(bankInfo, externalId);
    }

    public AccountInfo getByCompositeKey(long subCasinoId, BankInfo bankInfo, String externalId) throws CommonException {
        return getByCompositeKey(bankInfo, externalId);
    }

    private AccountInfo getByCompositeKey(BankInfo bankInfo, String externalId) throws CommonException {
        if (!bankInfo.isPersistAccounts()) {
            return null;
        }
        long bankId = bankInfo.getId();
        AccountInfo accountInfo;
        if (isPerfectAccountIdMode(bankId)) {
            accountInfo = getAccountInfo(AccountIdGenerator.generate(bankId, Long.parseLong(externalId),
                    casinoSystemType));
        } else {
            accountInfo = internalGetAccountInfo(bankInfo.getId(), externalId);
        }
        return accountInfo;
    }

    protected AccountInfo internalGetAccountInfo(long bankId, String externalId) {
        final ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        AccountInfo accountInfo = transactionData != null ? transactionData.getAccount() : null;
        if (accountInfo == null) {
            accountInfo = accountInfoPersister.getByCompositeKey(bankId, externalId);
            if (transactionData != null) {
                transactionData.setAccount(accountInfo);
            }
        }
        return accountInfo;
    }

    public AccountInfo getAccountInfo(long accountId) {
        return getAccountInfo(accountId, true);
    }

    public AccountInfo getAccountInfo(long accountId, boolean withLoadFromCassandra) {
        final ITransactionData td = SessionHelper.getInstance().getTransactionData();
        AccountInfo accountInfo = null;
        if (td != null) {
            accountInfo = td.getAccount();
        }
        if (accountInfo == null && withLoadFromCassandra) {
            accountInfo = accountInfoPersister.get(accountId);
            if (td != null) {
                td.setAccount(accountInfo);
            }
        }
        return accountInfo;
    }

    public Collection<AccountInfo> loadAll() {
        throw new UnsupportedOperationException("not implemented");
    }

    public AccountInfo getAccountInfo(short subCasinoId, int bankId, String externalId) throws CommonException {
        if (isTrimmedEmpty(externalId)) {
            LOG.error("Invalid externalId parameter (subCasinoId:{}, bankId:{}, externalId:{})", subCasinoId, bankId, externalId);
            throw new CommonException("Parameter externalId is missing");
        }
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        return getByCompositeKey(subCasinoId, bankInfo, externalId);
    }

    public AccountInfo getAccountInfo(long subCasinoId, long bankId, String externalId) throws CommonException {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        return getByCompositeKey(subCasinoId, bankInfo, externalId);
    }

    public void remove(AccountInfo accountInfo, BankInfo bankInfo) {
        if (bankInfo.isPersistAccounts()) {
            if (bankInfo.getPgsType() != PlayerGameSettingsType.ACCOUNT) {
                accountInfo.setGameSettings(null);
            }
            if (!accountInfo.isGuest()) {
                SessionHelper.getInstance().getTransactionData().add(StoredItemType.ACCOUNT, accountInfo, null);
                //getAccountInfoPersister.persist(accountInfo);
            }
        }
        SessionHelper.getInstance().getTransactionData().setAccount(null);
    }

    public void save(AccountInfo accountInfo) {
        SessionHelper.getInstance().getTransactionData().setAccount(accountInfo);
    }

    public void persist(AccountInfo accountInfo, boolean saveToCasandra, boolean newAccount,
                        boolean saveToTransactionData) {
        if (saveToCasandra) {
            accountInfoPersister.persist(accountInfo, newAccount);
        }
        if (saveToTransactionData) {
            SessionHelper.getInstance().getTransactionData().setAccount(accountInfo);
        }
    }

    private AccountInfo composeAccount(Long accountId, String externalId, int bankId,
                                       short subCasinoId, String nickName, boolean isGuest,
                                       boolean isLocked, Currency currency, String countryCode) {
        if(accountId == null) {
            if (isPerfectAccountIdMode(bankId)) {
                accountId = AccountIdGenerator.generate(bankId, Long.parseLong(externalId),
                        GameServerConfiguration.getInstance().getCasinoSystemType());
            } else {
                accountId = GameServer.getInstance().getIdGenerator().getNext(AccountInfo.class);
            }
        }
        return new AccountInfo(accountId, externalId, bankId, subCasinoId, System.currentTimeMillis(),
                isGuest, isLocked, currency, countryCode, nickName);
    }

    private void saveAccount(AccountInfo accountInfo, ClientType clientType, boolean isAgent,
                                Boolean gender, Long dateOfBirth, String addressId, String address, BankInfo bankInfo,
                                String city, String state, String country, String zip, String refType, Long refId,
                                boolean newAccount, boolean saveToTransactionData)
            throws AccountException {
        try {
            boolean isGuest = accountInfo != null && accountInfo.isGuest();
            if (!isGuest) {
                boolean saveToCassandra = bankInfo.isPersistAccounts();
                persist(accountInfo, saveToCassandra, newAccount, saveToTransactionData);

                if (saveToTransactionData) {
                    SessionHelper.getInstance().getDomainSession().persistAccount();
                }
            }
        } catch (Exception e) {
            LOG.error("Cannot persist account", e);
            throw new AccountException(e);
        }
    }

    private void saveNewAccount(AccountInfo accountInfo, ClientType clientType, boolean isAgent,
                                Boolean gender, Long dateOfBirth, String addressId, String address, BankInfo bankInfo,
                                String city, String state, String country, String zip, String refType, Long refId,
                                boolean initTransactionData)
            throws AccountException {

        saveAccount(
                accountInfo,
                clientType,
                isAgent,
                gender,
                dateOfBirth,
                addressId,
                address,
                bankInfo,
                city,
                state,
                country,
                zip,
                refType,
                refId,
                true,
                initTransactionData);
    }

    private void assertValidSubCasino(long subCasinoId, long bankId) throws AccountException {
        if (!SubCasinoCache.getInstance().isExist(subCasinoId, bankId)) {
            throw new AccountException("SubCasino or bank not found");
        }
    }

    public AccountInfo createAccountInDbOnly(String externalId, BankInfo bankInfo,
                                             short subCasinoId, String nickName, boolean isGuest,
                                             boolean isLocked, String email, ClientType clientType,
                                             String firstName, String lastName, Currency currency,
                                             String countryCode)
            throws AccountException {
        return _saveAccount(null, externalId, bankInfo, subCasinoId, nickName, isGuest, isLocked, email, clientType,
                firstName, lastName, currency, countryCode, true, false);
    }

    public AccountInfo saveAccountWithCurrencyUpdate(Long accountId, String externalId, BankInfo bankInfo, String nickName, boolean isGuest,
                                                     boolean isLocked, String email, ClientType clientType,
                                                     String firstName, String lastName, String extCurrency,
                                                     String countryCode, boolean newAccount) throws CommonException {
        return saveAccountWithCurrencyUpdate(
                accountId,
                externalId,
                bankInfo,
                nickName,
                isGuest,
                isLocked,
                email,
                clientType,
                firstName,
                lastName,
                extCurrency,
                countryCode,
                null,
                newAccount);
    }

    public AccountInfo saveAccountWithCurrencyUpdate(Long accountId, String externalId, BankInfo bankInfo, String nickName, boolean isGuest,
                                                     boolean isLocked, String email, ClientType clientType,
                                                     String firstName, String lastName, String extCurrency,
                                                     String countryCode, IGetAccountInfoProvider getAccountInfoProvider,
                                                     boolean newAccount) throws CommonException {

        String currencyCode = AbstractPlayerSessionManager.updateCurrency(bankInfo, externalId, extCurrency, getAccountInfoProvider);
        currencyCode = CurrencyManager.getInstance().getCurrencyCodeByAlias(currencyCode, bankInfo);

        return saveAccount(
                accountId,
                externalId,
                bankInfo,
                nickName,
                isGuest,
                isLocked, email, clientType,
                firstName,
                lastName,
                currencyCode,
                countryCode,
                newAccount);
    }

    public AccountInfo saveAccount(Long accountId, String externalId, BankInfo bankInfo, String nickName, boolean isGuest,
                                   boolean isLocked, String email, ClientType clientType,
                                   String firstName, String lastName, String currencyCode,
                                   String countryCode, boolean newAccount) throws CommonException {
        Currency currency;
        if (isTrimmedEmpty(currencyCode)) {
            currency = bankInfo.getDefaultCurrency();
        } else {
            currency = CurrencyManager.getInstance().setupCurrency(currencyCode, currencyCode, bankInfo.getId());
        }

        return saveAccount(
                accountId,
                externalId,
                bankInfo,
                (short) bankInfo.getSubCasinoId(),
                nickName,
                isGuest,
                isLocked,
                email,
                clientType,
                firstName,
                lastName,
                currency,
                countryCode,
                newAccount);
    }

    public AccountInfo saveAccount(Long accountId, String externalId, BankInfo bankInfo,
                                   short subCasinoId, String nickName, boolean isGuest,
                                   boolean isLocked, String email, ClientType clientType,
                                   String firstName, String lastName, Currency currency,
                                   String countryCode, boolean newAccount) throws AccountException {

        return _saveAccount(
                accountId,
                externalId,
                bankInfo,
                subCasinoId,
                nickName,
                isGuest,
                isLocked,
                email,
                clientType,
                firstName,
                lastName,
                currency,
                countryCode,
                newAccount,
                true);
    }

    public AccountInfo _saveAccount(Long accountId, String externalId, BankInfo bankInfo,
                                    short subCasinoId, String nickName, boolean isGuest,
                                    boolean isLocked, String email, ClientType clientType,
                                    String firstName, String lastName, Currency currency,
                                    String countryCode, boolean newAccount, boolean saveToTransactionData)
            throws AccountException {

        assertValidSubCasino(subCasinoId, bankInfo.getId());
        assertValidBankCurrency(currency, bankInfo);

        AccountInfo accountInfo = composeAccount(
                accountId,
                externalId,
                (int) bankInfo.getId(),
                subCasinoId,
                nickName,
                isGuest,
                isLocked,
                currency,
                countryCode);

        setEmailAndNames(email, firstName, lastName, accountInfo, bankInfo);

        saveAccount(
                accountInfo,
                clientType,
                false,
                null,
                null,
                null,
                null,
                bankInfo,
                null,
                null,
                null,
                null,
                null,
                null,
                newAccount,
                saveToTransactionData);

        return accountInfo;
    }

    public AccountInfo createAccount(String externalId, BankInfo bankInfo,
                                     short subCasinoId, String nickName, boolean isGuest,
                                     boolean isLocked, String email, ClientType clientType,
                                     String firstName, String lastName, String password, String agentId,
                                     boolean isAgent, Currency currency, String countryCode) throws AccountException {
        assertValidSubCasino(subCasinoId, bankInfo.getId());
        assertValidBankCurrency(currency, bankInfo);

        AccountInfo accountInfo = composeAccount(null, externalId, (int) bankInfo.getId(),
                subCasinoId, nickName, isGuest, isLocked, currency, countryCode);
        setEmailAndNames(email, firstName, lastName, accountInfo, bankInfo);
        accountInfo.setPassword(password);
        accountInfo.setAgentId(agentId);
        saveNewAccount(accountInfo, clientType, false, null, null, null, null, bankInfo, null, null, null, null,
                null, null, true);
        return accountInfo;
    }

    public AccountInfo createAccount(String externalId, BankInfo bankInfo, short subCasinoId, String nickName,
                                     boolean isGuest, boolean isLocked, String email, ClientType clientType,
                                     String firstName, String lastName, boolean isAgent, Currency currency,
                                     long initialBalance, Boolean gender, Long dateOfBirth, String addressId,
                                     String address, String city, String state, String country, String zip,
                                     String refType, Long refId)
            throws AccountException {
        assertValidSubCasino(subCasinoId, bankInfo.getId());
        assertValidBankCurrency(currency, bankInfo);

        AccountInfo accountInfo = composeAccount(null, externalId, (int) bankInfo.getId(),
                subCasinoId, nickName, isGuest, isLocked, currency, null);
        setEmailAndNames(email, firstName, lastName, accountInfo, bankInfo);
        try {
            accountInfo.setBalance(initialBalance);
        } catch (CommonException e) {
            LOG.error("Cannot set balance", e);
        }
        saveNewAccount(accountInfo, clientType, isAgent, gender, dateOfBirth, addressId, address, bankInfo, city,
                state, country, zip, refType, refId, true);
        return accountInfo;
    }

    public void update(long accountId, String userName, String firstName, String lastName, String email,
                       String password, String agentId, boolean locked)
            throws CommonException {
        AccountInfo accountInfo = getAccountInfo(accountId);
        if (accountInfo != null) {
            accountInfo.setNickName(userName);

            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId());
            setEmailAndNames(email, firstName, lastName, accountInfo, bankInfo);

            accountInfo.setPassword(password);
            accountInfo.setAgentId(agentId);
            accountInfo.setLocked(locked);
            update(accountInfo, true);
        }
    }

    private void setEmailAndNames(String email, String firstName, String lastName, AccountInfo accountInfo, BankInfo bankInfo) {
        if (bankInfo.isGDPROff()) {
            accountInfo.setEmail(email);
            accountInfo.setFirstName(firstName);
            accountInfo.setLastName(lastName);
        } else {
            accountInfo.setEmail(null);
            accountInfo.setFirstName(null);
            accountInfo.setLastName(null);
        }
    }

    public void lock(long accountId, boolean locked) throws CommonException {
        AccountInfo accountInfo = getAccountInfo(accountId);
        if (accountInfo != null) {
            accountInfo.setLocked(locked);
            update(accountInfo, true);
        }
    }

    public void update(AccountInfo account, boolean saveToCassandra) {
        if (account != null) {
            final BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(account.getBankId());
            persist(account, saveToCassandra && bankInfo.isPersistAccounts(), false, true);
            if (!isTrimmedEmpty(account.getAgentId())) {
                //nop, not ported functionality (agents)
            }
        }
    }

    public void updateIntegrationStatus(String agentId, boolean status, long subcasinoId, long bankId) {
        //nop, not ported functionality (agents)
    }

    @Override
    public String getAdditionalInfo() {
        return "";
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream) throws IOException {
        accountInfoPersister.processAll(new CacheExportProcessor<>(outStream));
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream, Long bankId) throws IOException {
        accountInfoPersister.exportAccounts(outStream, bankId);
    }

    @Override
    public AccountInfo getObject(String id) {
        return accountInfoPersister.get(Long.valueOf(id));
    }

    @Override
    public Map<Long, AccountInfo> getAllObjects() {
        return null;
    }

    @Override
    public int size() {
        return accountInfoPersister.size();
    }

    @Override
    public String printDebug() {
        return "accounts.size()=" + size();
    }

    public void put(AccountInfo accountInfo) {
        accountInfoPersister.persist(accountInfo, true);
    }

    @Override
    public void remove(String id) {
        //nop
    }

    @Override
    public AccountInfo getByAccountId(long accountId) throws CommonException {
        return getAccountInfo(accountId);
    }

    @Override
    public Pair<Integer, String> getBankIdExternalIdByAccountId(long accountId) throws CommonException {
        AccountInfo account = getAccountInfo(accountId, true);
        if (account == null) {
            throw new AccountException("Account not found, id=" + accountId);
        }
        return new Pair<>(account.getBankId(), account.getExternalId());
    }

    private void assertValidBankCurrency(Currency currency, BankInfo bankInfo) throws AccountException {
        if (currency != null && !bankInfo.isCurrencyCodeAllowed(currency.getCode())) {
            throw new AccountException(" Currency is not allowed, currencyCode=" + currency.getCode() +
                    ", bankInfo=" + bankInfo.getId());
        }
    }

    public void setFreeBalance(AccountInfo account, long gameId) {
        IBaseGameInfo<?, ?> gameInfo = BaseGameCache.getInstance().getGameInfoById(
                account.getBankId(), gameId, account.getCurrency());
        FreeGameCalculator freeGameCalculator = ApplicationContextHelper.getBean(FreeGameCalculator.class);
        account.setFreeBalance(freeGameCalculator.calculateFreeBalance(gameInfo, account.getCurrency().getCode()));
    }

    public void setCasinoSystemType(CasinoSystemType casinoSystemType) {
        this.casinoSystemType = casinoSystemType;
    }
}
