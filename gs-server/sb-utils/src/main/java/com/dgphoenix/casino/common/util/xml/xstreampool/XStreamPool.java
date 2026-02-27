package com.abs.casino.common.util.xml.xstreampool;

import com.thoughtworks.xstream.XStream;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * User: flsh
 * Date: 06.12.14.
 * Based on com.esotericsoftware.kryo.pool
 */
public interface XStreamPool {
    XStream borrow();

    void release(XStream xstream);

    <T> T run(XStreamCallback<T> callback);

    public static class Builder {
        private final XStreamFactory factory;
        private Queue<XStream> queue = new ConcurrentLinkedQueue<XStream>();
        private boolean softReferences;

        public Builder(XStreamFactory factory) {
            if(factory == null) {
                throw new IllegalArgumentException("factory must not be null");
            }
            this.factory = factory;
        }

        public Builder queue(Queue<XStream> queue) {
            if(queue == null) {
                throw new IllegalArgumentException("queue must not be null");
            }
            this.queue = queue;
            return this;
        }

        public Builder softReferences() {
            softReferences = true;
            return this;
        }

        public XStreamPool build() {
            Queue<XStream> q = softReferences ? new SoftReferenceQueue(queue) : queue;
            return new XStreamPoolQueueImpl(factory, q);
        }

        @Override
        public String toString () {
            return getClass().getName() + "[queue.class=" + queue.getClass() + ", softReferences=" + softReferences + "]";
        }
    }


}
