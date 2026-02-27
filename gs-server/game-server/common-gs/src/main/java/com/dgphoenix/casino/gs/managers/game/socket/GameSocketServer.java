package com.dgphoenix.casino.gs.managers.game.socket;

import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.configuration.ServerConfiguration;
import com.dgphoenix.casino.common.engine.IControllable;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.socket.ISocketClient;
import com.abs.casino.common.socket.ISocketServerObserver;
import com.abs.casino.common.socket.SocketServer;
import com.abs.casino.common.socket.SocketTools;
import com.dgphoenix.casino.gs.persistance.PlayerSessionPersister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created
 * Date: 02.12.2008
 * Time: 17:32:09
 */
public class GameSocketServer implements ISocketServerObserver, IControllable {
    private static final Logger LOG = LogManager.getLogger(GameSocketServer.class);
    private int port;
    private SocketServer socketServer;
    private Set<ISocketClient> connections;
    private Map<String, IGameSocketObserver> observers;
    private Map<ISocketClient, IGameSocketObserver> clientObservers;
    private Map<ISocketClient, String> sessions;

    private String policyFile;

    public GameSocketServer() {
        socketServer = null;
        port = 0;
    }

    public void init(int port) throws CommonException {
        this.port = port;
        if (socketServer != null) {
            socketServer.shutdown();
        }
        socketServer = new SocketServer(this, port);
        connections = new HashSet<>();
        observers = new HashMap<>();
        clientObservers = new HashMap<>();
        sessions = new ConcurrentHashMap<>();

        String domain = ServerConfiguration.getInstance().getStringProperty("socket.main_serv.domain");
        policyFile = SocketTools.buildPolicyFile(domain, port);
    }

    public boolean startup() {
        LOG.info("startup");
        return socketServer.startup();
    }

    public boolean isStarted() {
        return socketServer.isStarted();
    }

    public boolean isRunning() {
        return socketServer.isRunning();
    }

    public boolean shutdown() {
        LOG.info("shutdown");
        if (connections != null) {
            for (ISocketClient iSocketClient : new HashSet<ISocketClient>(connections)) {
                removeConnection(iSocketClient);
            }
        }
        return socketServer == null || socketServer.shutdown();
    }

    public void acceptedConnection(ISocketClient client) {
        LOG.debug("accepted new connection: " + client);
        connections.add(client);
    }

    public void connectionClosed(ISocketClient client) {
        LOG.debug("connection closed: " + client);
        removeConnection(client);
    }

    public void dataReceived(ISocketClient client) {
        String message = client.getMessage();
        LOG.debug("received message from " + client + " message=\"" + (message == null ? "[null]" : message) + "\"");

        if (SocketTools.isPolicyFileRequest(message)) {
            //LOG.debug("sending policy file");
            client.sendMessageAndClose(policyFile);
        } else if (message != null && !"".equals(message)) {
            // flash sends 0 (remove last byte)
            if (message.charAt(message.length() - 1) == 0) {
                message = message.substring(0, message.length() - 1);
            }

            LOG.debug("before search message=\"" + message + "\"");
            try {
                StringTokenizer st = new StringTokenizer(message, " ");
                String obsKey = st.nextToken();
                String sessionId = st.nextToken();

                if (observers.containsKey(obsKey)) {
                    SessionInfo sessionInfo = PlayerSessionPersister.getInstance().getSessionInfo();
                    if (sessionInfo != null) {
                        IGameSocketObserver obs = observers.get(obsKey);
                        if (obs.acceptConnection(client, sessionInfo)) {
                            LOG.debug("Connection accepted. " + client);
                            clientObservers.put(client, obs);
                            sessions.put(client, sessionId);
                        } else {
                            LOG.debug("Connection rejected. closing connection " + client);
                            closeConnection(client);
                        }
                    } else {
                        LOG.debug("Session not found. closing connection " + client);
                        closeConnection(client);
                    }
                } else {
                    LOG.debug("Observer not found. closing connection " + client);
                    closeConnection(client);
                }
            } catch (Throwable e) {
                LOG.error("read socket data error. closing connection " + client, e);
                closeConnection(client);
            }
        } else {
            LOG.debug("Empty message. closing connection " + client);
            closeConnection(client);
        }
    }

    public void closeConnection(ISocketClient client) {
        LOG.debug("closing connection " + client);
        try {
            socketServer.closeConnection(client);
            removeConnection(client);
        } catch (CommonException e) {
            LOG.error("close connection error", e);
        }
    }

    public int getPort() {
        return port;
    }

    public void addObserver(String key, IGameSocketObserver obs) throws CommonException {
        if (observers.containsKey(key)) throw new CommonException("Observer Key is busy");
        observers.put(key, obs);
    }

    public void removeObserver(String key) throws CommonException {
        IGameSocketObserver obs = observers.get(key);
        if (obs != null) {
            observers.remove(key);
            Set<ISocketClient> clientsToRemove = new HashSet<ISocketClient>();
            for (Map.Entry<ISocketClient, IGameSocketObserver> clientObserverEntry : clientObservers.entrySet()) {
                if (clientObserverEntry.getValue().equals(obs)) {
                    clientsToRemove.add(clientObserverEntry.getKey());
                }
            }
            for (ISocketClient client : clientsToRemove) {
                removeConnection(client);
            }
        }
    }

    private void removeConnection(ISocketClient client) {
        connections.remove(client);
        IGameSocketObserver obs = clientObservers.get(client);
        if (obs != null) {
            String sessionId = sessions.get(client);
            obs.connectionClosed(client, PlayerSessionPersister.getInstance().getSessionInfo());
            clientObservers.remove(client);
            sessions.remove(client);
        }
    }

}
