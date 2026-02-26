package com.abs.casino.forms.game.cwv3;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * User: isirbis
 * Date: 07.10.14
 */
public class CWStartGameForm extends AbstractCWStartGameForm {
    private final static Logger LOG = LogManager.getLogger(CWStartGameForm.class);


    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public boolean isLaunchChecked() {
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CWStartGameForm");
        sb.append("{token='").append(token).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
