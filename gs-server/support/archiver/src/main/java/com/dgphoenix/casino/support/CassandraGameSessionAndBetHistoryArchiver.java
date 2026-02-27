package com.abs.casino.support;

import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by grien on 06.02.15.
 */
public class CassandraGameSessionAndBetHistoryArchiver extends CassandraGameSessionArchiver {
    private CassandraBetHistoryArchiver betHistoryArchiver = new CassandraBetHistoryArchiver();
    private List<Long> betHistoryNeedRemove;
    private ObjectOutputStream betOutStream;
    private boolean successExportBet = false;

    public CassandraGameSessionAndBetHistoryArchiver() {
        super();
    }

    @Override
    public String getName() {
        return "cassandra_gamesession_and_bet";
    }

    @Override
    protected void processDayItems(String outputPath, Date currentStartDate, Date currentEndDate, boolean needRemoveAfterArchive, DateFormat df)
            throws CommonException, IOException {
        String betOutFileName = betHistoryArchiver.getOutFileName(outputPath, currentStartDate, df);
        betOutStream = betHistoryArchiver.createObjectOutputStream(betOutFileName);
        betHistoryNeedRemove = needRemoveAfterArchive ? new ArrayList<Long>() : null;
        try {
            super.processDayItems(outputPath, currentStartDate, currentEndDate, needRemoveAfterArchive, df);
        } finally {
            betHistoryArchiver.close(betOutFileName, betOutStream);
            betOutStream = null;
        }
        if (needRemoveAfterArchive && successExportBet) {
            betHistoryArchiver.remove(betHistoryNeedRemove);
            betHistoryArchiver.updateLastArchiveDate(currentEndDate);
            betHistoryNeedRemove = null;
        }
    }

    @Override
    protected void afterWriteRecord(GameSession record, boolean needRemoveAfterArchive) {
        super.afterWriteRecord(record, needRemoveAfterArchive);
        GameSessionHistory history = betHistoryArchiver.getRecord(record);
        successExportBet = false;
        try {
            betOutStream.writeObject(history);
            successExportBet = true;
        } catch (IOException e) {
            betHistoryArchiver.error("Can't write game history for gameSession Id=" + record.getId(), e);
        }
        if (needRemoveAfterArchive && successExportBet) {
            betHistoryArchiver.addNeedRemoveIdentifier(history, betHistoryNeedRemove);
        }
    }

    @Override
    protected String getOutFileName(String outputPath, Date currentStartDate, DateFormat df) {
        return outputPath + "/" + super.getName() + "_" + df.format(currentStartDate) + ".xml";
    }
}
