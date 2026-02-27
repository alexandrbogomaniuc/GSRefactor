package com.abs.casino.support;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraArchiverPersister;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.CalendarUtils;
import com.abs.casino.common.util.xml.ConcurrentHashMapXStreamConverter;
import com.abs.casino.common.util.xml.ConcurrentStringMapXStreamConverter;
import com.thoughtworks.xstream.XStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

/**
 * Created by grien on 06.02.15.
 */
public abstract class AbstractStorageArchiver<T, K> {

    private final Logger LOG = LogManager.getLogger(this.getClass());
    private static final String timeZone = "GMT";
    private static final int DEFAULT_END_PERIOD = 3;

    private final CassandraArchiverPersister archiverPersister;

    public AbstractStorageArchiver() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        archiverPersister = persistenceManager.getPersister(CassandraArchiverPersister.class);
    }

    public void archive(String outputPath, Date endRangePeriod, String timeZone, boolean needRemoveAfterArchive, DateFormat df) throws CommonException, IOException {
        info("Start archive: " + getName());
        final Long startDate = getLastArchiveData();
        final Date startRangePeriod = new Date(startDate);
        info("oldestRecordDate: " + startRangePeriod);
        if (endRangePeriod == null) {
            endRangePeriod = getEndDateByDefault();
        }
        if (startRangePeriod.after(endRangePeriod)) {
            throw new CommonException("startRange: " + startRangePeriod + " after endRange: " + endRangePeriod);
        }
        info("Archive range: " + startRangePeriod + " - " + endRangePeriod);
        Calendar currentStart = CalendarUtils.getStartDay(startRangePeriod, timeZone);
        while (currentStart.getTime().before(endRangePeriod)) {
            Date currentStartDate = currentStart.getTime();
            Date currentEndDate = CalendarUtils.getEndDay(currentStartDate, timeZone).getTime();
            info("Processing range: " + currentStartDate + " - " + currentEndDate);
            processDayItems(outputPath, currentStartDate, currentEndDate, needRemoveAfterArchive, df);
            currentStart.add(Calendar.HOUR, 24);
            info("****************************************************");
        }
    }

    public abstract String getName();

    private Long getLastArchiveData() {
        return archiverPersister.getLastArchiveDate(getColumnFamilyName(), getDefaultStartPeriod());
    }

    private Date getEndDateByDefault() {
        int defaultEndPeriod = getDefaultEndPeriod();
        Calendar now = Calendar.getInstance();
        now.setTimeZone(TimeZone.getTimeZone(timeZone));
        now.add(Calendar.MONTH, -defaultEndPeriod);
        info("End date not defined, use -" + defaultEndPeriod + " Month rule: " + now.getTime());
        return now.getTime();
    }

    protected abstract String getColumnFamilyName();

    protected abstract int getDefaultStartPeriod();

    protected int getDefaultEndPeriod() {
        return DEFAULT_END_PERIOD;
    }

    protected void processDayItems(String outputPath, Date currentStartDate, Date currentEndDate, boolean needRemoveAfterArchive, DateFormat df)
            throws CommonException, IOException {
        String outFileName = getOutFileName(outputPath, currentStartDate, df);
        ObjectOutputStream outStream = createObjectOutputStream(outFileName);
        List<K> needRemoveIdentifiers = needRemoveAfterArchive ? new ArrayList<K>() : null;
        try {
            long count = 0;
            System.out.print(addSystemLogPrefix("Write.. Number of wrote records:0 "));
            Iterable<T> records = getRecords(currentStartDate, currentEndDate);
            for (T record : records) {
                outStream.writeObject(record);
                if (needRemoveAfterArchive) {
                    addNeedRemoveIdentifier(record, needRemoveIdentifiers);
                }
                afterWriteRecord(record, needRemoveAfterArchive);
                count++;
                if (count % 10000 == 0) {
                    System.out.print(addSystemLogPrefix(count + " "));
                }
            }
            System.out.println();
            info("Finished. Total number of wrote records: " + count);
            System.gc();
        } finally {
            close(outFileName, outStream);
        }
        if (needRemoveAfterArchive) {
            info("Removing archived records...");
            remove(needRemoveIdentifiers);
        }
        updateLastArchiveDate(currentEndDate);
    }

    protected abstract Iterable<T> getRecords(Date dayStartDate, Date dayEndDate) throws CommonException;

    protected abstract void remove(List<K> needRemoveIdentifiers);

    protected abstract void addNeedRemoveIdentifier(T record, List<K> needRemoveIdentifiers);

    protected void updateLastArchiveDate(Date currentEndDate) {
        archiverPersister.persist(getColumnFamilyName(), currentEndDate.getTime());
    }

    protected void afterWriteRecord(T record, boolean needRemoveAfterArchive) {
        //noop
    }

    protected ObjectOutputStream createObjectOutputStream(String outFileName) throws IOException, CommonException {
        File outFile = new File(outFileName);
        if (outFile.exists()) {
            info("Export file already exist: " + outFile + ", if previous export has been failed, just " +
                    "remove file and run again");
            throw new CommonException("Export file already exist: " + outFile);
        }
        return getXStream().createObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outFile, false), 64 * 1024));
    }

    protected String getOutFileName(String outputPath, Date currentStartDate, DateFormat df) {
        return outputPath + "/" + getName() + "_" + df.format(currentStartDate) + ".xml";
    }

    protected void close(String sessionsFile, ObjectOutputStream outStream) {
        if (outStream != null) {
            try {
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
                error("Cannon close out stream, file: " + sessionsFile, e);
            }
        }
    }

    private static XStream getXStream() {
        XStream xstream = new XStream();
        xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
        xstream.registerConverter(new ConcurrentStringMapXStreamConverter(xstream.getMapper()), XStream.PRIORITY_NORMAL);
        xstream.registerConverter(new ConcurrentHashMapXStreamConverter(xstream.getMapper()), XStream.PRIORITY_NORMAL);
        return xstream;
    }

    public void info(String s) {
        System.out.println(addSystemLogPrefix(s));
        LOG.debug(s);
    }

    private String addSystemLogPrefix(String s) {
        return getName() + "::" + s;
    }

    public void error(String s, Exception e) {
        System.err.println(addSystemLogPrefix("Error: " + s + ": " + e.getMessage()));
        LOG.error(s, e);
    }
}