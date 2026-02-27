package com.abs.casino.common.cache.data.game;

import java.util.LinkedList;
import java.util.List;

/**
 * User: isirbis
 * Date: 12.09.14
 */
public enum GameLanguageHelper {
    TENSBETTER {
        public List<String> getGameLanguagesPaths(String languageCode) {
            List<String> list = new LinkedList<String>();
            String gameFolderName = "10sorbetter";

            list.add(getFreeLanguagePath(gameFolderName, languageCode));
            list.add(getRealLanguagePath(gameFolderName, languageCode));
            return list;
        }
    };

    public abstract List<String> getGameLanguagesPaths(String languageCode);

    public static String getTranslateXMLPath(String gameFolderName, String languageCode) {
        return "/flash/" + gameFolderName + "/translate/" + "xml/language_" + languageCode + ".xml";
    }

    public static String getTranslateDirPath(String gameFolderName, String languageCode) {
        return "/flash/" + gameFolderName + "/translate/" + languageCode;
    }

    public static String getFreeLanguagePath(String gameFolderName, String languageCode) {
        return "/free/" + languageCode + "/flash/" + gameFolderName + "/language.swf";
    }

    public static String getRealLanguagePath(String gameFolderName, String languageCode) {
        return "/real/" + languageCode + "/flash/" + gameFolderName + "/language.swf";
    }

    public static String getFreeFontAssetsPath(String gameFolderName, String languageCode) {
        return "/free/" + languageCode + "/flash/" + gameFolderName + "/fontAssets.swf";
    }

    public static String getRealFontAssetsPath(String gameFolderName, String languageCode) {
        return "/real/" + languageCode + "/flash/" + gameFolderName + "/fontAssets.swf";
    }
}
