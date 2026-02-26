package com.betsoft.casino.mp.data.persister;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.cache.IDistributedCache;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * User: flsh
 * Date: 16.07.18.
 */
public class PlayerNicknamePersister extends AbstractCassandraPersister<String, String> {
    private static final Logger LOG = LogManager.getLogger(PlayerNicknamePersister.class);

    private static final String CF_NAME = "PlayerNickname";
    private static final String NICK_NAME_COLUMN = "nickname";
    //bankId+accountId
    private static final String BANK_AID_COLUMN = "bank_aid";

    private static final TableDefinition TABLE = new TableDefinition(CF_NAME,
            Arrays.asList(
                    new ColumnDefinition(NICK_NAME_COLUMN, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(BANK_AID_COLUMN, com.datastax.driver.core.DataType.text(), false, true, false)
            ), NICK_NAME_COLUMN);

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public String getMainColumnFamilyName() {
        return CF_NAME;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public String getNickname(Long bankId, Long accountId) {
        com.datastax.driver.core.querybuilder.Select query = getSelectColumnsQuery(NICK_NAME_COLUMN);
        query.where().and(eq(BANK_AID_COLUMN, getBankAndAid(bankId, accountId))).limit(1);
        com.datastax.driver.core.Row result = execute(query, "getNickname").one();
        return result == null ? null : result.getString(NICK_NAME_COLUMN);
    }

    public boolean isNicknameAvailable(String nickname) {
        com.datastax.driver.core.querybuilder.Select query = getSelectColumnsQuery(BANK_AID_COLUMN);
        query.where().and(eq(NICK_NAME_COLUMN, nickname));
        com.datastax.driver.core.Row result = execute(query, "isNicknameAvailable").one();
        return result == null || result.getString(BANK_AID_COLUMN) == null;
    }

    public boolean isNicknameAvailable(String nickname, Long bankId, Long accountId) {
        com.datastax.driver.core.querybuilder.Select query = getSelectAllColumnsQuery();
        query.where().and(eq(NICK_NAME_COLUMN, nickname));
        com.datastax.driver.core.Row result = execute(query, "isNicknameAvailable").one();
        if(result == null) {
            return true;
        }
        String dbBankAid = result.getString(BANK_AID_COLUMN);
        String bankAndAid = getBankAndAid(bankId, accountId);
        return StringUtils.isTrimmedEmpty(dbBankAid) || dbBankAid.equals(bankAndAid);
    }

    public boolean changeNickname(Long bankId, Long accountId, String oldNickName, String newNickname)
            throws CommonException {
        if (StringUtils.isTrimmedEmpty(newNickname)) {
            throw new CommonException("Nickname is empty");
        }
        com.datastax.driver.core.ResultSet result;
        String bankAndAid = getBankAndAid(bankId, accountId);
        if (StringUtils.isTrimmedEmpty(oldNickName)) { //newUser, create record
            com.datastax.driver.core.querybuilder.Insert insert = getInsertQuery();
            insert.value(NICK_NAME_COLUMN, newNickname).value(BANK_AID_COLUMN, bankAndAid).ifNotExists();
            result = execute(insert, "changeNickname[new]");
        } else {
            //we cannot use batch: Batch with conditions cannot span multiple partitions
            com.datastax.driver.core.querybuilder.Delete delete =
                    com.datastax.driver.core.querybuilder.QueryBuilder.delete().from(getMainColumnFamilyName());
            delete.where(com.datastax.driver.core.querybuilder.QueryBuilder.eq(NICK_NAME_COLUMN, oldNickName)).
                    onlyIf(com.datastax.driver.core.querybuilder.QueryBuilder.eq(BANK_AID_COLUMN, bankAndAid));
            boolean applied = execute(delete, "changeNickname[deleteOld]").wasApplied();
            if(applied) {
                com.datastax.driver.core.querybuilder.Insert insert = getInsertQuery();
                insert.value(NICK_NAME_COLUMN, newNickname).value(BANK_AID_COLUMN, bankAndAid).ifNotExists();
                result = execute(insert, "changeNickname[insertNew]");
            } else {
                return false;
            }
        }
        return result.wasApplied();
    }

    private String getBankAndAid(Long bankId, Long accountId) {
        return bankId + IDistributedCache.ID_DELIMITER + accountId;
    }

}
