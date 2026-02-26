package com.dgphoenix.casino.promo.persisters;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Caching;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.promo.IPromoCampaign;
import com.dgphoenix.casino.common.promo.NetworkPromoCampaign;
import com.dgphoenix.casino.common.promo.NetworkPromoEvent;
import com.dgphoenix.casino.common.promo.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: flsh
 * Date: 17.11.16.
 */
public class CassandraPromoCampaignPersister extends AbstractCassandraPersister<Long, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraPromoCampaignPersister.class);

    private static final String PROMO_CAMPAIGN_CF = "PromoCampaignCF";
    private static final String CAMPAIGN_ID = "campId";
    private static final String CAMPAIGN_DATA = "campData";
    private static final TableDefinition CAMPAIGN_TABLE = new TableDefinition(PROMO_CAMPAIGN_CF,
            Arrays.asList(
                    new ColumnDefinition(CAMPAIGN_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(CAMPAIGN_DATA, com.datastax.driver.core.DataType.blob(), false, false, false),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text(), false, false, false)
            ), CAMPAIGN_ID)
            .compaction(CompactionStrategy.LEVELED);

    private static final String ARCHIVE_PROMO_CAMPAIGN_CF = "PromoCampaignArchCF";
    private static final TableDefinition ARCHIVE_CAMPAIGN_TABLE = new TableDefinition(ARCHIVE_PROMO_CAMPAIGN_CF,
            Arrays.asList(
                    new ColumnDefinition(CAMPAIGN_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(CAMPAIGN_DATA, com.datastax.driver.core.DataType.blob(), false, false, false),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text(), false, false, false)
            ), CAMPAIGN_ID)
            .compaction(CompactionStrategy.SIZE_TIRED)
            .caching(Caching.NONE);

    private static final String PROMO_CAMPAIGN_BY_BANK_AND_GAME_CF = "PromoCampaignByBankAndGameCF";
    private static final String BANK_ID = "bankId";
    private static final long ID_FOR_ALL = -1;
    private static final String GAME_ID = "gameId";
    private static final String CAMPAIGN_STATUS = "campStatus";
    private static final TableDefinition CAMPAIGN_BY_BANK_AND_GAME_TABLE = new TableDefinition(PROMO_CAMPAIGN_BY_BANK_AND_GAME_CF,
            Arrays.asList(
                    new ColumnDefinition(BANK_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(CAMPAIGN_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(CAMPAIGN_STATUS, com.datastax.driver.core.DataType.ascii(), false, true, false)
            ), BANK_ID)
            .compaction(CompactionStrategy.LEVELED);

    public void persist(IPromoCampaign campaign) {
        LOG.info("persist promoCampaign: {}", campaign);
        if (campaign instanceof NetworkPromoEvent) {
            throw new RuntimeException("NetworkPromoEvent cannot be saved as separated record");
        }
        ByteBuffer campaignAsBytes = getMainTableDefinition().serializeWithClassToBytes(campaign);
        String json = getMainTableDefinition().serializeWithClassToJson(campaign);
        try {
            com.datastax.driver.core.querybuilder.Batch batch = batch();
            TableDefinition storeTable = getStoreTableForStatus(campaign.getStatus());

            IPromoCampaign storedCampaign = get(campaign.getId());
            if (storedCampaign != null) {
                Set<Long> banksToRemove = storedCampaign.getBankIds();
                banksToRemove.removeAll(campaign.getBankIds());
                for (Long bankId : banksToRemove) {
                    for (Long gameId : storedCampaign.getGameIds()) {
                        addDeletion(batch, bankId, gameId, campaign.getId());
                    }
                    addDeletion(batch, bankId, ID_FOR_ALL, campaign.getId());
                }

                Set<Long> gamesToRemove = storedCampaign.getGameIds();
                gamesToRemove.removeAll(campaign.getGameIds());
                for (Long gameId : gamesToRemove) {
                    for (Long bankId : storedCampaign.getBankIds()) {
                        addDeletion(batch, bankId, gameId, campaign.getId());
                    }
                }
            }
            batch.add(getInsertQuery(storeTable, getTtl())
                    .value(CAMPAIGN_ID, campaign.getId())
                    .value(CAMPAIGN_DATA, campaignAsBytes)
                    .value(JSON_COLUMN_NAME, json));

            for (Long bankId : campaign.getBankIds()) {
                Set<Long> gameIds = campaign.getGameIds();
                if (gameIds == null) {
                    gameIds = lookUpGamesIdsForBank(bankId);
                }

                for (Long gameId : gameIds) {
                    batch.add(getInsertQuery(CAMPAIGN_BY_BANK_AND_GAME_TABLE, getTtl())
                            .value(BANK_ID, bankId)
                            .value(GAME_ID, gameId)
                            .value(CAMPAIGN_ID, campaign.getId())
                            .value(CAMPAIGN_STATUS, campaign.getStatus().name()));
                }

                batch.add(getInsertQuery(CAMPAIGN_BY_BANK_AND_GAME_TABLE, getTtl())
                        .value(BANK_ID, bankId)
                        .value(GAME_ID, ID_FOR_ALL)
                        .value(CAMPAIGN_ID, campaign.getId())
                        .value(CAMPAIGN_STATUS, campaign.getStatus().name()));
            }

            batch.add(getInsertQuery(CAMPAIGN_BY_BANK_AND_GAME_TABLE, getTtl())
                    .value(BANK_ID, ID_FOR_ALL)
                    .value(GAME_ID, ID_FOR_ALL)
                    .value(CAMPAIGN_ID, campaign.getId())
                    .value(CAMPAIGN_STATUS, campaign.getStatus().name()));

            if (storeTable == ARCHIVE_CAMPAIGN_TABLE) {
                batch.add(addItemDeletion(CAMPAIGN_TABLE.getTableName(), eq(CAMPAIGN_ID, campaign.getId())));
            }

            execute(batch, "persist");
        } finally {
            if (campaignAsBytes != null) {
                releaseBuffer(campaignAsBytes);
            }
        }
    }

    private Set<Long> lookUpGamesIdsForBank(long bankId) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        checkNotNull(bankInfo, "Bank info is null, bankId = %s", bankId);
        Currency defaultCurrency = bankInfo.getDefaultCurrency();
        return BaseGameCache.getInstance().getAllGamesSet(bankId, defaultCurrency);
    }

    private void addDeletion(com.datastax.driver.core.querybuilder.Batch batch, long bankId, long gameId, long campaignId) {
        batch.add(addItemDeletion(CAMPAIGN_BY_BANK_AND_GAME_TABLE.getTableName(),
                eq(BANK_ID, bankId), eq(GAME_ID, gameId), eq(CAMPAIGN_ID, campaignId)));
    }

    public NetworkPromoEvent getNetworkPromoEvent(long eventId) {
        Iterator<com.datastax.driver.core.Row> rows = getAll();
        while (rows.hasNext()) {
            com.datastax.driver.core.Row campaignData = rows.next();
            if (campaignData != null) {
                ByteBuffer campaignDataAsBytes = campaignData.getBytes(CAMPAIGN_DATA);
                String json = campaignData.getString(JSON_COLUMN_NAME);
                IPromoCampaign promoCampaign = getMainTableDefinition().
                        deserializeWithClassFromJson(json);

                if (promoCampaign == null) {
                    promoCampaign = getMainTableDefinition().
                            deserializeWithClassFrom(campaignDataAsBytes);
                }
                if (promoCampaign.isNetworkPromoCampaign()) {
                    NetworkPromoCampaign networkPromoCampaign = (NetworkPromoCampaign) promoCampaign;
                    Set<NetworkPromoEvent> events = networkPromoCampaign.getEvents();
                    for (NetworkPromoEvent event : events) {
                        if (event.getId() == eventId) {
                            return event;
                        }
                    }
                }
            }
        }
        return null;
    }

    public IPromoCampaign get(long campaignId) {
        IPromoCampaign promoCampaign = getByIdFromTable(campaignId, CAMPAIGN_TABLE);
        if (promoCampaign == null) {
            promoCampaign = getByIdFromTable(campaignId, ARCHIVE_CAMPAIGN_TABLE);
        }
        return promoCampaign;
    }

    public Set<IPromoCampaign> getStartedForBankAndGame(long bankId, long gameId) {
        return getPromos(bankId, gameId, Status.STARTED);
    }

    public Set<IPromoCampaign> getStarted(long bankId) {
        return getPromos(bankId, null, Status.STARTED);
    }

    public Set<IPromoCampaign> getByStatus(Status status) {
        return getPromos(null, null, status);
    }

    public Set<IPromoCampaign> getPromos(Long bankId, Long gameId, Status status) {
        if (bankId == null) {
            bankId = ID_FOR_ALL;
        }
        if (gameId == null) {
            gameId = ID_FOR_ALL;
        }

        com.datastax.driver.core.querybuilder.Select selectCampaignsByClause = status == null
                ? getSelectColumnsQuery(CAMPAIGN_BY_BANK_AND_GAME_TABLE, CAMPAIGN_ID, CAMPAIGN_STATUS)
                : getSelectColumnsQuery(CAMPAIGN_BY_BANK_AND_GAME_TABLE, CAMPAIGN_ID);
        selectCampaignsByClause
                .where(eq(BANK_ID, bankId))
                .and(eq(GAME_ID, gameId));
        if (status != null) {
            selectCampaignsByClause.where(eq(CAMPAIGN_STATUS, status.name()));
        }
        com.datastax.driver.core.ResultSet campaignsResult = execute(selectCampaignsByClause, "selectCampaignsByClause");

        Set<IPromoCampaign> promoCampaigns = new HashSet<>();
        for (com.datastax.driver.core.Row campaignResult : campaignsResult) {
            long campaignId = campaignResult.getLong(CAMPAIGN_ID);
            Status campaignStatus = status == null
                    ? Status.valueOf(campaignResult.getString(CAMPAIGN_STATUS))
                    : status;

            TableDefinition storeTable = getStoreTableForStatus(campaignStatus);
            IPromoCampaign campaign = getByIdFromTable(campaignId, storeTable);
            if (campaign != null) {
                promoCampaigns.add(campaign);
            }
        }

        return promoCampaigns;
    }

    public Set<IPromoCampaign> getPromos(long bankId, Set<Long> gameIds) {
        Set<IPromoCampaign> promoCampaigns = new HashSet<>();
        for (long gameId : gameIds) {
            Set<IPromoCampaign> gameCampaigns = getPromos(bankId, gameId, null);
            promoCampaigns.addAll(gameCampaigns);
        }
        return promoCampaigns;
    }

    private IPromoCampaign getByIdFromTable(long campaignId, TableDefinition table) {
        com.datastax.driver.core.Row campaignData = execute(getSelectColumnsQuery(table, CAMPAIGN_DATA, JSON_COLUMN_NAME)
                .where(eq(CAMPAIGN_ID, campaignId)), "getByIdFromTable").one();

        IPromoCampaign promoCampaign = null;
        if (campaignData != null) {
            String json = campaignData.getString(JSON_COLUMN_NAME);
            promoCampaign = getMainTableDefinition().deserializeWithClassFromJson(json);

            if (promoCampaign == null) {
                ByteBuffer campaignDataAsBytes = campaignData.getBytes(CAMPAIGN_DATA);
                promoCampaign = getMainTableDefinition().deserializeWithClassFrom(campaignDataAsBytes);
            }
        }

        return promoCampaign;
    }

    public Set<Long> getPromoIdsByBank(long bankId) {
        com.datastax.driver.core.ResultSet campaignsIdsByBank = execute(getSelectColumnsQuery(CAMPAIGN_BY_BANK_AND_GAME_TABLE, CAMPAIGN_ID)
                .where(eq(BANK_ID, bankId))
                .and(eq(GAME_ID, ID_FOR_ALL)), "getPromoIdsByBank");

        Set<Long> bankCampaignsIds = new HashSet<>();
        for (com.datastax.driver.core.Row campaignIdResult : campaignsIdsByBank) {
            long campaignId = campaignIdResult.getLong(CAMPAIGN_ID);
            bankCampaignsIds.add(campaignId);
        }

        return bankCampaignsIds;
    }

    private TableDefinition getStoreTableForStatus(Status status) {
        return status == Status.READY || status == Status.STARTED || status == Status.QUALIFICATION
                ? CAMPAIGN_TABLE
                : ARCHIVE_CAMPAIGN_TABLE;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return CAMPAIGN_TABLE;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Arrays.asList(CAMPAIGN_TABLE, ARCHIVE_CAMPAIGN_TABLE, CAMPAIGN_BY_BANK_AND_GAME_TABLE);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
