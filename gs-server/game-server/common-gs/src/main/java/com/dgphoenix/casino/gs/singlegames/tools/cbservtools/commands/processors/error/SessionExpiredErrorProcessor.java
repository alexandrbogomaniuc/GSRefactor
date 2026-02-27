package com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.processors.error;

import com.abs.casino.gs.managers.dblink.IDBLink;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.GameError;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.responses.ErrorResponse;
import org.springframework.core.annotation.Order;

import javax.annotation.Nullable;

@Order(5)
public class SessionExpiredErrorProcessor implements IErrorProcessor {

    @Nullable
    @Override
    public ErrorResponse process(IDBLink dbLink, String command, Throwable exception) {
        return null;
    }

    @Override
    public boolean canProcessError(GameError error, Throwable exception, IDBLink dbLink) {
        return error == GameError.SESSION_EXPIRED_ERROR;
    }
}
