package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.util.IGeoIp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CassandraBlockedCountriesPersister extends AbstractCassandraPersister<String, String> {
    public static final String COLUMN_FAMILY_NAME = "BlockedCountriesCF";

    private static final Logger LOG = LogManager.getLogger(CassandraBlockedCountriesPersister.class);

    private static final TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY_NAME,
            Collections.singletonList(
                    new ColumnDefinition(KEY, com.datastax.driver.core.DataType.text(), false, false, true)
            ),
            KEY);

    private Set<String> countries;

    private CassandraBlockedCountriesPersister() {
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public void persist(String countryISOCode, boolean isBlocked) {
        LOG.debug("persist " + countryISOCode + " blocked=" + isBlocked);
        if (isBlocked) {
            com.datastax.driver.core.Statement query = getInsertQuery().value(KEY, countryISOCode);
            execute(query, "persist");
        } else {
            deleteWithCheck(KEY);
        }
    }

    public List<String> get() {
        com.datastax.driver.core.ResultSet resultSet = execute(getSelectAllColumnsQuery(), "get");
        List<String> blockedCountries = resultSet.all().stream()
                .map(row -> row.getString(KEY))
                .collect(Collectors.toList());
        LOG.debug("get blocked country " + blockedCountries);
        return blockedCountries;
    }

    public synchronized void reset() {
        countries = null;
    }

    private void initCache() {
        Set<String> tmp = new HashSet<>();
        List<String> countriesFromCassandra = get();
        for (String country : countriesFromCassandra) {
            tmp.add(country);
        }
        changeLocalCash(tmp);
        LOG.debug("initCache " + countries);
    }

    private synchronized void changeLocalCash(Set<String> tmp) {
        countries = tmp;
    }

    public boolean check(String ip, IGeoIp geoIp) {
        try {
            if (countries == null) {
                initCache();
            }
            String userCountry = geoIp.getCountryCode(ip);
            boolean check = !countries.contains(userCountry);
            LOG.debug("check ip " + ip + " code " + userCountry + " check " + check);
            return check;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return true;
    }
}
