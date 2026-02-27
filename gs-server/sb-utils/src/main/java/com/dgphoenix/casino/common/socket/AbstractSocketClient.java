package com.abs.casino.common.socket;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Controllable;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.thoughtworks.xstream.XStream;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * User: flsh
 * Date: 4/29/11
 */
public abstract class AbstractSocketClient implements Controllable {
    private static final Logger LOG = Logger.getLogger(AbstractSocketClient.class);
    public static final String ZERO_DELIMITER = String.valueOf('\0');
    public static final long SLEEP_PAUSE = 500;

    protected boolean started;
    protected Thread thread;

    protected Socket socket;
    protected BufferedReader in;

    protected final String host;
    protected final int port;
    private String messageTail;
    protected XStream xstream;

    public AbstractSocketClient(String host, int port) {
        this.host = host;
        this.port = port;
        setupXStream();
    }

    protected long getSleepPause() {
        return SLEEP_PAUSE;
    }

    protected abstract void setupXStream();

    public abstract void debug(String s);

    public abstract void error(String s, Throwable e);

    protected abstract void setupClient() throws CommonException;

    protected abstract void processMessage(String msg) throws CommonException;

    public synchronized boolean startup() {
        if (isStarted() || isRunning()) {
            return false;
        }

        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create socket", e);
        }
        try {
            setupClient();
            if (!socket.isClosed()) {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.started = true;
                this.thread = new PlayThread();
                this.thread.start();
                debug("CLIENT STARTED");
                return true;
            } else {
                debug("SOCKET CLOSED");
                socket = null;
                in = null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot init socket client", e);
        }
        return false;
    }

    public boolean isStarted() {
        return this.started;
    }

    public boolean isRunning() {
        final Thread thread = this.thread;
        return thread != null && thread.isAlive();
    }

    public synchronized boolean shutdown() {
        if (!isStarted() || !isRunning()) {
            return false;
        }
        this.started = false;
        notify();
        if (socket != null) {
            try {
                debug("CLOSING CONNECTION");
//                in.close();
//                out.close();
                socket.close();
                socket = null;
                debug("CONNECTION CLOSED");
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return true;
    }

    protected LinkedList<String> readMessageList() throws IOException, InterruptedException {
        LinkedList<String> result = new LinkedList<String>();
        long now = 0;
        while (isStarted() && socket.isConnected() && result.isEmpty()) {
            if (in.ready()) {
                CharBuffer buffer = CharBuffer.allocate(1024);
                in.read(buffer);

                int length = buffer.position();
                String message = null;
                if (length != 0) {
                    buffer.flip();
                    char chars[] = new char[length];
                    buffer.get(chars, 0, length);
                    message = new String(chars);
                }

                if (now == 0 && !StringUtils.isTrimmedEmpty(message)) {
                    now = System.currentTimeMillis();
                }
                //System.out.println("AbstractSocketClient, read msg=" + message);
                if (!StringUtils.isTrimmedEmpty(message)) {
                    StringTokenizer st = new StringTokenizer(
                            messageTail == null ? message : messageTail + message,
                            ZERO_DELIMITER, true);
                    messageTail = null;
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        if (token.equals(ZERO_DELIMITER)) {
                            if (messageTail != null) {
                                result.addLast(messageTail);
                            }
                            messageTail = null;
                        } else {
                            messageTail = token;
                        }
                    }
                }
            }
            if (result.isEmpty() && StringUtils.isTrimmedEmpty(messageTail)) {
                //Thread.sleep(getSleepPause());
            }
        }
        //System.out.println("AbstractSocketClient, read time: " + (System.currentTimeMillis() - now) +
        //        " ms., msg=" + Arrays.asList(result));
        return result;
    }

    private final class PlayThread extends Thread {
        public void run() {
            try {
                while (started && socket.isConnected()) {
                    try {
                        LinkedList<String> messages = readMessageList();
                        for (String message : messages) {
                            processMessage(message);
                        }
                    } catch (Exception e) {
                        error("Stream processing error", e);
                    }
                }
            } catch (Throwable e) {
                error("Unexpected error", e);
            } finally {
                try {
                    shutdown();
                } catch (Throwable e) {
                    error("Cannot shutdown correctly, please restart", e);
                }
            }
        }
    }


}
