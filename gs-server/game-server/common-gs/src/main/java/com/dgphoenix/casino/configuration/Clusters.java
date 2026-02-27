package com.abs.casino.configuration;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 28.10.15.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Clusters implements Serializable, KryoSerializable {
    private static final int VERSION = 0;

    public static Clusters fromFile(URL configUrl) throws JAXBException {
        String configPath = configUrl == null ? null : configUrl.getPath();
        return fromFile(configPath);
    }

    public static Clusters fromFile(String path) throws JAXBException {
        File configFile;
        if (path == null || !(configFile = new File(path)).exists()) {
            return new Clusters(new ArrayList<ClusterInfo>());
        } else {
            JAXBContext context = JAXBContext.newInstance(Clusters.class);
            return (Clusters) context.createUnmarshaller().unmarshal(configFile);
        }
    }

    @XmlElement(name = "cluster")
    private List<ClusterInfo> clusterInfos;

    public Clusters() {
    }

    public Clusters(List<ClusterInfo> clusterInfos) {
        this.clusterInfos = clusterInfos;
    }

    public List<ClusterInfo> getClusterInfos() {
        return clusterInfos;
    }

    public void setClusterInfos(List<ClusterInfo> clusterInfos) {
        this.clusterInfos = clusterInfos;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ClusterServerInfos", clusterInfos)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Clusters) {
            return ((Clusters) obj).getClusterInfos().equals(clusterInfos);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return clusterInfos.hashCode();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(VERSION, true);
        kryo.writeClassAndObject(output, clusterInfos);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input) {
        int ver = input.readInt(true);
        if (ver == 0) {
            Object obj = kryo.readClassAndObject(input);
            if (obj instanceof List) {
                clusterInfos = (List<ClusterInfo>) obj;
            } else {
                clusterInfos = new ArrayList<>();
            }
        } else {
            throw new RuntimeException("Unknown version: " + ver);
        }
    }
}
