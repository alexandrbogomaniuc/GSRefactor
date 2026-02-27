package com.abs.casino.common.util.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.CustomObjectOutputStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.StatefulWriter;

import java.io.IOException;
import java.io.NotActiveException;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * User: flsh
 * Date: 7/11/11
 */
public class XStreamWrapper extends XStream {
    public ObjectOutputStream createObjectOutputStream(
            final HierarchicalStreamWriter writer, String rootNodeName) throws IOException {
        final StatefulWriter statefulWriter = new StatefulWriter(writer);
        //statefulWriter.startNode(rootNodeName, null);
        return new CustomObjectOutputStream(new CustomObjectOutputStream.StreamCallback() {
            public void writeToStream(Object object) {
                marshal(object, statefulWriter);
            }

            public void writeFieldsToStream(Map fields) throws NotActiveException {
                throw new NotActiveException("not in call to writeObject");
            }

            public void defaultWriteObject() throws NotActiveException {
                throw new NotActiveException("not in call to writeObject");
            }

            public void flush() {
                statefulWriter.flush();
            }

            public void close() {
                if (statefulWriter.state() != StatefulWriter.STATE_CLOSED) {
                    //statefulWriter.endNode();
                    statefulWriter.close();
                }
            }
        });
    }

}
