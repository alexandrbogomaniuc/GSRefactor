/**
 * User: flsh
 * Date: 17.09.2009
 */
package com.abs.casino.gs.maintenance;

import com.abs.casino.common.cache.ExportableCacheEntry;
import com.abs.casino.common.cache.AbstractExportableCache;
import com.abs.casino.account.AccountManager;
import com.abs.casino.cache.CachesHolder;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraBankInfoPersister;
import com.abs.casino.cassandra.persist.CassandraBaseGameInfoPersister;
import com.abs.casino.common.cache.*;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.bank.SubCasino;
import com.abs.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.cache.data.game.BaseGameInfo;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.xml.ConcurrentHashMapXStreamConverter;
import com.abs.casino.gs.maintenance.converters.BaseGameInfoConverter;
import com.abs.casino.gs.maintenance.converters.CurrencyConverter;
import com.abs.casino.gs.maintenance.converters.ExportableCacheEntryConverter;
import com.abs.casino.gs.maintenance.ICacheExporter;
import com.abs.casino.gs.managers.payment.currency.CurrencyManager;
import com.abs.casino.gs.persistance.remotecall.RefreshConfigCall;
import com.abs.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.abs.casino.system.configuration.GameServerConfiguration;
import com.thoughtworks.xstream.XStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class CacheExporter {
    public static final String CASINO_SYSTEM_TYPE = "CASINO_SYSTEM_TYPE";
    private static final Logger LOG = LogManager.getLogger(CacheExporter.class);
    private static final String FILE_EXTENSION = ".xml";
    private static final CacheExporter instance = new CacheExporter();
    private RemoteCallHelper remoteCallHelper;
    private GameServerConfiguration gameServerConfiguration;
    private AccountManager accountManager;
    private CachesHolder cachesHolder;
    private final CurrencyCache currencyCache;
    private final SubCasinoCache subCasinoCache;
    private final BankInfoCache bankInfoCache;
    private final BaseGameCache baseGameCache;
    private volatile CassandraBankInfoPersister bankInfoPersister;
    private volatile CassandraBaseGameInfoPersister baseGameInfoPersister;

    public static CacheExporter getInstance() {
        return instance;
    }

    private CacheExporter() {
        currencyCache = CurrencyCache.getInstance();
        subCasinoCache = SubCasinoCache.getInstance();
        bankInfoCache = BankInfoCache.getInstance();
        baseGameCache = BaseGameCache.getInstance();
    }

    private CassandraPersistenceManager getPersistenceManager() {
        ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
        if (applicationContext == null) {
            throw new IllegalStateException("Spring applicationContext is not initialized for CacheExporter");
        }
        return applicationContext.getBean("persistenceManager", CassandraPersistenceManager.class);
    }

    private CassandraBankInfoPersister getBankInfoPersister() {
        CassandraBankInfoPersister persister = bankInfoPersister;
        if (persister != null) {
            return persister;
        }
        synchronized (this) {
            persister = bankInfoPersister;
            if (persister == null) {
                persister = getPersistenceManager().getPersister(CassandraBankInfoPersister.class);
                bankInfoPersister = persister;
            }
        }
        return persister;
    }

    private CassandraBaseGameInfoPersister getBaseGameInfoPersister() {
        CassandraBaseGameInfoPersister persister = baseGameInfoPersister;
        if (persister != null) {
            return persister;
        }
        synchronized (this) {
            persister = baseGameInfoPersister;
            if (persister == null) {
                persister = getPersistenceManager().getPersister(CassandraBaseGameInfoPersister.class);
                baseGameInfoPersister = persister;
            }
        }
        return persister;
    }

    private RemoteCallHelper getRemoteCallHelper() {
        if(remoteCallHelper == null) {
            remoteCallHelper = ApplicationContextHelper.getApplicationContext().getBean("remoteCallHelper", RemoteCallHelper.class);
        }
        return remoteCallHelper;
    }

    private GameServerConfiguration getGameServerConfiguration() {
        if(gameServerConfiguration == null) {
            gameServerConfiguration = ApplicationContextHelper.getApplicationContext().getBean("gameServerConfiguration",
                    GameServerConfiguration.class);
        }
        return gameServerConfiguration;
    }

    private AccountManager getAccountManager() {
        if(accountManager == null) {
            ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
            if (applicationContext != null) {
                accountManager = applicationContext.getBean(AccountManager.class);
            }
        }
        return accountManager;
    }

    private CachesHolder getCachesHolder() {
        if(cachesHolder == null) {
            cachesHolder = ApplicationContextHelper.getApplicationContext().getBean("cachesHolder", CachesHolder.class);
        }
        return cachesHolder;
    }

    private XStream getXStream(AbstractExportableCache cache) {
        XStream xstream = new XStream();
        XStream.setupDefaultSecurity(xstream);
        xstream.allowTypesByWildcard(new String[]{"com.dgphoenix.casino.**", "com.abs.casino.**"});
        xstream.aliasPackage("com.dgphoenix.casino", "com.abs.casino");
        // Local bootstrap files can contain fields from older schema revisions.
        xstream.ignoreUnknownElements();
        xstream.setMode(cache.isNoReferenceMode() ? XStream.NO_REFERENCES : XStream.XPATH_ABSOLUTE_REFERENCES);
        //jackpots
        xstream.registerConverter(new ExportableCacheEntryConverter());
        if (!(cache instanceof CurrencyCache)) { //currency cache must be exported in full form (with code and symbol)
            xstream.registerConverter(new CurrencyConverter());
        }
        xstream.registerConverter(new BaseGameInfoConverter());
        xstream.registerConverter(new ConcurrentHashMapXStreamConverter(xstream.getMapper()), XStream.PRIORITY_NORMAL);
        return xstream;
    }

    public void exportAll(final String exportPath) throws CommonException {
        exportAll(exportPath, null);
    }

    private boolean containsBankInfoId(List<BankInfo> banks, long bankId, String currency) {
        for (BankInfo bi : banks) {
            if (bi.getId() == bankId) {
                return currency == null
                        || bi.getCurrencies().contains(currencyCache.get(currency));
            }
        }
        return false;
    }

    private boolean containsGameInfoId(List<BaseGameInfo> games, long gameId) {
        for (BaseGameInfo bgi : games) {
            if (bgi.getId() == gameId) {
                return true;
            }
        }
        return false;
    }


    public long importSubCasinoFromSingleFile(InputStream stream) throws CommonException {
        SubCasino subCasino = null;
        List<BankInfo> banks = new ArrayList<>();
        List<BaseGameInfo> games = new ArrayList<>();

        XStream xStream = getXStream(subCasinoCache);
        try (ObjectInputStream inStream = xStream.createObjectInputStream(stream)) {
            while (true) {
                ExportableCacheEntry entry = (ExportableCacheEntry) inStream.readObject();
                if (entry == null) { //or EOFException
                    break;
                }
                final IDistributedCacheEntry importEntry = entry.getValue();
                if (importEntry instanceof SubCasino) {
                    if (subCasino != null) {
                        throw new CommonException("Only one SubCasino entry allowed in import file");
                    }
                    subCasino = (SubCasino) importEntry;
                } else if (importEntry instanceof BankInfo) {
                    final BankInfo bankInfo = (BankInfo) importEntry;
                    if (bankInfoCache.getBankInfo(bankInfo.getId()) != null) {
                        throw new CommonException("BankInfo already exist, bankId = " + bankInfo.getId());
                    }
                    banks.add(bankInfo);
                } else if (importEntry instanceof BaseGameInfo) {
                    BaseGameInfo game = (BaseGameInfo) importEntry;
                    games.add(game);
                } else {
                    LOG.warn("importSubCasinoFromSingleFile: unsupported entry = {}", importEntry);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new CommonException("ClassNotFoundException", e);
        } catch (EOFException e) {
            LOG.warn("CacheExporter:: import: File ended");
        } catch (IOException ioe) {
            throw new CommonException("Can't read stream", ioe);
        }

        SubCasino existSubCasino = subCasinoCache.get(subCasino.getId());
        if (existSubCasino == null) {
            subCasinoCache.put(subCasino);
        } else {
            final List<Long> bankIds = subCasino.getBankIds();
            for (Long bankId : bankIds) {
                existSubCasino.addBankId(bankId);
            }
            final List<String> domainNames = subCasino.getDomainNames();
            for (String domainName : domainNames) {
                existSubCasino.addDomainName(domainName);
            }
            subCasino = existSubCasino;
        }
        getRemoteCallHelper().saveAndSendNotification(subCasino);
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            LOG.error("error", e);
            Thread.currentThread().interrupt();
        }
        for (BankInfo bank : banks) {
            bank.setSubCasinoId(subCasino.getId());
            LOG.info("importSubCasinoFromSingleFile: import bank: {}", bank);
            getRemoteCallHelper().saveAndSendNotification(bank);
        }
        for (BaseGameInfo game : games) {
            //fix new local reference to DSO currency
            if (game.getCurrency() != null) {
                Currency currency = currencyCache.get(game.getCurrency().getCode());
                if (currency == null) {
                    currency = CurrencyManager.getInstance().setupCurrency(game.getCurrency().getCode(),
                            game.getCurrency().getSymbol(), game.getBankId());
                }
                game.setCurrency(currency);
            }

            baseGameCache.put(game);
            getRemoteCallHelper().saveAndSendNotification(game);
        }
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            LOG.error("error", e);
            Thread.currentThread().interrupt();
        }
        for (BankInfo bank : banks) {
            long bankId = bank.getId();
            final BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
            if (bankInfo == null) {
                LOG.error("BankInfo not found: {}", bankId);
            } else {
                final List<Currency> currencies = bankInfo.getCurrencies();
                for (Currency currency : currencies) {
                    CurrencyManager.getInstance().setupCurrency(currency.getCode(), currency.getSymbol(), bankId);
                }
            }
        }

        LOG.warn("importSubCasinoFromSingleFile: import subCasino completed: {}", subCasino);
        return subCasino.getId();
    }

    public void importSubCasino(final String exportPath, final Long subCasinoId) throws CommonException {
        importCache(subCasinoCache, exportPath);
        final SubCasino subCasino = subCasinoCache.get(subCasinoId);
        checkArgument(subCasino != null, "Subcasino not found: " + subCasinoId);
        getRemoteCallHelper().saveAndSendNotification(subCasino);

        importCache(bankInfoCache, exportPath);
        importCache(baseGameCache, exportPath);
        getBaseGameInfoPersister().saveAll();

        final List<Long> bankIds = new ArrayList<>(subCasino.getBankIds());
        if (!bankIds.contains(subCasino.getDefaultBank())) {
            bankIds.add(subCasino.getDefaultBank());
        }
        for (Long bankId : bankIds) {
            final BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
            final List<Currency> currencies = bankInfo.getCurrencies();
            for (Currency currency : currencies) {
                CurrencyManager.getInstance().setupCurrency(currency.getCode(),
                        currency.getSymbol(), bankId);
            }
            bankInfo.setSubCasinoId(subCasino.getId());
            getBankInfoPersister().persist(bankInfo.getId(), bankInfo);
            getRemoteCallHelper().sendCallToAllServers(new RefreshConfigCall(
                    BankInfoCache.class.getCanonicalName(), String.valueOf(bankInfo.getId())));

        }
    }

    public void exportSubCasino(final String exportPath, final Long subCasinoId, String bankId) throws CommonException {
        final SubCasino subCasino = subCasinoCache.get(subCasinoId);
        checkArgument(subCasino != null, "Subcasino not found: " + subCasinoId);
        exportCache(outStream -> subCasinoCache.exportEntry(outStream, subCasino),
                subCasinoCache, exportPath);

        final List<Long> bankIds = new ArrayList<>();
        if (bankId.equalsIgnoreCase("all")) {
            bankIds.addAll(subCasino.getBankIds());
            if (!bankIds.contains(subCasino.getDefaultBank())) {
                bankIds.add(subCasino.getDefaultBank());
            }
        } else {
            bankIds.add(Long.valueOf(bankId));
        }
        exportCache(outStream -> bankInfoCache.exportEntry(outStream, bankIds),
                bankInfoCache, exportPath);

        exportCache(outStream -> baseGameCache.exportEntries(outStream, bankIds),
                baseGameCache, exportPath);
    }

    public void exportSubCasinoToSingleFile(OutputStream stream, final Long subCasinoId, String bankId) throws IOException {
        XStream xStream = getXStream(subCasinoCache);
        try (final ObjectOutputStream outStream = xStream.createObjectOutputStream(stream)) {
            final SubCasino subCasino = subCasinoCache.get(subCasinoId);
            if (subCasino == null) {
                LOG.error("SubCasino not found: {}", subCasinoId);
                System.err.println("SubCasino not found: " + subCasinoId);
                if (bankId.equalsIgnoreCase("all") || StringUtils.isTrimmedEmpty(bankId)) {
                    throw new IllegalArgumentException("Subcasino not found: " + subCasinoId);
                }
            } else {
                subCasinoCache.exportEntry(outStream, subCasino);
                LOG.info("Export: {}", subCasino);
            }

            final List<Long> bankIds = new ArrayList<>();
            if (subCasino != null && (StringUtils.isTrimmedEmpty(bankId) || bankId.equalsIgnoreCase("all"))) {
                bankIds.addAll(subCasino.getBankIds());
                if (!bankIds.contains(subCasino.getDefaultBank())) {
                    bankIds.add(subCasino.getDefaultBank());
                }
            } else {
                bankIds.add(Long.valueOf(bankId));
            }
            bankInfoCache.exportEntry(outStream, bankIds);
            baseGameCache.exportEntries(outStream, bankIds);
        }
    }

    public void exportAll(final String exportPath, final Long bankId) throws CommonException {
        Thread exportAccountsCache = new Thread(() -> {
            try {
                exportCache(getAccountManager(), exportPath, bankId);
            } catch (CommonException e) {
                LOG.error("Can't export AccountsCache", e);
            }
        });
        exportAccountsCache.setPriority(Thread.MAX_PRIORITY);
        exportAccountsCache.start();

        Collection<AbstractExportableCache> caches = getCachesHolder().getExportableCaches();
        for (AbstractExportableCache cache : caches) {
            if (!(cache instanceof AccountManager)) {
                exportCache(cache, exportPath, bankId);
            }
        }
        while (exportAccountsCache.isAlive()) {
            try {
                exportAccountsCache.join(60000);
                LOG.warn("AccountsCache export in progress");
            } catch (InterruptedException e) {
                LOG.error("Export accounts error", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void importAll(final String importPath, CachesHolder cachesHolder) throws CommonException {
        importCache(currencyCache, importPath);
        importCache(ServerConfigsTemplateCache.getInstance(), importPath);
        AccountManager accountManager = getAccountManager();
        if (accountManager != null && accountManager.size() > 0) {
            throw new CommonException("Can't import caches! Empty first!");
        }
        Thread importAccountsCache = null;
        if (accountManager != null) {
            AccountManager finalAccountManager = accountManager;
            importAccountsCache = new Thread(() -> {
                try {
                    importCache(finalAccountManager, importPath);
                } catch (CommonException e) {
                    LOG.error("Can't import AccountsCache", e);
                }
                LOG.error("Import AccountsCache completed");
            });
            importAccountsCache.start();
        } else {
            LOG.warn("CacheExporter:: skip AccountManager import because applicationContext is not ready yet");
        }
        Collection<AbstractExportableCache> caches = cachesHolder.getExportableCaches();
        for (AbstractExportableCache cache : caches) {
            if (cache instanceof AccountManager || cache instanceof CurrencyCache ||
                    cache instanceof ServerConfigsTemplateCache) {
                continue;
            }
            importCache(cache, importPath);
        }
        while (importAccountsCache != null && importAccountsCache.isAlive()) {
            try {
                importAccountsCache.join(60000);
                LOG.warn("AccountsCache import in progress");
            } catch (InterruptedException e) {
                LOG.error("Import accounts error", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void exportCache(ICacheExporter exporter, AbstractExportableCache cache, String exportPath) throws CommonException {
        LOG.warn("CacheExporter:: start export: {}", cache.getClass().getCanonicalName());
        String outFile = exportPath + cache.getClass().getCanonicalName() + FILE_EXTENSION;
        XStream xStream = getXStream(cache);
        try (ObjectOutputStream outStream =
                     xStream.createObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outFile, false), 256 * 1024))) {
            exporter.export(outStream);
        } catch (FileNotFoundException e) {
            throw new CommonException("Can't open file " + outFile, e);
        } catch (IOException ioe) {
            throw new CommonException("Can't write file " + outFile, ioe);
        } catch (Exception e) {
            LOG.error("Can't export cache: {}", cache, e);
            return;
        }
        LOG.warn("CacheExporter:: end export: {}", cache.getClass().getCanonicalName());
    }

    public void exportCache(AbstractExportableCache cache, String exportPath, Long bankId) throws CommonException {
        LOG.warn("CacheExporter:: start export: {}", cache.getClass().getCanonicalName());
        String outFile = exportPath + cache.getClass().getCanonicalName() + FILE_EXTENSION;
        XStream xStream = getXStream(cache);
        try (ObjectOutputStream outStream =
                     xStream.createObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outFile, false), 256 * 1024))) {
            if (bankId == null) {
                cache.exportEntries(outStream);
            } else {
                cache.exportEntries(outStream, bankId);
            }
        } catch (FileNotFoundException e) {
            throw new CommonException("Can't open file " + outFile, e);
        } catch (IOException ioe) {
            throw new CommonException("Can't write file " + outFile, ioe);
        } catch (Exception e) {
            LOG.error("Can't export cache: {}", cache, e);
            return;
        }
        LOG.warn("CacheExporter:: end export: {}", cache.getClass().getCanonicalName());
    }

    public void importCache(AbstractExportableCache cache) throws CommonException {
        importCache(cache, getGameServerConfiguration().getExportCachePath());
    }

    public void importCache(AbstractExportableCache cache, String importPath) throws CommonException {
        LOG.warn("CacheExporter:: start import: {}", cache.getClass().getCanonicalName());
        int count = 0;
        String inFile = resolveImportFilePath(cache, importPath);
        XStream xStream = getXStream(cache);
        try (ObjectInputStream inStream = xStream.createObjectInputStream(new FileInputStream(inFile))) {
            while (true) {
                ExportableCacheEntry entry = (ExportableCacheEntry) inStream.readObject();
                if (entry == null) { //or EOFException
                    break;
                }
                cache.importEntry(entry);
                count++;
            }
        } catch (ClassNotFoundException e) {
            throw new CommonException("ClassNotFoundException", e);
        } catch (FileNotFoundException e) {
            if (cache.isRequiredForImport()) {
                throw new CommonException("Can't open file " + inFile, e);
            } else {
                LOG.warn("Import file not found, but not required, silent exit: {}", e.getMessage());
            }
        } catch (EOFException e) {
            LOG.warn("CacheExporter:: import: File ended: {}", inFile);
        } catch (IOException ioe) {
            throw new CommonException("Can't read file " + inFile, ioe);
        }
        LOG.warn("CacheExporter:: end import: {}, count = {}", cache.getClass().getCanonicalName(), count);
    }

    public List<ExportableCacheEntry> loadAsList(AbstractExportableCache cache, String exportPath) throws CommonException {
        List<ExportableCacheEntry> result = new ArrayList<>();
        LOG.warn("CacheExporter:: start import: {}", cache.getClass().getCanonicalName());
        int count = 0;
        String inFile = resolveImportFilePath(cache, exportPath);
        XStream xStream = getXStream(cache);
        try (ObjectInputStream inStream = xStream.createObjectInputStream(new FileInputStream(inFile))) {
            while (true) {
                ExportableCacheEntry entry = (ExportableCacheEntry) inStream.readObject();
                if (entry == null) { //or EOFException
                    break;
                }
                result.add(entry);
                count++;
            }
        } catch (ClassNotFoundException e) {
            throw new CommonException("ClassNotFoundException", e);
        } catch (FileNotFoundException e) {
            if (cache.isRequiredForImport()) {
                throw new CommonException("Can't open file " + inFile, e);
            } else {
                LOG.warn("Import file not found, but not required, silent exit: {}", e.getMessage());
            }
        } catch (EOFException e) {
            LOG.warn("CacheExporter:: import: File ended: {}", inFile);
        } catch (IOException ioe) {
            throw new CommonException("Can't read file " + inFile, ioe);
        }
        LOG.warn("CacheExporter:: end import: {}, count = {}", cache.getClass().getCanonicalName(), count);
        return result;
    }

    private String resolveImportFilePath(AbstractExportableCache cache, String importPath) {
        String canonicalName = cache.getClass().getCanonicalName();
        String primaryPath = importPath + canonicalName + FILE_EXTENSION;
        File primaryFile = new File(primaryPath);
        if (primaryFile.exists()) {
            return primaryPath;
        }
        if (canonicalName.startsWith("com.abs.casino.")) {
            String legacyPath = importPath + canonicalName.replace("com.abs.casino.", "com.dgphoenix.casino.") + FILE_EXTENSION;
            File legacyFile = new File(legacyPath);
            if (legacyFile.exists()) {
                LOG.info("CacheExporter:: fallback to legacy import file: {}", legacyPath);
                return legacyPath;
            }
        }
        return primaryPath;
    }
}
