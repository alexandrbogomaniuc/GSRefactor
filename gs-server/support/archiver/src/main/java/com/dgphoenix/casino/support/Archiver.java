package com.dgphoenix.casino.support;

import com.abs.casino.common.config.CommonContextConfiguration;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.gs.SharedGameServerComponentsConfiguration;
import com.dgphoenix.casino.init.CassandraPersistenceContextConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: flsh
 * Date: 9/14/11
 */
public class Archiver {
    private static final Logger LOG = LogManager.getLogger(Archiver.class);
    private static final DateFormat DF = new SimpleDateFormat("dd.MM.yyyy");
    private static final String timeZone = "GMT";

    private static Map<String, AbstractStorageArchiver> archivers = new HashMap<>();
    private static ApplicationContext applicationContext;

    static {
        applicationContext = getApplicationContext();
        register(new CassandraBetHistoryArchiver());
        register(new CassandraGameSessionArchiver());
        register(new CassandraGameSessionAndBetHistoryArchiver());
        register(new CassandraPlayerSessionArchiver());
        register(new CassandraWalletOperationArchiver());
        register(new CassandraBonusArchiver());
        register(new CassandraFRBonusArchiver());
    }

    private final String outputPath;
    private final Date endRangePeriod;

    public Archiver(String outputPath, Date endRangePeriod) {
        this.outputPath = outputPath;
        this.endRangePeriod = endRangePeriod;
    }

    private static void register(AbstractStorageArchiver archiver) {
        archivers.put(archiver.getName(), archiver);
    }

    public void export(String archiveType, boolean clearDbRecords) throws CommonException, IOException {
        AbstractStorageArchiver archiver = archivers.get(archiveType);
        if (archiver == null) {
            System.err.println("Unsupported archiver: " + archiveType + ", possible: " + StringUtils.join(archivers.keySet(), " "));
            throw new CommonException("Unsupported archiver: " + archiveType);
        }
        archiver.archive(this.outputPath, this.endRangePeriod, timeZone, clearDbRecords, DF);
    }

    public static void main(String[] args) throws Exception {
        TimeZone tz = TimeZone.getTimeZone(timeZone);
        info("Used timeZone=" + tz);
        info("Running!");
        int argumentNumber = args.length;
        if (argumentNumber < 2) {
            List<String> names = new ArrayList<>(archivers.keySet());
            Collections.sort(names);
            info("Options: run.sh outputPath " + StringUtils.join(names, '|') + " highRangePeriod[dd.mm.yyyy]");
            return;
        }
        try {
            Date endDate = null;
            if (argumentNumber >= 3) {
                endDate = DF.parse(args[2]);
            }
            //clear must be disabled, data removed automatically by ColumnFamily TTL
            boolean clear = false;
            if (argumentNumber >= 4 && "false".equalsIgnoreCase(args[3])) {
                //clear = false;
            }
            //No need to init caches, caches already inited in CachesHolder Bean
            //CacheExporter.getInstance().initCashes(applicationContext);
            Archiver archiver = new Archiver(args[0], endDate);
            archiver.export(args[1], clear);
        } catch (Exception e) {
            error("Archive error", e);
            System.exit(-1);
        }
        System.exit(0);
    }

    private static ApplicationContext getApplicationContext() {
        return new AnnotationConfigApplicationContext(CommonContextConfiguration.class,
                CassandraPersistenceContextConfiguration.class, SharedGameServerComponentsConfiguration.class);
    }

    private static void info(String s) {
        System.out.println(s);
        LOG.debug(s);
    }

    private static void error(String s, Exception e) {
        System.err.println("Error: " + s + ": " + e.getMessage());
        LOG.error(s, e);
    }
}
