package com.dgphoenix.casino.gs.singlegames.tools.cbservtools;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created
 * Date: 19.03.2009
 * Time: 15:59:36
 */
public class GameMessages {

    private static GameMessages instance = new GameMessages();

    public static GameMessages getInstance() {
        return instance;
    }


    private ResourceBundle bundle;

    public GameMessages() {
        initGameMessage();
    }

    private void initGameMessage() {
        String lang = "en";
        Locale locale = new Locale(lang);
        bundle = ResourceBundle.getBundle("com.dgphoenix.casino.gs.singlegames.tools.cbservtools.GameMessages", locale);
    }

    public String getMessage(String key) {
        return bundle.getString(key);
    }
}
