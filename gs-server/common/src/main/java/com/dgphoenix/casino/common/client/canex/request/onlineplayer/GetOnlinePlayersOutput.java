package com.abs.casino.common.client.canex.request.onlineplayer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetOnlinePlayersOutput {
    private static final Logger LOG = LogManager.getLogger(GetOnlinePlayersOutput.class);

    @JsonProperty("EXTSYSTEM")
    @SerializedName("EXTSYSTEM")
    private GetOnlinePlayersExtSystem extSystem;

    public GetOnlinePlayersOutput() {
    }

    public GetOnlinePlayersOutput(GetOnlinePlayersRequest request, GetOnlinePlayersResponse response, String time) {
        extSystem = new GetOnlinePlayersExtSystem();
        extSystem.setRequest(request);
        extSystem.setResponse(response);
        extSystem.setTime(time);
    }

    public GetOnlinePlayersExtSystem getExtSystem() {
        return extSystem;
    }

    public void setExtSystem(GetOnlinePlayersExtSystem extSystem) {
        this.extSystem = extSystem;
    }
}
