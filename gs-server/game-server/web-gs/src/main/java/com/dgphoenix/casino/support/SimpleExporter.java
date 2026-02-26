package com.abs.casino.support;

import com.thoughtworks.xstream.XStream;

import java.io.*;
import java.util.Collection;

/**
 * User: Grien
 * Date: 05.09.2012 17:33
 */
public class SimpleExporter {
    private ObjectOutputStream outStream = null;

    private SimpleExporter() {
    }

    private void init(File file) throws IOException {
        try {
            XStream xStream = getXStream();
            outStream = xStream.createObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file, false), 256 * 1024));
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public static SimpleExporter create(File f) throws IOException {
        SimpleExporter exporter = new SimpleExporter();
        exporter.init(f);
        return exporter;
    }

    public static SimpleExporter create(String file) throws IOException {
        return create(new File(file));
    }

    public void write(Serializable serializable) throws IOException {
        outStream.writeObject(serializable);
    }

    public void write(Collection<? extends Serializable> items) throws IOException {
        for (Serializable object : items) {
            outStream.writeObject(object);
        }
    }

    public void close() {
        if (outStream != null) {
            try {
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
            }
        }
    }


    protected XStream getXStream() {
        XStream xStream = new XStream();
        xStream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);
        xStream.autodetectAnnotations(true);
        return xStream;
    }
}
