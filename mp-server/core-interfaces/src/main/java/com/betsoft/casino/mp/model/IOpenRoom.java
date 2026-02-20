package com.betsoft.casino.mp.model;

import com.betsoft.casino.utils.InboundObject;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IOpenRoom extends InboundObject {
    int getServerId();

    void setServerId(int serverId);

    long getRoomId();

    void setRoomId(long roomId);

    String getSid();

    void setSid(String sid);

    String getLang();

    void setLang(String lang);
}
