package com.abs.casino.common.socket;


/**
 * Created by ANGeL
 * Date: Dec 29, 2007
 * Time: 5:34:11 PM
 */
public interface ISocketServerObserver {
    void acceptedConnection(ISocketClient client);
    void connectionClosed(ISocketClient client);
    void dataReceived(ISocketClient client);
}
