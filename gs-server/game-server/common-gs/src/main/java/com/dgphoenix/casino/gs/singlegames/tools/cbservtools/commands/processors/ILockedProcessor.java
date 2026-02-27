package com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors;

import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.abs.casino.gs.managers.dblink.IDBLink;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import static com.dgphoenix.casino.gs.singlegames.tools.cbservtools.IGameController.*;

public interface ILockedProcessor {

    Set<String> NOT_CHANGE_BALANCE_COMMANDS = ImmutableSet.of(
            CMDENTER,
            CMDRESTART,
            CMDUPDATEBALANCE,
            CMD_REFRESH_BALANCE
    );

    @Nullable
    ServerResponse processLocked(ServletRequest request, String sessionId, String command,
                                 ITransactionData transactionData, IDBLink dbLink,
                                 boolean roundFinished) throws CommonException, IOException;

    boolean canProcessCommand(String command, boolean isNewRoundBet);


    static Locale getLocale(IDBLink dbLink) {
        GameSession gameSession = dbLink.getGameSession();
        Locale locale = Locale.getDefault();
        if (gameSession != null) {
            locale = new Locale(gameSession.getLang());
        }
        return locale;
    }
}
