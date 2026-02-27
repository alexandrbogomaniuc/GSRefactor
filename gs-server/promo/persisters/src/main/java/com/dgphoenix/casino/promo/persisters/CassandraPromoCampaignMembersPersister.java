package com.abs.casino.promo.persisters;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.promo.PromoCampaignMember;
import com.dgphoenix.casino.common.promo.PromoCampaignMemberInfos;
import com.dgphoenix.casino.common.util.StreamUtils;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: flsh
 * Date: 17.11.16.
 */
public class CassandraPromoCampaignMembersPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraPromoCampaignMembersPersister.class);
    private static final String PROMO_CAMPAIGN_MEMBER_CF = "PromoCampaignMemberCF";
    private static final String PROMO_MEMBER_ALIASES_CF = "PromoMemberAliasesCF";
    private static final String ACCOUNT_ID = "accId";
    private static final String CAMPAIGN_ID = "campId";
    private static final String BANK_ID = "bankId";
    private static final String ALIAS_NAME = "campAlias";
    private static final String CAMPAIGN_MEMBER_DATA = "memData";
    private static final TableDefinition CAMPAIGN_MEMBER_TABLE = new TableDefinition(PROMO_CAMPAIGN_MEMBER_CF,
            Arrays.asList(
                    new ColumnDefinition(CAMPAIGN_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(CAMPAIGN_MEMBER_DATA, com.datastax.driver.core.DataType.blob(), false, false, false),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text(), false, false, false)
            ), CAMPAIGN_ID)
            .compaction(CompactionStrategy.LEVELED);
    private static final TableDefinition PROMO_MEMBER_ALIASES_TABLE = new TableDefinition(PROMO_MEMBER_ALIASES_CF,
            Arrays.asList(
                    new ColumnDefinition(CAMPAIGN_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(BANK_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(ALIAS_NAME, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID, com.datastax.driver.core.DataType.bigint(), false, false, false)
            ), CAMPAIGN_ID)
            .compaction(CompactionStrategy.LEVELED);

    public void prepareToPersist(Map<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap, PromoCampaignMemberInfos members,
                                 List<ByteBuffer> byteBuffersCollector) {
        List<com.datastax.driver.core.Statement> statements = getOrCreateStatements(statementsMap);
        members.getPromoMembers().forEach((campaignId, member) -> {
            String json = getMainTableDefinition().serializeWithClassToJson(member);
            ByteBuffer byteBuffer = getMainTableDefinition().serializeWithClassToBytes(member);
            byteBuffersCollector.add(byteBuffer);
            com.datastax.driver.core.Statement updateQuery = getUpdateQuery(campaignId, member.getAccountId(), byteBuffer, json);
            statements.add(updateQuery);
        });
    }

    public void update(PromoCampaignMember member) {
        ByteBuffer memberAsBytes = getMainTableDefinition().serializeWithClassToBytes(member);
        String json = getMainTableDefinition().serializeWithClassToJson(member);
        try {
            execute(getUpdateQuery(member.getCampaignId(), member.getAccountId(), memberAsBytes, json), "update");
        } finally {
            releaseBuffer(memberAsBytes);
        }
    }

    private com.datastax.driver.core.Statement getUpdateQuery(long campaignId, long accountId, ByteBuffer memberAsBytes, String json) {
        return getUpdateQuery()
                .where(eq(CAMPAIGN_ID, campaignId))
                .and(eq(ACCOUNT_ID, accountId))
                .with(com.datastax.driver.core.querybuilder.QueryBuilder.set(CAMPAIGN_MEMBER_DATA, memberAsBytes))
                .and(com.datastax.driver.core.querybuilder.QueryBuilder.set(JSON_COLUMN_NAME, json));
    }

    public void create(PromoCampaignMember member) throws CommonException {
        persistAlias(member);
        ByteBuffer memberAsBytes = getMainTableDefinition().serializeWithClassToBytes(member);
        String json = getMainTableDefinition().serializeWithClassToJson(member);
        try {
            execute(getInsertQuery(CAMPAIGN_MEMBER_TABLE, getTtl())
                    .value(CAMPAIGN_ID, member.getCampaignId())
                    .value(ACCOUNT_ID, member.getAccountId())
                    .value(CAMPAIGN_MEMBER_DATA, memberAsBytes)
                    .value(JSON_COLUMN_NAME, json), "insert");
        } finally {
            releaseBuffer(memberAsBytes);
        }
    }

    private void persistAlias(PromoCampaignMember member) throws CommonException {
        com.datastax.driver.core.ResultSet resultSet = execute(getInsertQuery(PROMO_MEMBER_ALIASES_TABLE, getTtl())
                .value(CAMPAIGN_ID, member.getCampaignId())
                .value(BANK_ID, member.getBankId())
                .value(ALIAS_NAME, member.getDisplayName())
                .value(ACCOUNT_ID, member.getAccountId())
                .ifNotExists(), "persistAlias");
        if (!resultSet.wasApplied()) {
            Long existAccountId = getPromoAccountId(member.getCampaignId(), member.getBankId(), member.getDisplayName());
            LOG.error("persistAlias: alias already exist, campaignId={}, accountId={}, alias={}, existAccountId={}", member.getCampaignId(),
                    member.getAccountId(), member.getDisplayName(), existAccountId);
            if (existAccountId != null) {
                if (member.getAccountId() != existAccountId) {
                    throw new CommonException("Alias already exist");
                } else {
                    LOG.warn("Alias already persisted, used create() instead of update(). Need fix");
                }
            } else {
                throw new CommonException("Cannot add alias by unknown reason");
            }
        }
    }

    public Map<Long, String> getAllPromoAliases(long promoId) {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(PROMO_MEMBER_ALIASES_TABLE, ALIAS_NAME, ACCOUNT_ID)
                .where(eq(CAMPAIGN_ID, promoId)), "getAllPromoAliases");
        return convert(resultSet);
    }

    public Long getPromoAccountId(long promoId, long bankId, String alias) {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(PROMO_MEMBER_ALIASES_TABLE, ACCOUNT_ID)
                .where(eq(CAMPAIGN_ID, promoId))
                .and(eq(BANK_ID, bankId))
                .and(eq(ALIAS_NAME, alias)), "getPromoAccountId");
        com.datastax.driver.core.Row row = resultSet.one();
        return row == null ? null : row.getLong(ACCOUNT_ID);
    }

    public Map<Long, String> getAllBankAliases(long promoId, long bankId) {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectColumnsQuery(PROMO_MEMBER_ALIASES_TABLE, ALIAS_NAME, ACCOUNT_ID)
                .where(eq(CAMPAIGN_ID, promoId))
                .and(eq(BANK_ID, bankId)), "getAllPromoAliases");
        return convert(resultSet);
    }

    private Map<Long, String> convert(com.datastax.driver.core.ResultSet resultSet) {
        Map<Long, String> aliasesMap = new HashMap<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            String alias = row.getString(ALIAS_NAME);
            long accountId = row.getLong(ACCOUNT_ID);
            aliasesMap.put(accountId, alias);
        }
        return aliasesMap;
    }

    public PromoCampaignMember getPromoMember(long accountId, long campaignId) {
        com.datastax.driver.core.Row campaignMemberResult = execute(getSelectColumnsQuery(CAMPAIGN_MEMBER_TABLE, CAMPAIGN_MEMBER_DATA, JSON_COLUMN_NAME)
                .where(eq(CAMPAIGN_ID, campaignId))
                .and(eq(ACCOUNT_ID, accountId)), "getOrCreatePromoMember:: selectCampaignMember").one();
        PromoCampaignMember campaignMember = null;
        if (campaignMemberResult != null) {
            String json = campaignMemberResult.getString(JSON_COLUMN_NAME);
            campaignMember = getMainTableDefinition().deserializeWithClassFromJson(json);

            if (campaignMember == null) {
                ByteBuffer campaignMemberAsBytes = campaignMemberResult.getBytes(CAMPAIGN_MEMBER_DATA);
                campaignMember = getMainTableDefinition().deserializeWithClassFrom(campaignMemberAsBytes);
            }
        }
        return campaignMember;
    }

    public Set<PromoCampaignMember> getPromoCampaignMembers(long promoCampaignId) {
        return Sets.newHashSet(getPromoCampaignMembersSafely(promoCampaignId));
    }

    public Iterable<PromoCampaignMember> getPromoCampaignMembersSafely(long promoCampaignId) {
        com.datastax.driver.core.querybuilder.Select selectCampaignMembers =
                getSelectColumnsQuery(CAMPAIGN_MEMBER_TABLE, CAMPAIGN_MEMBER_DATA, JSON_COLUMN_NAME);
        selectCampaignMembers.where(eq(CAMPAIGN_ID, promoCampaignId));
        selectCampaignMembers.setFetchSize(1000);
        return StreamUtils.asStream(execute(selectCampaignMembers, "getPromoCampaignMembersSafely"))
                .map(memberResult -> {
                    PromoCampaignMember member = null;
                    String json = memberResult.getString(JSON_COLUMN_NAME);
                    member = getMainTableDefinition().deserializeWithClassFromJson(json);

                    if (member == null) {
                        ByteBuffer campaignMemberAsBytes = memberResult.getBytes(CAMPAIGN_MEMBER_DATA);
                        if (campaignMemberAsBytes != null) {
                            member = getMainTableDefinition().deserializeWithClassFrom(campaignMemberAsBytes);
                        }
                    }
                    return member;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return CAMPAIGN_MEMBER_TABLE;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Arrays.asList(CAMPAIGN_MEMBER_TABLE, PROMO_MEMBER_ALIASES_TABLE);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
