package com.abs.casino.actions.enter.game.cwv3;

import com.dgphoenix.casino.actions.enter.game.cwv3.CWStartGameForm;

/**
 * Backward-compatible CW start form that accepts legacy `sessionId`
 * as auth token source for restart flows.
 */
public class CWStartGameBySessionForm extends CWStartGameForm {

    @Override
    protected String getTokenParamName() {
        return "sessionId";
    }
}
