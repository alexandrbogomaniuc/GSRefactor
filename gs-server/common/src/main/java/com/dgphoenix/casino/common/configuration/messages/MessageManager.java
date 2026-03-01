package com.abs.casino.common.configuration.messages;

import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.BaseGameInfoTemplateCache;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.game.BaseGameInfoTemplate;
import org.apache.commons.lang.LocaleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.util.MessageResources;

import java.util.Locale;

import static com.abs.casino.common.util.string.StringUtils.isTrimmedEmpty;;

public class MessageManager {
    private static final MessageManager instance = new MessageManager();
    private static final Logger LOG = LogManager.getLogger(MessageManager.class);

    public static final String APPLICATION_MESSAGE_BUNDLE =
            "com.dgphoenix.casino.common.configuration.messages.ApplicationMessage";

    public static final String LOCALIZATION_MESSAGE_BUNDLE =
            "com.dgphoenix.casino.common.configuration.messages.LocalizationMessage";

    public static final String GAME_SERVLET_MESSAGE_BUNDLE =
            "com.dgphoenix.casino.common.configuration.messages.GameServletMessage";

    public static final String BANK_PREFIX = "BANK.";
    public static final String SUBCASINO_PREFIX = "SUBCASINO.";
    public static final String CW_ERROR_PREFIX = "CW_ERROR.";
    public static final String GAME_NAME_PREFIX = "game.name.";

    private MessageManager() {
    }

    public static MessageManager getInstance() {
        return instance;
    }

    private String getResourceMessage(Locale locale, String messageBundleName, String key) {
        return MessageResources.getMessageResources(messageBundleName).getMessage(locale, key);
    }

    private String getResourceMessage(String messageBundleName, String key) {
        return MessageResources.getMessageResources(messageBundleName).getMessage(key);
    }

    public String getApplicationMessage(String key) {
        if (key.contains(GAME_NAME_PREFIX)) {
            String name = key.substring(10);
            Long gameIdByName = BaseGameInfoTemplateCache.getInstance().getGameIdByName(name);
            if (gameIdByName == null) {
                return getResourceMessage(APPLICATION_MESSAGE_BUNDLE, key);
            } else {
                BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getObject(
                        String.valueOf(gameIdByName));
                if (template == null) {
                    throw new RuntimeException("Cannot load template, key=" + key + ", gameIdByName=" + gameIdByName);
                }
                return template.getTitle();
            }
        } else {
            return getResourceMessage(APPLICATION_MESSAGE_BUNDLE, key);
        }
    }

    public String getApplicationMessage(Locale locale, String key) {
        return getResourceMessage(locale, APPLICATION_MESSAGE_BUNDLE, key);
    }

    public String getGameTitle(String key, Locale locale) {
        return getResourceMessage(locale, LOCALIZATION_MESSAGE_BUNDLE, key);
    }

    public String getGameServletMessage(String key) {
        return getResourceMessage(GAME_SERVLET_MESSAGE_BUNDLE, key);
    }

    public String getGameServletMessage(Locale locale, String key) {
        return getResourceMessage(locale, GAME_SERVLET_MESSAGE_BUNDLE, key);
    }

    public String getGameServletMessage(Locale locale, String key, BankInfo bankInfo) {
        String message = null;
        if (bankInfo != null) {
            String bankKey = BANK_PREFIX + bankInfo.getId() + '.' + key;
            message = getResourceMessage(locale, GAME_SERVLET_MESSAGE_BUNDLE, bankKey);
            if (message == null) {
                String subCasinoKey = SUBCASINO_PREFIX + bankInfo.getSubCasinoId() + '.' + key;
                message = getResourceMessage(locale, GAME_SERVLET_MESSAGE_BUNDLE, subCasinoKey);
            }
        }

        if (message == null) {
            message = getResourceMessage(locale, GAME_SERVLET_MESSAGE_BUNDLE, key);
        }
        return message;
    }

    private static Locale getGameTitleLocale(long bankId, String lang) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        Locale locale = Locale.ENGLISH;
        if (bankInfo != null && bankInfo.localizeGameTitle() && !isTrimmedEmpty(lang)) {
            locale = getRequestLocale(locale, lang);
        }
        return locale;
    }

    private static Locale getRequestLocale(Locale locale, String lang) {
        try {
            locale = LocaleUtils.toLocale(lang);
        } catch (IllegalArgumentException ignore) {
            LOG.debug("Incorrect LANG property:{}", lang);
        }
        return locale;
    }

    public static String getLocalizedTitleOrDefault(long bankId, long gameId, String lang) {
        Locale locale = getGameTitleLocale(bankId, lang);
        String key = GAME_NAME_PREFIX + gameId;
        return MessageManager.getInstance().getGameTitle(key, locale);
    }
}
