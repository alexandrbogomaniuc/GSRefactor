package com.dgphoenix.casino.configuration;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 28.10.15.
 */

public class ClusterInfo implements Serializable, KryoSerializable {

    public static final String TYPE_SB = "SB";
    public static final String TYPE_GP3 = "GP3";

    private static final int VERSION = 0;
    private String shutdownCommandFormat;
    private String startCommandFormat;
    private String rebootCommandFormat;
    private boolean production;
    private String name;
    private String endpoint;
    private String transferServiceUrl;
    private String type;
    private List<Node> nodes;
    private transient int implementationVersion;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return "ClusterInfo{" +
                "shutdownCommandFormat='" + shutdownCommandFormat + '\'' +
                ", startCommandFormat='" + startCommandFormat + '\'' +
                ", rebootCommandFormat='" + rebootCommandFormat + '\'' +
                ", production=" + production +
                ", name='" + name + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", transferServiceUrl='" + transferServiceUrl + '\'' +
                ", type='" + type + '\'' +
                ", nodes=" + nodes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClusterInfo that = (ClusterInfo) o;

        if (production != that.production) return false;
        if (shutdownCommandFormat != null ? !shutdownCommandFormat.equals(that.shutdownCommandFormat) : that.shutdownCommandFormat != null)
            return false;
        if (startCommandFormat != null ? !startCommandFormat.equals(that.startCommandFormat) : that.startCommandFormat != null)
            return false;
        if (rebootCommandFormat != null ? !rebootCommandFormat.equals(that.rebootCommandFormat) : that.rebootCommandFormat != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (endpoint != null ? !endpoint.equals(that.endpoint) : that.endpoint != null) return false;
        if (transferServiceUrl != null ? !transferServiceUrl.equals(that.transferServiceUrl) : that.transferServiceUrl != null)
            return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return !(nodes != null ? !nodes.equals(that.nodes) : that.nodes != null);

    }

    @Override
    public int hashCode() {
        int result = shutdownCommandFormat != null ? shutdownCommandFormat.hashCode() : 0;
        result = 31 * result + (startCommandFormat != null ? startCommandFormat.hashCode() : 0);
        result = 31 * result + (rebootCommandFormat != null ? rebootCommandFormat.hashCode() : 0);
        result = 31 * result + (production ? 1 : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (endpoint != null ? endpoint.hashCode() : 0);
        result = 31 * result + (transferServiceUrl != null ? transferServiceUrl.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (nodes != null ? nodes.hashCode() : 0);
        return result;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(VERSION, true);
        output.writeString(startCommandFormat);
        output.writeString(shutdownCommandFormat);
        output.writeString(rebootCommandFormat);
        output.writeBoolean(production);
        output.writeString(name);
        output.writeString(type);
        output.writeString(endpoint);
        output.writeString(transferServiceUrl);
        kryo.writeClassAndObject(output, nodes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input) {
        int ver = input.readInt(true);
        if (ver == 0) {
            startCommandFormat = input.readString();
            shutdownCommandFormat = input.readString();
            rebootCommandFormat = input.readString();
            production = input.readBoolean();
            name = input.readString();
            type = input.readString();
            endpoint = input.readString();
            transferServiceUrl = input.readString();
            Object obj = kryo.readClassAndObject(input);
            if (obj instanceof List) {
                nodes = (List<Node>) obj;
            } else {
                nodes = new ArrayList<Node>();
            }
        } else {
            throw new RuntimeException("Unknown version: " + ver);
        }
    }

    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    public String getShutdownCommandFormat() {
        return shutdownCommandFormat;
    }

    @XmlElement(name = "shutdowncommand")
    public void setShutdownCommandFormat(String shutdownCommandFormat) {
        this.shutdownCommandFormat = shutdownCommandFormat;
    }

    public String getStartCommandFormat() {
        return startCommandFormat;
    }

    @XmlElement(name = "startcommand")
    public void setStartCommandFormat(String startCommandFormat) {
        this.startCommandFormat = startCommandFormat;
    }

    public String getRebootCommandFormat() {
        return rebootCommandFormat;
    }

    @XmlElement(name = "rebootcommand")
    public void setRebootCommandFormat(String rebootCommandFormat) {
        this.rebootCommandFormat = rebootCommandFormat;
    }

    public String getTransferServiceUrl() {
        return transferServiceUrl;
    }

    @XmlElement(name = "transferservice")
    public void setTransferServiceUrl(String transferServiceUrl) {
        this.transferServiceUrl = transferServiceUrl;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    @XmlElement(name = "node")
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getServerIds() {
        List<String> serverIds = new ArrayList<String>();
        for (Node node : nodes) {
            serverIds.add(node.getId());
        }
        return serverIds;
    }

    public int getImplementationVersion() {
        return implementationVersion;
    }

    public void setImplementationVersion(int implementationVersion) {
        this.implementationVersion = implementationVersion;
    }
}
