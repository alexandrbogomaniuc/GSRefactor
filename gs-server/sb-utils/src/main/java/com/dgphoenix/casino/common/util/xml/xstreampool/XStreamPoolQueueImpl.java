package com.abs.casino.common.util.xml.xstreampool;

import com.thoughtworks.xstream.XStream;

import java.util.Queue;

/**
 * User: flsh
 * Date: 06.12.14.
 */
public class XStreamPoolQueueImpl implements XStreamPool {
    private final Queue<XStream> queue;
    private final XStreamFactory factory;

    XStreamPoolQueueImpl(XStreamFactory factory, Queue<XStream> queue) {
        this.factory = factory;
        this.queue = queue;
    }

    public int size () {
        return queue.size();
    }

    public XStream borrow () {
        XStream res;
        if((res = queue.poll()) != null) {
            return res;
        }
        return factory.create();
    }

    public void release (XStream xstream) {
        queue.offer(xstream);
    }

    public <T> T run(XStreamCallback<T> callback) {
        XStream xstream = borrow();
        try {
            return callback.execute(xstream);
        } finally {
            release(xstream);
        }
    }

    public void clear() {
        queue.clear();
    }
}
