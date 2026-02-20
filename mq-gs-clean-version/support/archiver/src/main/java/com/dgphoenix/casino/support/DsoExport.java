package com.dgphoenix.casino.support;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.cache.CachesHolder;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.config.CommonContextConfiguration;
import com.dgphoenix.casino.common.configuration.CasinoSystemType;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.gs.SharedGameServerComponentsConfiguration;
import com.dgphoenix.casino.gs.maintenance.CacheExporter;
import com.dgphoenix.casino.init.CassandraPersistenceContextConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.*;

/**
 * User: flsh
 * Date: 9/14/11
 */
public class DsoExport {
    private static final Logger LOG = LogManager.getLogger(DsoExport.class);
    private final PeriodFormatter formatter;
    private final String outputPath;
    private final CacheExporter cacheExporter;
    private final ApplicationContext applicationContext;

    public DsoExport(String outputPath) {
        this.outputPath = outputPath;
        formatter = new PeriodFormatterBuilder()
                .appendHours().appendSuffix("h ")
                .appendMinutes().appendSuffix("m ")
                .appendSeconds().appendSuffix("s")
                .toFormatter();
        applicationContext = getApplicationContext();
        cacheExporter = CacheExporter.getInstance();
    }

    private ApplicationContext getApplicationContext() {
        return new AnnotationConfigApplicationContext(CommonContextConfiguration.class,
                CassandraPersistenceContextConfiguration.class, SharedGameServerComponentsConfiguration.class);
    }

    public void exportAll() throws CommonException {
        long exportStart = System.currentTimeMillis();
        cacheExporter.exportAll(outputPath);
        long exportFinish = System.currentTimeMillis();
        Duration exportDuration = new Duration(exportFinish - exportStart);
        info("DsoExport:: export finished in " + formatter.print(exportDuration.toPeriod()));
    }

    public void exportBank(Long bankId) throws CommonException {
        long exportStart = System.currentTimeMillis();
        cacheExporter.exportAll(outputPath, bankId);
        long exportFinish = System.currentTimeMillis();
        Duration exportDuration = new Duration(exportFinish - exportStart);
        info("DsoExport:: export finished in " + formatter.print(exportDuration.toPeriod()));
    }

    public void exportSubCasino(Long subCasinoId, String banks) throws CommonException {
        long exportStart = System.currentTimeMillis();
        cacheExporter.exportSubCasino(outputPath, subCasinoId, banks);
        long exportFinish = System.currentTimeMillis();
        Duration exportDuration = new Duration(exportFinish - exportStart);
        info("DsoExport:: export finished in " + formatter.print(exportDuration.toPeriod()));
    }

    public void exportSubCasinoSingleFile(OutputStream stream, Long subCasinoId, String bankId) throws IOException {
        long exportStart = System.currentTimeMillis();
        cacheExporter.exportSubCasinoToSingleFile(stream, subCasinoId, bankId);
        long exportFinish = System.currentTimeMillis();
        Duration exportDuration = new Duration(exportFinish - exportStart);
        info("DsoExport:: export finished in " + formatter.print(exportDuration.toPeriod()));
    }

    public void importAll(String importPath) throws CommonException {
        long importStart = System.currentTimeMillis();
        CachesHolder cachesHolder = ApplicationContextHelper.getApplicationContext()
                .getBean("cachesHolder", CachesHolder.class);
        cacheExporter.importAll(importPath, cachesHolder);
        long importFinish = System.currentTimeMillis();
        Duration importDuration = new Duration(importFinish - importStart);
        info("DsoExport:: import finished in " + formatter.print(importDuration.toPeriod()));
    }

    public void importSubCasino(Long subCasinoId) throws CommonException {
        long importStart = System.currentTimeMillis();
        cacheExporter.importSubCasino(outputPath, subCasinoId);
        long importFinish = System.currentTimeMillis();
        Duration importDuration = new Duration(importFinish - importStart);
        info("DsoExport:: import finished in " + formatter.print(importDuration.toPeriod()));
    }

    public void importSubCasinoSingleFile(InputStream stream) throws CommonException {
        long importStart = System.currentTimeMillis();
        cacheExporter.importSubCasinoFromSingleFile(stream);
        long importFinish = System.currentTimeMillis();
        Duration importDuration = new Duration(importFinish - importStart);
        info("DsoExport:: import finished in " + formatter.print(importDuration.toPeriod()));
    }

    public static void info(String s) {
        System.out.println(s);
        LOG.debug(s);
    }

    public static void error(String s, Exception e) {
        System.err.println("Error: " + s + ": " + e.getMessage());
        LOG.error(s, e);
    }

    public static void error(String s) {
        System.err.println("Error: " + s);
        LOG.error(s);
    }

    public static void main(String[] args) throws CommonException, IOException {
        info("Running!");
        if (args.length < 1) {
            info("Options: [export mode - entire system] run.sh outputPath");
            info("Options: [export mode - entire bank] run.sh outputPath bankId");
            info("Options: [export mode - entire subCasino configs] run.sh outputPath subCasinoId exportSubCasino");
            info("Options: [export mode - one bank or all subCasino banks configs] run.sh " +
                    "outputPath subCasinoId exportSubCasino bankId|all");
            info("Options: [export mode - to single file] run.sh outputFile exportToSingleFile subCasinoId  bankId");
            info("Options: [import mode - to single file] run.sh inputFile importFromSingleFile");
            info("Options: [import mode - import subCasino configs] run.sh outputPath subCasinoId importSubCasino");
            info("Options: [import mode - entire system] run.sh outputPath import");
            return;
        }
        String outPath = args[0];
        String sysProp = System.getProperty(CacheExporter.CASINO_SYSTEM_TYPE);
        CasinoSystemType systemType = sysProp != null ? CasinoSystemType.valueOf(sysProp) : CasinoSystemType.MULTIBANK;
        info("Used CasinoSystemType: " + systemType);
        DsoExport archiver = new DsoExport(outPath);
        ApplicationContextHelper.getBean(AccountManager.class).setCasinoSystemType(systemType);
        if (args.length > 1 && "import".equalsIgnoreCase(args[1])) {
            info("Entering import mode");
            if (BaseGameCache.getInstance().size() > 0) {
                error("Cannot import, BaseGameCache is not empty");
                System.exit(-1);
            }
            if (BankInfoCache.getInstance().size() > 0) {
                error("Cannot import, BankInfoCache is not empty");
                System.exit(-1);
            }
            if (CurrencyCache.getInstance().size() > 0) {
                error("Cannot import, CurrencyCache is not empty");
                System.exit(-1);
            }
            archiver.importAll(outPath);
            info("Import completed!");
            System.exit(0);
        }
        if (args.length > 1) {
            if ("exportToSingleFile".equalsIgnoreCase(args[1])) {
                Long subCasinoId = Long.valueOf(args[2]);
                String bankId = args[3];
                OutputStream stream = new BufferedOutputStream(new FileOutputStream(outPath, false), 256 * 1024);
                archiver.exportSubCasinoSingleFile(stream, subCasinoId, bankId);
                System.exit(0);
            } else if ("importFromSingleFile".equalsIgnoreCase(args[1])) {
                FileInputStream stream = new FileInputStream(outPath);
                archiver.importSubCasinoSingleFile(stream);
                System.exit(0);
            }
        }
        Long exportedObjectId = args.length > 1 ? Long.valueOf(args[1]) : null;
        try {
            if (exportedObjectId == null) {
                archiver.exportAll();
            } else {
                if (args.length == 2) {
                    archiver.exportBank(exportedObjectId);
                } else {
                    boolean exportSubCasinoMode = args.length > 2 && "exportSubCasino".equalsIgnoreCase(args[2]);
                    if (exportSubCasinoMode) {
                        String banks = args.length >= 4 ? args[3] : "all";
                        info("Running export for subCasinoId: " + exportedObjectId + " banks=" + banks);
                        archiver.exportSubCasino(exportedObjectId, banks);
                    } else {
                        boolean importSubCasinoMode = args.length > 2 && "importSubCasino".equalsIgnoreCase(args[2]);
                        if (importSubCasinoMode) {
                            info("Running import for subCasinoId: " + exportedObjectId);
                            archiver.importSubCasino(exportedObjectId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            error("Archive error", e);
            System.exit(-1);
        }
        info("Export completed!");
        System.exit(0);
    }
}
