package com.abs.casino.promo;

import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.common.exception.AlreadyExistsException;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.promo.persisters.CassandraPlayerAliasPersister;
import com.abs.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerAliasManager {
    private static final Logger LOG = LogManager.getLogger(PlayerAliasManager.class);
    private static final String DELIMITER = "#";
    private static final int MAX_ALIAS_LENGTH = 50;
    private final CassandraPlayerAliasPersister playerAliasPersister;
    private final int clusterId;
    private final long tournamentPostfixIncrement;

    private List<String> bannedNames;

    public PlayerAliasManager(CassandraPersistenceManager persistenceManager,
                              GameServerConfiguration gameServerConfiguration) {
        this.playerAliasPersister = persistenceManager.getPersister(CassandraPlayerAliasPersister.class);
        this.clusterId = gameServerConfiguration.getClusterId();
        this.tournamentPostfixIncrement = gameServerConfiguration.getTournamentClustersCount();
    }

    @PostConstruct
    public void init() throws IOException {
        try (InputStream resource = new ClassPathResource("/banned-names.txt").getInputStream()) {
            bannedNames = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.toList());
            LOG.info("Banned names loaded: {}", bannedNames);
        } catch (IOException e) {
            LOG.warn("Banned names weren't loaded");
            bannedNames = new ArrayList<>();
        }
    }

    public String saveForMultiCluster(long networkTournamentId, String alias) {
        Long postfix = playerAliasPersister.getAliasPostfix(networkTournamentId, alias);
        if (postfix != null) {
            postfix += tournamentPostfixIncrement;
        } else {
            postfix = (long) clusterId;
        }
        playerAliasPersister.persistForMultiCluster(networkTournamentId, alias, postfix);
        String newName = alias + (postfix == 0 ? "" : (DELIMITER + postfix));
        LOG.debug("Name={} saved for networkTournamentId={}", newName, networkTournamentId);
        return newName;
    }

    public void saveForSingleCluster(long networkTournamentId, String alias) throws AlreadyExistsException {
        if (playerAliasPersister.isExistsForSingleCluster(networkTournamentId, alias)) {
            LOG.warn("Name={} already exists for networkTournamentId={}", alias, networkTournamentId);
            throw new AlreadyExistsException("Such alias already exists");
        }
        playerAliasPersister.persistForSingleCluster(networkTournamentId, alias);
    }

    public void checkAliasLength(String alias) throws CommonException {
        if (alias.length() > MAX_ALIAS_LENGTH) {
            throw new CommonException("Alias length exceeds max value");
        }
    }

    public boolean checkObscene(String alias) {
        return bannedNames.contains(alias.toLowerCase());
    }
}
