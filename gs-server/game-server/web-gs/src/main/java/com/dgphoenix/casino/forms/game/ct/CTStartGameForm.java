package com.abs.casino.forms.game.ct;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Created by : angel
 * Date: 14.02.2011
 * Time: 16:24:52
 */
public class CTStartGameForm extends CommonCTStartGameForm {
    private final static Logger LOG = LogManager.getLogger(CTStartGameForm.class);

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
