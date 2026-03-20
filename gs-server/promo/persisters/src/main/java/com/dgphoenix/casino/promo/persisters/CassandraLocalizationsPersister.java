package com.abs.casino.promo.persisters;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.promo.LocalizationTitles;
import com.google.common.base.Splitter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.ascii;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

/**
 * Created by vladislav on 12/14/16.
 */
public class CassandraLocalizationsPersister extends AbstractCassandraPersister<String, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraLocalizationsPersister.class);

    private static final String IDENTIFIER_DELIMITER = "_";
    private static final String ITEM_DELIMITER = ".";
    private static final Splitter ITEM_SPLITTER = Splitter.on(ITEM_DELIMITER);
    private static final String PROMO_TYPE = "promo";
    private static final String TITLE = "title";

    private static final String TOURNAMENT_RULES = "tournamentrules";
    private static final String PRIZE_ALLOCATION = "prizeallocation";
    private static final String HOW_TO_WIN = "howtowin";

    private static final String LOCALIZATIONS_CF = "LocalizationsCf";
    private static final String LANG = "lan";
    private static final String ITEM = "it";
    private static final String LOCALIZATION = "loc";
    private static final TableDefinition TABLE = new TableDefinition(LOCALIZATIONS_CF, Arrays.asList(
            new ColumnDefinition(KEY, ascii(), false, false, true),
            new ColumnDefinition(LANG, ascii(), false, false, true),
            new ColumnDefinition(ITEM, ascii(), false, false, true),
            new ColumnDefinition(LOCALIZATION, text(), false, false, false)
    ), KEY);

    public void persistPromoLocalizations(long campaignId, Map<String, String> localizedItems) {
        LOG.info("persist localizations for promo campaign with id = {}, localizations = {}",
                campaignId, localizedItems);
        com.datastax.driver.core.querybuilder.Batch batch = batch();
        String key = PROMO_TYPE + IDENTIFIER_DELIMITER + campaignId;
        for (Map.Entry<String, String> itemLangWithLocalization : localizedItems.entrySet()) {
            String itemWithLang = itemLangWithLocalization.getKey();
            Iterator<String> itemAndLang = ITEM_SPLITTER.split(itemWithLang).iterator();
            String item = itemAndLang.next();
            addInsertStatement(batch, key, itemLangWithLocalization, itemAndLang, item);
        }
        execute(batch, "persistPromoLocalizations");
    }

    public void persistNetworkPromoLocalizations(long networkPromoCampaignId, Map<String, String> localizedItems) {
        LOG.info("persist localizations for network promo campaign with id = {}, localizations = {}",
                networkPromoCampaignId, localizedItems);
        com.datastax.driver.core.querybuilder.Batch batch = batch();
        String key = PROMO_TYPE + IDENTIFIER_DELIMITER + networkPromoCampaignId;
        for (Map.Entry<String, String> entry : localizedItems.entrySet()) {
            String itemWithLang = entry.getKey();
            Iterator<String> itemAndLang = ITEM_SPLITTER.split(itemWithLang).iterator();
            String item = itemAndLang.next();
            switch (item.toLowerCase()) {
                case TOURNAMENT_RULES:
                    item = TOURNAMENT_RULES;
                    break;
                case PRIZE_ALLOCATION:
                    item = PRIZE_ALLOCATION;
                    break;
                case HOW_TO_WIN:
                    item = HOW_TO_WIN;
                    break;
                default:
                    throw new NoSuchElementException(String.format("Item '%s' not found", item));
            }
            addInsertStatement(batch, key, entry, itemAndLang, item);
        }
        execute(batch, "persistNetworkPromoLocalizations");
    }

    private void addInsertStatement(com.datastax.driver.core.querybuilder.Batch batch, String key, Map.Entry<String, String> entry,
                                    Iterator<String> itemAndLang, String item) {
        String lang = itemAndLang.next().toLowerCase();
        String localization = entry.getValue();
        batch.add(getInsertQuery(TABLE, getTtl())
                .value(KEY, key)
                .value(LANG, lang)
                .value(ITEM, item)
                .value(LOCALIZATION, localization));
    }

    public LocalizationTitles getNetworkPromoLocalizations(long networkPromoCampaignId, String lang) {
        String key = PROMO_TYPE + IDENTIFIER_DELIMITER + networkPromoCampaignId;
        List<com.abs.casino.cassandra.persist.engine.Row> rows = execute(getSelectColumnsQuery(ITEM, LOCALIZATION)
                .where(eq(KEY, key))
                .and(eq(LANG, lang.toLowerCase())), "getNetworkPromoLocalizations").all();
        String tournamentRules = "";
        String prizeAllocation = "";
        String howToWin = "";
        for (com.abs.casino.cassandra.persist.engine.Row row : rows) {
            String item = row.getString(ITEM);
            String localization = row.getString(LOCALIZATION);
            switch (item) {
                case TOURNAMENT_RULES:
                    tournamentRules = localization;
                    break;
                case PRIZE_ALLOCATION:
                    prizeAllocation = localization;
                    break;
                case HOW_TO_WIN:
                    howToWin = localization;
                    break;
                default:
                    throw new NoSuchElementException(String.format("Item '%s' not found", item));
            }
        }
        return new LocalizationTitles(tournamentRules, prizeAllocation, howToWin);
    }

    public String getLocalizedPromoTitle(long campaignId, String lang) {
        String key = PROMO_TYPE + IDENTIFIER_DELIMITER + campaignId;
        String item = TITLE;
        com.abs.casino.cassandra.persist.engine.Row result = execute(getSelectColumnsQuery(LOCALIZATION)
                .where(eq(KEY, key))
                .and(eq(LANG, lang.toLowerCase()))
                .and(eq(ITEM, item)), "getLocalizedPromoTitle").one();

        String localization = null;
        if (result != null) {
            localization = result.getString(LOCALIZATION);
        }
        return localization;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
