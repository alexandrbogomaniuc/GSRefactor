package com.dgphoenix.casino.common.socket;

import com.dgphoenix.casino.common.exception.CommonException;
import org.apache.log4j.Logger;

/**
 * Created by ANGeL
 * Date: Jan 7, 2008
 * Time: 4:47:57 PM
 */
public class SocketServerObserver implements ISocketServerObserver {
    private static final Logger LOG = Logger.getLogger(SocketServerObserver.class);
    private SocketServer server;

    public void setServer(SocketServer server) {
        this.server = server;
    }

    public void acceptedConnection(ISocketClient client) {
        LOG.debug("new client connected " + client);
    }

    public void connectionClosed(ISocketClient client) {
        LOG.debug("client connection closed " + client);
    }

    public void dataReceived(ISocketClient client) {
        while (client.hasNextMessage()) {
            String message = client.getMessage();
            LOG.debug("message from client " + client + " text : " + message);
            if ("q".equals(message)) {
                try {
                    server.closeConnection(client);
                } catch (CommonException e) {
                    LOG.error(e.getMessage(), e);
                }
            } else if ("w".equals(message)) {
                server.shutdown();
            } else {
                client.sendMessage(message);
            }
        }
    }
}
