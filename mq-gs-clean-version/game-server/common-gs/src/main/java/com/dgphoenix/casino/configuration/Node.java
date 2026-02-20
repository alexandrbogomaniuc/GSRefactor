package com.dgphoenix.casino.configuration;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by nkurtushin on 17.11.15.
 */
@XmlRootElement
public class Node implements Serializable, KryoSerializable {
    private static final int VERSION = 0;
    private String name;
    private String id;
    private String healthCheckUrl;
    private String onlineCheckUrl;
    private String stateChangeCheckPerformingCommand;
    private String isProcessAliveCheckingCommand;
    private SSHConnectionParams connectionParams;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SSHConnectionParams getConnectionParams() {
        return connectionParams;
    }

    @XmlElement(name = "sshconnectionparams")
    public void setConnectionParams(SSHConnectionParams connectionParams) {
        this.connectionParams = connectionParams;
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", healthCheckUrl='" + healthCheckUrl + '\'' +
                ", onlineCheckUrl='" + onlineCheckUrl + '\'' +
                ", stateChangeCheckPerformingCommand='" + stateChangeCheckPerformingCommand + '\'' +
                ", isProcessAliveCheckingCommand='" + isProcessAliveCheckingCommand + '\'' +
                ", connectionParams=" + connectionParams +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (!name.equals(node.name)) return false;
        if (!id.equals(node.id)) return false;
        if (healthCheckUrl != null ? !healthCheckUrl.equals(node.healthCheckUrl) : node.healthCheckUrl != null)
            return false;
        if (onlineCheckUrl != null ? !onlineCheckUrl.equals(node.onlineCheckUrl) : node.onlineCheckUrl != null)
            return false;
        if (stateChangeCheckPerformingCommand != null ? !stateChangeCheckPerformingCommand.equals(
                node.stateChangeCheckPerformingCommand) : node.stateChangeCheckPerformingCommand != null) return false;
        if (isProcessAliveCheckingCommand != null ? !isProcessAliveCheckingCommand.equals(
                node.isProcessAliveCheckingCommand) : node.isProcessAliveCheckingCommand != null) return false;
        return !(connectionParams != null ? !connectionParams.equals(
                node.connectionParams) : node.connectionParams != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (healthCheckUrl != null ? healthCheckUrl.hashCode() : 0);
        result = 31 * result + (onlineCheckUrl != null ? onlineCheckUrl.hashCode() : 0);
        result = 31 * result + (stateChangeCheckPerformingCommand != null ? stateChangeCheckPerformingCommand.hashCode() : 0);
        result = 31 * result + (isProcessAliveCheckingCommand != null ? isProcessAliveCheckingCommand.hashCode() : 0);
        result = 31 * result + (connectionParams != null ? connectionParams.hashCode() : 0);
        return result;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(VERSION, true);
        output.writeString(name);
        output.writeString(id);
        output.writeString(healthCheckUrl);
        output.writeString(onlineCheckUrl);
        output.writeString(stateChangeCheckPerformingCommand);
        output.writeString(isProcessAliveCheckingCommand);
        kryo.writeClassAndObject(output, connectionParams);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int ver = input.readInt(true);
        if (ver == 0) {
            name = input.readString();
            id = input.readString();
            healthCheckUrl = input.readString();
            onlineCheckUrl = input.readString();
            stateChangeCheckPerformingCommand = input.readString();
            isProcessAliveCheckingCommand = input.readString();
            Object obj = kryo.readClassAndObject(input);
            if (obj instanceof SSHConnectionParams) {
                connectionParams = (SSHConnectionParams) obj;
            }
        } else {
            throw new RuntimeException("Unknown version: " + ver);
        }
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    @XmlElement(name = "healthcheckurl")
    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public String getStateChangeCheckPerformingCommand() {
        return stateChangeCheckPerformingCommand;
    }

    @XmlElement(name = "checkstatechange")
    public void setStateChangeCheckPerformingCommand(String stateChangeCheckPerformingCommand) {
        this.stateChangeCheckPerformingCommand = stateChangeCheckPerformingCommand;
    }

    public String getOnlineCheckUrl() {
        return onlineCheckUrl;
    }

    @XmlElement(name = "onlinecheckurl")
    public void setOnlineCheckUrl(String onlineCheckUrl) {
        this.onlineCheckUrl = onlineCheckUrl;
    }

    public String getIsProcessAliveCheckingCommand() {
        return isProcessAliveCheckingCommand;
    }

    @XmlElement(name = "checkisalive")
    public void setIsProcessAliveCheckingCommand(String isProcessAliveCheckingCommand) {
        this.isProcessAliveCheckingCommand = isProcessAliveCheckingCommand;
    }
}
