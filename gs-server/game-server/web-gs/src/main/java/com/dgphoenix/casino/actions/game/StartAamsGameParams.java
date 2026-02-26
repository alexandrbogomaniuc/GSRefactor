package com.abs.casino.actions.game;

import java.io.Serializable;

/**
 * User: van0ss
 * Date: 03.02.2017
 */
public class StartAamsGameParams implements Serializable {
    private String aamsSessionId;
    private String aamsTicketId;
    private String startGameUrl;

    public String getAamsSessionId() {
        return aamsSessionId;
    }

    public void setAamsSessionId(String aamsSessionId) {
        this.aamsSessionId = aamsSessionId;
    }

    public String getAamsTicketId() {
        return aamsTicketId;
    }

    public void setAamsTicketId(String aamsTicketId) {
        this.aamsTicketId = aamsTicketId;
    }

    public String getStartGameUrl() {
        return startGameUrl;
    }

    public void setStartGameUrl(String startGameUrl) {
        this.startGameUrl = startGameUrl;
    }
}
