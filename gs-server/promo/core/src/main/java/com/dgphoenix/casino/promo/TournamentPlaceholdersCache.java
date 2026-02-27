package com.abs.casino.promo;

import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

/** Store welcome and rules messages with replaced placeholders */
@Component
public class TournamentPlaceholdersCache {
    private static final Logger LOG = LogManager.getLogger(TournamentPlaceholdersCache.class);

    /** welcome message prefix for cache key */
    public static final String WELCOME_MSG_CACHE_KEY = "welcomeMsg";

    /** rules message prefix for cache key */
    public static final String RULES_CACHE_KEY = "rules";

    /** Separates parts of cache key */
    private static final String KEY_DELIMITER = "_";

    private final Cache<String, String> placeholderMessageCache;

    public TournamentPlaceholdersCache() {
        placeholderMessageCache = CacheBuilder.newBuilder()
                .initialCapacity(500)
                .maximumSize(10000)
                .recordStats()
                .concurrencyLevel(8)
                .build();
        StatisticsManager.getInstance()
                .registerStatisticsGetter("TournamentBuilder",
                        () -> "size=" + placeholderMessageCache.size() + ", stats=" + placeholderMessageCache.stats());
    }

    public String getIfPresent(String key) {
        return placeholderMessageCache.getIfPresent(key);
    }

    public void put(String key, String value) {
        placeholderMessageCache.put(key, value);
    }

    /** Invalidate all cache values for keys with given tournamentId */
    public void invalidateByTournamentId(long tournamentId) {
        String partialWelcomeMessageKey = String.join(KEY_DELIMITER, WELCOME_MSG_CACHE_KEY, String.valueOf(tournamentId));
        String partialRulesKey = String.join(KEY_DELIMITER, RULES_CACHE_KEY, String.valueOf(tournamentId));
        Set<String> deleteKeys = placeholderMessageCache.asMap().keySet()
                .stream()
                .filter(key -> key.startsWith(partialRulesKey) || key.startsWith(partialWelcomeMessageKey))
                .collect(Collectors.toSet());
        LOG.debug("Invalidate by tournamentId={}, found {} keys to invalidate", tournamentId, deleteKeys);
        placeholderMessageCache.invalidateAll(deleteKeys);
    }

    /**
     * @param msgKey key prefix with message name
     * @return string key for searching message with placeholders in cache
     */
    public String composeKey(String msgKey, long id, String lang, String playerCurrency) {
        checkState(ObjectUtils.allNotNull(msgKey,lang, playerCurrency),
                "msgKey,lang,playerCurrency can't be null");
        return String.join(KEY_DELIMITER, msgKey, String.valueOf(id), lang, playerCurrency);
    }
}
