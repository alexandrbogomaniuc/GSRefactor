package com.dgphoenix.casino.gs.managers.payment.bonus;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BonusGameMode;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.abs.casino.services.bonus.ForbiddenGamesForBonusProvider;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collector;

import static java.util.stream.Collectors.*;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 28.06.2021
 */
public class CreationBonusHelper {

    private final BankInfoCache bankInfoCache;
    private final BaseGameCache baseGameCache;
    private final BaseGameInfoTemplateCache baseGameInfoTemplateCache;
    private final Collector<Long, ?, Set<Long>> collector;
    private final BiPredicate<Long, Long> isAvailableForBonus;

    public CreationBonusHelper(BankInfoCache bankInfoCache, BaseGameCache baseGameCache, BaseGameInfoTemplateCache baseGameInfoTemplateCache,
                               ForbiddenGamesForBonusProvider forbiddenGamesForBonusProvider) {
        this.bankInfoCache = bankInfoCache;
        this.baseGameCache = baseGameCache;
        this.baseGameInfoTemplateCache = baseGameInfoTemplateCache;
        collector = toCollection(TreeSet::new);
        isAvailableForBonus = (bankId, gameId) -> !forbiddenGamesForBonusProvider.getGames(bankId).contains(gameId);
    }

    public List<Long> filterBonusGames(List<Long> games, long bankId, boolean isActionGames, BonusGameMode gameMode) {
        Set<Long> gamesSet = new TreeSet<>(games);
        Currency defaultCurrency = bankInfoCache.getBankInfo(bankId).getDefaultCurrency();
        Set<Long> availableForBonusGames = baseGameCache.getAllGamesSet(bankId, defaultCurrency).stream()
                .filter(gameId -> isAvailableForBonus.test(bankId, gameId))
                .collect(toSet());
        Set<Long> resultGamesSet = filterGames(gamesSet, bankId, isActionGames, gameMode, availableForBonusGames);
        return new ArrayList<>(resultGamesSet);
    }

    public List<Long> filterFRBonusGames(List<Long> games, long bankId, boolean isActionGames, BonusGameMode gameMode) {
        Set<Long> gamesSet = new TreeSet<>(games);
        BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
        Set<Long> resultGameSet = filterGames(gamesSet, bankId, isActionGames, gameMode, bankInfoCache.getFrbGames(bankInfo));
        return new ArrayList<>(resultGameSet);
    }

    public List<Long> getValidGameIds(long bankId, List<Long> gameIds) {
        BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
        return baseGameCache.getAllGamesSet(bankId, bankInfo.getDefaultCurrency()).stream()
                .filter(gameIds::contains)
                .collect(toList());
    }

    public List<Long> getBonusGames(long bankId) {
        BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
        Set<Long> unwantedGames = bankInfo.isUseSingleGameIdForAllDevices() ? getAllMobileGameIds(bankInfo) : Collections.emptySet();
        return baseGameCache.getAllGamesSet(bankId, bankInfo.getDefaultCurrency()).stream()
                .filter(gameId -> isAvailableForBonus.test(bankId, gameId))
                .filter(gameId -> !unwantedGames.contains(gameId))
                .collect(toList());
    }

    private Set<Long> getAllMobileGameIds(BankInfo bankInfo) {
        return baseGameCache.getAllGamesSet(bankInfo.getId(), bankInfo.getDefaultCurrency()).stream()
                .map(gameId -> baseGameCache.getGameInfoShared(bankInfo.getId(), gameId, bankInfo.getDefaultCurrency()))
                .filter(Objects::nonNull)
                .filter(IBaseGameInfo::isMobile)
                .map(IBaseGameInfo::getId)
                .collect(toSet());
    }

    private Set<Long> filterGames(Set<Long> chosenGames, long bankId, boolean isActionGames, BonusGameMode gameMode, Set<Long> availableGames) {
        Currency defaultCurrency = bankInfoCache.getBankInfo(bankId).getDefaultCurrency();
        Set<Long> allAvailableGames = availableGames.stream()
                .map(gameId -> baseGameCache.getGameInfoById(bankId, gameId, defaultCurrency))
                .filter(Objects::nonNull)
                .filter(IBaseGameInfo::isEnabled)
                .map(IBaseGameInfo::getId)
                .collect(collector);
        Set<Long> allActionGames = baseGameInfoTemplateCache.getMultiplayerGames();
        Set<Long> allBankNonActionGames = allAvailableGames.stream()
                .filter(gameId -> !allActionGames.contains(gameId))
                .collect(collector);
        Set<Long> allBankActionGames = allAvailableGames.stream()
                .filter(allActionGames::contains)
                .collect(collector);
        if (isActionGames) {
            checkGamesForTypeConsistency(chosenGames, allBankNonActionGames, isActionGames);
        } else {
            checkGamesForTypeConsistency(chosenGames, allBankActionGames, isActionGames);
        }
        switch (gameMode) {
            case ALL:
                return isActionGames ? allBankActionGames : allBankNonActionGames;
            case ONLY:
                return (isActionGames ? allBankActionGames.stream() : allBankNonActionGames.stream())
                        .filter(chosenGames::contains)
                        .collect(collector);
            case EXCEPT:
                return (isActionGames ? allBankActionGames.stream() : allBankNonActionGames.stream())
                        .filter(gameId -> !chosenGames.contains(gameId))
                        .collect(collector);
            default:
                throw new IllegalArgumentException("Unsupported bonus game mode: " + gameMode);
        }
    }

    private void checkGamesForTypeConsistency(Set<Long> desiredGames, Set<Long> bankGames, boolean isActionGames) {
        Sets.SetView<Long> incorrectGames = Sets.intersection(desiredGames, bankGames);
        if (!incorrectGames.isEmpty()) {
            throw new IllegalArgumentException("Following games: " + incorrectGames + " are not available when isActionGames: " + isActionGames);
        }
    }
}
