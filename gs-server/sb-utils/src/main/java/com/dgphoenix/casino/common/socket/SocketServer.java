package com.dgphoenix.casino.common.socket;

import com.dgphoenix.casino.common.engine.IControllable;
import com.dgphoenix.casino.common.exception.CommonException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ANGeL
 * Date: Dec 29, 2007
 * Time: 7:21:01 PM
 */
public class SocketServer implements IControllable {
    private static final Logger LOG = Logger.getLogger(SocketServer.class);
    private boolean started;
    private Selector selector;
    ISocketServerObserver observer;
    private static final long SELECT_DELAY = 200l;
    private int portNumber;
    Thread thread;
    ServerSocketChannel serverSocketChannel;
    int readBufferCapacity = 500;

    private ThreadPoolExecutor threadPool;

    private HashMap<SelectionKey, SocketClient> clients;

    public SocketServer(ISocketServerObserver obs, int portNumber) {
        this.observer = obs;
        this.portNumber = portNumber;
        clients = new HashMap<SelectionKey, SocketClient>();
        threadPool = new ThreadPoolExecutor(100, 500, 3, TimeUnit.HOURS, new LinkedBlockingQueue<Runnable>());
        threadPool.setRejectedExecutionHandler(new RejectedHandler());
    }

    public boolean startup() {
        if (isStarted()) {
            LOG.info("[SocketServerThread] already started");
            return false;
        }
        if (isRunning()) {
            LOG.info("[SocketServerThread] already started");
            return false;
        }
        LOG.debug("[SocketServerThread] startup");

        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(portNumber));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Throwable e) {
            LOG.error("SocketServer" + portNumber + " :: startup exception", e);
            return false;
        }

        this.started = true;
        this.thread = new Server();
        this.thread.start();

        return true;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isRunning() {
        return thread != null && thread.isAlive();
    }

    public synchronized boolean shutdown() {
        if (!isStarted()) {
            LOG.info("[SocketServerThread] not started");
            return false;
        }
        if (!isRunning()) {
            LOG.info("[SocketServerThread] not started");
            return false;
        }
        LOG.debug("[SocketServerThread] shutdown");
        this.started = false;
        threadPool.shutdownNow();
        notify();
        return true;
    }

    public synchronized void closeConnection(ISocketClient socketClient) throws CommonException {
        if (!SocketClient.class.equals(socketClient.getClass())) {
            throw new CommonException("SocketServer. Close connection. Invalid Client class Exception");
        }
        
        if (socketClient != null && clients.containsValue(socketClient)){
        	closeClient((SocketClient) socketClient);
        }
    }

    private void closeClient(SocketClient client) {
    	LOG.debug("[SocketServer] Closing connection " + client);
        closeKey(client.getKey());
        clients.remove(client.getKey());
        try{
        	threadPool.execute(new ConnectionClosedNotifier(client, observer));
        } catch (RejectedExecutionException e){
        	LOG.error("[SocketServer]closeClient rejected client:" + client, e);
        }
        
    }

    private void acceptConnection() throws IOException {
        SocketChannel clientSocketChannel = serverSocketChannel.accept();
        clientSocketChannel.configureBlocking(false);
        SelectionKey clientKey = clientSocketChannel.register(selector, SelectionKey.OP_READ);
        SocketClient client = new SocketClient(clientKey, this);
        clients.put(clientKey, client);
        clientKey.attach(client);
        
        try{
        	threadPool.execute(new ConnectionClosedNotifier(client, observer));
        	LOG.debug("[SocketServer] Accepted connection from IP " + clientSocketChannel.socket()
                    .getRemoteSocketAddress());
        } catch (RejectedExecutionException e){
        	LOG.error("[SocketServer]acceptConnection rejected client:" + client, e);
        }        
    }

    static void closeKey(SelectionKey key) {
        key.cancel();
        if (key.channel() instanceof SocketChannel) {
            try {
                ((SocketChannel) key.channel()).socket().close();
                key.channel().close();
            } catch (IOException e) {
                LOG.error("[SocketServer] Closing client channel exception", e);
            }
        }
    }

    private void readData(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketClient client = (SocketClient) key.attachment();
        ByteBuffer buffer = ByteBuffer.allocateDirect(readBufferCapacity);
        buffer.clear();
        try {
            int numRead = channel.read(buffer);
            if (numRead == -1) {
                throw new IOException();
            }
        } catch (IOException e) {
            LOG.debug("Reading exception, Closing connection " + client);
            closeClient(client);
            return;
        }
        int length = buffer.position();
        if (length != 0) {
            buffer.flip();
            byte bytes[] = new byte[length];
            buffer.get(bytes, 0, length);
            String message = new String(bytes);
            client.addMessage(message);
            threadPool.execute(new DataReceivedNotifier(client, observer));
        }
    }

    private class Server extends Thread {
        public void run() {
            while (isStarted()) {
                SelectionKey key;
                try {
                    if (selector.select(SELECT_DELAY) > 0) {
                        if (!isStarted()) {
                            break;
                        }

                        Iterator it = selector.selectedKeys().iterator();
                        while (it.hasNext()) {
                            key = (SelectionKey) it.next();
                            it.remove();
                            if (!key.isValid()) {
                                continue;
                            }
                            if (key.isAcceptable()) {
                                acceptConnection();
                            } else if (key.isReadable()) {
                                readData(key);
                            } else if (key.isWritable()) {
                                SocketClient client = (SocketClient) key.attachment();
                                client.writeData();
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Cannot process connection", e);
                }
            }

            LOG.debug("[ServerSocket] thread stoped. Closing sockets...");
            try {
                serverSocketChannel.socket().close();
                serverSocketChannel.close();
            } catch (IOException e) {
                LOG.error("[SocketServer] Closing server socket exception", e);
            }
            LOG.debug("[ServerSocket] server channel closed.");

            for (SelectionKey key : selector.keys()) {
                SocketClient client = (SocketClient) key.attachment();
                if (client != null) {
                    closeClient(client);
                } else {
                    closeKey(key);
                }
            }

            try {
                selector.close();
            } catch (IOException e) {
                LOG.error("[SocketServer] Closing selector exception", e);
            }

            LOG.debug("[SocketServer] All sockets closed");

            for (SocketClient client : clients.values()) {
                clients.remove(client.getKey());
                threadPool.execute(new ConnectionClosedNotifier(client, observer));
            }
        }
    }

    class SocketClient implements ISocketClient {
        private SelectionKey key;
        private OutputQueue outputQueue;
        private LinkedList<String> messages;
        private boolean closeIt;
        private SocketServer server;

        public SocketClient(SelectionKey key, SocketServer server) {
            this.key = key;
            this.outputQueue = new OutputQueue();
            this.messages = new LinkedList<String>();
            this.closeIt = false;
            this.server = server;
        }

        public void sendMessage(String message) {
            LOG.debug("" + this + " sending message " + message);
            if (message != null) {
                try {
                    outputQueue.queueMessage(message + '\0');
                    key.interestOps(SelectionKey.OP_WRITE);
                    key.selector().wakeup();
                } catch (CancelledKeyException e) {
                    LOG.error("Cannot send command, client disconnected", e);
                    closeClient(this);
                }
            }
        }

        public void sendMessageAndClose(String message) {
            sendMessage(message);
            closeIt = true;
        }

        public synchronized boolean hasNextMessage() {
            return !messages.isEmpty();
        }

        public synchronized String getMessage() {
            if (messages.isEmpty()) {
                return null;
            } else {
                String message = messages.getFirst();
                messages.removeFirst();
                return message;
            }
        }

        public SocketServer getSocketServer() {
            return server;
        }

        public void closeConnection() throws CommonException {
            LOG.debug("SocketClient :: close connection called : " + this);
            server.closeConnection(this);
        }

        SelectionKey getKey() {
            return key;
        }

        void writeData() {
            if (!outputQueue.isEmpty()) {
                try {
                    outputQueue.writeBytes((WritableByteChannel) key.channel());
                } catch (IOException e) {
                    LOG.error("Cannot write data to client", e);
                    outputQueue.flushAll();
                    closeClient(this);
                }
            }
            if (outputQueue.isEmpty()) {
                key.interestOps(SelectionKey.OP_READ);
                if (closeIt) {
                    LOG.debug("GameSocketServer :: closing connection as requested"  + this) ;
                    closeClient(this);
                }
            }
        }

        public synchronized void addMessage(String message) {
            messages.addLast(message);
        }

        public String toString() {
            return "SocketServerClient {IP=" + ((SocketChannel) key.channel()).socket().getRemoteSocketAddress() + "}";
        }
    }

    class ConnectionAcceptedNotifier implements Runnable {
        SocketClient client;
        ISocketServerObserver obs;

        ConnectionAcceptedNotifier(SocketClient client, ISocketServerObserver obs) {
            this.client = client;
            this.obs = obs;
        }

        public void run() {
            obs.acceptedConnection(client);
        }
    }

    class ConnectionClosedNotifier implements Runnable {
        SocketClient client;
        ISocketServerObserver obs;

        ConnectionClosedNotifier(SocketClient client, ISocketServerObserver obs) {
            this.client = client;
            this.obs = obs;
        }

        public void run() {
            obs.connectionClosed(client);
        }
    }

    class DataReceivedNotifier implements Runnable {
        SocketClient client;
        ISocketServerObserver obs;

        DataReceivedNotifier(SocketClient client, ISocketServerObserver obs) {
            this.client = client;
            this.obs = obs;
        }

        public void run() {
            if (client.hasNextMessage()) {
                obs.dataReceived(client);
            }
        }
    }

    public class RejectedHandler implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            LOG.debug("Task rejected by threadPool. RERUNNING in new Thread. Task:" + r);
            new Thread(r).start();
        }
    }

    public static void main(String[] args) {
        SocketServerObserver socketServerObserver = new SocketServerObserver();
        SocketServer socketServer = new SocketServer(socketServerObserver, 12345);
        socketServerObserver.setServer(socketServer);
        socketServer.startup();
        try {
            Thread.sleep(30000);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

//        socketServer.shutdown();
//        try {
//            Thread.sleep(2000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}