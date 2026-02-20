package com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.responses;

import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;

import javax.annotation.Nonnull;

public class ErrorResponse {

    private final ServerResponse serverResponse;
    private final boolean processCanBeContinued;

    public ErrorResponse(@Nonnull ServerResponse serverResponse, boolean processCanBeContinued) {
        this.serverResponse = serverResponse;
        this.processCanBeContinued = processCanBeContinued;
    }

    @Nonnull
    public ServerResponse getServerResponse() {
        return serverResponse;
    }

    public boolean canProcessBeContinued() {
        return processCanBeContinued;
    }
}
