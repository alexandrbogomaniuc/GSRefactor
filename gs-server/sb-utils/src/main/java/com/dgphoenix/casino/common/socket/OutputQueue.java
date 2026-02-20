package com.dgphoenix.casino.common.socket;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;

/**
 * User: ktd
 * Date: 14.02.2007
 */
public class OutputQueue implements Serializable {
    private static final Logger LOG = Logger.getLogger(OutputQueue.class);


    private static final int DEFAULT_BUFFER_SIZE = 20000;
    private ByteBuffer byteBuffer;
    private LinkedList<String> msgList;

    public OutputQueue() {
        msgList = new LinkedList<String>();
        byteBuffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
    }

    public synchronized void queueMessage(String message) {
        msgList.addLast(message);
//        drainMessageQueue();
    }

    public synchronized int writeBytes(WritableByteChannel channel)
            throws IOException {

        drainMessageQueue();
        byteBuffer.flip();
        int rc;
        try {
//            LOG.debug("OutputQueue :: writeBytes before write");
            rc = channel.write(byteBuffer);
//            LOG.debug("OutputQueue :: writeBytes " + rc + " bytes has been sent");
        } catch (ClosedChannelException e) {
            throw new IOException(e.getMessage());
        }
        byteBuffer.compact();
        return rc;
    }

    private void drainMessageQueue() {
        for (; msgList.size() > 0;) {
            String message = msgList.getFirst();
            if (message != null) {
                byte[] bytes = message.getBytes();
                if (byteBuffer.remaining() < bytes.length + 1) {
                    if (byteBuffer.position() == 0) {
                        // message longer than buffer capacity
                        int len = byteBuffer.remaining();
                        while (byteBuffer.remaining() < (message.substring(0, len).getBytes().length + 1)) {
                            len--;
                        }
                        byteBuffer.put(message.substring(0, len).getBytes());
                        msgList.removeFirst();
                        msgList.addFirst(message.substring(len));
//                        msgList.add(1, message.substring(len));
//                        msgList.remove(0);
                    } else {
                        break;
                    }
                } else {
                    byteBuffer.put(bytes);
                    msgList.removeFirst();
                }
            } else {
                LOG.debug("MESSAGE is null");
                try {
                    msgList.removeFirst();
                } catch (Throwable e) {
                    LOG.error("remove first element exception", e);
                    break;
                }
            }
        }
    }

    public synchronized boolean isEmpty() {
        drainMessageQueue();
        return byteBuffer.position() == 0 && msgList.size() == 0;
    }

    public synchronized void flushAll() {
        byteBuffer.clear();
        msgList.clear();
    }
}

