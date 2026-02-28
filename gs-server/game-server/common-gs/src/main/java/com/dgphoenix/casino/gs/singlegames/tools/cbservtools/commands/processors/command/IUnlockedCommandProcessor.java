package com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.command;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.abs.casino.gs.managers.dblink.IDBLink;
import com.abs.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;

import javax.annotation.Nullable;
import javax.servlet.ServletRequest;

public interface IUnlockedCommandProcessor extends ICommandRelated {

    @Nullable
    ServerResponse processUnlocked(ServletRequest request, String sessionId, String command, IDBLink dbLink, ITransactionData data)
            throws CommonException;
}
