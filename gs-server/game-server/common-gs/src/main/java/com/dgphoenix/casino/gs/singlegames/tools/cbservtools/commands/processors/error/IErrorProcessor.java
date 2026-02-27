package com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.error;

import com.abs.casino.gs.managers.dblink.IDBLink;
import com.abs.casino.gs.singlegames.tools.cbservtools.GameError;
import com.abs.casino.gs.singlegames.tools.cbservtools.commands.responses.ErrorResponse;

import javax.annotation.Nullable;

public interface IErrorProcessor {
    @Nullable
    ErrorResponse process(IDBLink dbLink, String command, Throwable exception);

    boolean canProcessError(GameError error, Throwable exception, IDBLink dbLink);
}
