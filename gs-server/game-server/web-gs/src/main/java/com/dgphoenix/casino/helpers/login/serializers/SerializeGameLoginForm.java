package com.dgphoenix.casino.helpers.login.serializers;

import com.dgphoenix.casino.forms.game.CommonStartGameForm;
import com.dgphoenix.casino.forms.login.CommonGameLoginForm;
import com.dgphoenix.casino.sm.login.GameLoginRequest;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * User: isirbis
 * Date: 14.10.14
 */
public class SerializeGameLoginForm<L extends CommonGameLoginForm, S extends CommonStartGameForm> extends
        SerializeLoginForm<L, S> {
    public GameLoginRequest getLoginRequest(L form) throws InvocationTargetException, IllegalAccessException {
        GameLoginRequest loginRequest = new GameLoginRequest();
        BeanUtils.copyProperties(loginRequest, super.getLoginRequest(form));

        loginRequest.setGameMode(form.getGameMode());
        loginRequest.setGameId(form.getGameId());

        return loginRequest;
    }

    public GameLoginRequest getLoginRequest(S form) throws InvocationTargetException, IllegalAccessException {
        GameLoginRequest loginRequest = new GameLoginRequest();
        BeanUtils.copyProperties(loginRequest, super.getLoginRequest(form));

        loginRequest.setGameMode(form.getGameMode());
        loginRequest.setGameId(form.getGameId());

        return loginRequest;
    }
}
