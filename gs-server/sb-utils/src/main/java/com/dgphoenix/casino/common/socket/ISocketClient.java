package com.dgphoenix.casino.common.socket;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * Created by ANGeL
 * Date: Dec 29, 2007
 * Time: 7:21:01 PM
 */
public interface ISocketClient {
    void sendMessage(String message);

    void sendMessageAndClose(String message);

    boolean hasNextMessage();

    String getMessage();

    SocketServer getSocketServer();

    void closeConnection() throws CommonException;
}
