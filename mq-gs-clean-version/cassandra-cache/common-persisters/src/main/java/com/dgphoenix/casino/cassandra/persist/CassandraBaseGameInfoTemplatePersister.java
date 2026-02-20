package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * User: flsh
 * Date: 4/11/12
 */
public class CassandraBaseGameInfoTemplatePersister
        extends AbstractLongDistributedConfigEntryPersister<BaseGameInfoTemplate> {
    public static final String BASE_GAME_TEMPLATE_INFO_CF = "GameTInfoCF";
    private static final Logger LOG = LogManager.getLogger(CassandraBaseGameInfoTemplatePersister.class);

    private CassandraBaseGameInfoTemplatePersister() {
        super();
    }

    @Override
    public BaseGameInfoTemplateCache getCache() {
        return BaseGameInfoTemplateCache.getInstance();
    }

    @Override
    public int loadAll() {
        final Map<Long, BaseGameInfoTemplate> infos = loadAllAsMap(BaseGameInfoTemplate.class);
        if (infos == null) {
            LOG.error("loadAllForLongKeys is null");
            return 0;
        }
        int count = 0;
        for (BaseGameInfoTemplate info : infos.values()) {
            put(info);
            boolean isFrb = info.isFrbGame();
            if (isFrb && info.getGameId() != 779) {
                BaseGameInfoTemplateCache.getInstance().addFrbGame(info.getId());
            }
            count++;
        }
        LOG.info("loadAll: count=" + count + ", BaseGameInfoTemplateCache.getInstance().size()=" +
                BaseGameInfoTemplateCache.getInstance().size());

        return BaseGameInfoTemplateCache.getInstance().size();
    }

    @Override
    public String getMainColumnFamilyName() {
        return BASE_GAME_TEMPLATE_INFO_CF;
    }

    @Override
    public BaseGameInfoTemplate get(String id) {
        return get(id, BaseGameInfoTemplate.class);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
