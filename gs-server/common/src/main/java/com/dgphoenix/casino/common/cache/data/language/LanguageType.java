package com.abs.casino.common.cache.data.language;

import com.abs.casino.common.util.string.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum LanguageType {
    ENGLISH("en"),
    RUSSIAN("ru"),
    NORWEGIAN("no"),
    SWEDISH("se"),
    DUTCH("nl"),
    ITALIAN("it"),
    DANISH("dk"),
    FINNISH("fi"),
    SPANISH("es"),
    GREEK("el"),
    FRENCH("fr"),
    GERMAN("de"),
    ROMANIAN("ro"),
    TURKISH("tr"),
    POLISH("pl"),
    PORTUGUESE("pt-eu"),
    BRAZILIANPORTUGUESE("pt"),
    CHINESESIMPLIFIED("zh-cn"),
    CHINESETRADITIONAL("zh"),
    VIETNAMESE("vi"),
    HUNGARIAN("hu"),
    JAPANESE("jp"),
    BULGARIA("bg"),
    CZECH("cz"),
    SLOVAKIA("sk"),
    CHINESETAIWAN("zh-tw"),
    KOREAN("ko"),
    THAI("th"),
    INDONESIAN("id"),
    DANISH_SP("dk-sp"),
    KHMER("km"), //Cambodian
    UKRAINIAN("uk");

    private String code;
    private static Map<String, LanguageType> byTypeMap = new HashMap<String, LanguageType>();

    static {
        for (LanguageType langType : values()) {
            byTypeMap.put(langType.getCode(), langType);
        }
    }

    LanguageType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static LanguageType toLanguageType(String code) {
        return StringUtils.isTrimmedEmpty(code) ? null : byTypeMap.get(code.toLowerCase());
    }
}
