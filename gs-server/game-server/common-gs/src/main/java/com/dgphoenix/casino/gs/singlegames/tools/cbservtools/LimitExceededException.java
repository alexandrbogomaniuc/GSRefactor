package com.abs.casino.gs.singlegames.tools.cbservtools;


import com.abs.casino.common.exception.CommonException;
import com.abs.casino.gs.singlegames.tools.cbservtools.response.ServerResponse;

public class LimitExceededException extends CommonException {

    ServerResponse response;

    public LimitExceededException(ServerResponse response) {
        super(response.toString());
        this.response = response;
    }

    public ServerResponse getResponse() {
        return response;
    }

    public void setResponse(ServerResponse response) {
        this.response = response;
    }
}
