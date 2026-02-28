package com.abs.casino.actions.enter;

import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.abs.casino.common.web.MobileDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class LanguageDetector {

    private static Logger logger = LogManager.getLogger(LanguageDetector.class);

    private static Map<String, String> byAliasMap = new HashMap<String, String>();

    static {
        byAliasMap.put("sv", "se");
        byAliasMap.put("ja", "jp");
        byAliasMap.put("cs", "cz");
        byAliasMap.put("da", "dk");
        byAliasMap.put("pt-br", "pt");
    }

    public static boolean isLocalizationAvailable(String lang, BankInfo bankInfo, long gameId, AccountInfo accountInfo,
                                                  String userAgent) {
        try {
            lang = resolveLanguageAlias(lang);
            IBaseGameInfo gameInfo = getGameInfo(gameId, bankInfo);
            IBaseGameInfo alternateGameInfo = MobileDetector.getAlternateGameInfo(gameInfo, userAgent);
            if (alternateGameInfo != null && alternateGameInfo.getId() != gameId) {
                logger.debug("isLocalizationAvailable found using main gameId on mobileClient, main gameId=" + gameId +
                        ", alternateGameInfo=" + alternateGameInfo.getId());
                return alternateGameInfo.isExist(lang);
            }
            return gameInfo.isExist(lang);
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public static boolean isLocalizationAvailable(String lang, BankInfo bankInfo, long gameId, AccountInfo accountInfo) {
        try {
            lang = resolveLanguageAlias(lang);
            IBaseGameInfo gameInfo = getGameInfo(gameId, bankInfo);
            return gameInfo.isExist(lang);
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public static String getAlternateLanguage(BankInfo bankInfo, long gameId, AccountInfo accountInfo) {
        try {
            String language;
            IBaseGameInfo gameInfo = getGameInfo(gameId, bankInfo);
            String bankDefLang = bankInfo.getDefaultLanguage();
            if (gameInfo.isExist(bankDefLang)) {
                language = bankDefLang;
            } else {
                language = "en";
            }
            return language;
        } catch (Exception e) {
            logger.error(e);
            return "en";
        }
    }

    private static IBaseGameInfo getGameInfo(long gameId, BankInfo bankInfo) {
        return BaseGameCache.getInstance().getGameInfo(bankInfo.getId(), gameId, bankInfo.getDefaultCurrency());
    }

    public static String resolveLanguageAlias(String languageAlias) {
        if (byAliasMap.containsKey(languageAlias)) {
            return byAliasMap.get(languageAlias);
        } else {
            return languageAlias;
        }
    }

}
