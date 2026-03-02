package com.abs.casino.gs.managers.game.session;

import com.abs.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.KryoSerializable;

public interface INotifyResponseProcessor extends KryoSerializable {
    void process(CloseGameSessionNotifyRequest request) throws CommonException;
}
