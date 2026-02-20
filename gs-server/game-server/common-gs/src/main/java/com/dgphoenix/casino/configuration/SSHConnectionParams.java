package com.dgphoenix.casino.configuration;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by nick on 28.10.15.
 */
@XmlRootElement
public class SSHConnectionParams implements Serializable, KryoSerializable {
    private static final int VERSION = 0;
    private String username;
    private String ipAddress;
    private int port;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("username", username)
                .append("password", password)
                .append("ipAddress", ipAddress)
                .append("port", port)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SSHConnectionParams) {
            return ((SSHConnectionParams) obj).getUsername().equals(username) &&
                    ((SSHConnectionParams) obj).getPassword().equals(password) &&
                    ((SSHConnectionParams) obj).getPort() == port &&
                    ((SSHConnectionParams) obj).getIpAddress().equals(ipAddress);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(username)
                .append(port).append(password).append(ipAddress).toHashCode();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(VERSION, true);
        output.writeString(username);
        output.writeString(ipAddress);
        output.writeInt(port, true);
        output.writeString(password);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int ver = input.readInt(true);
        if (ver == 0) {
            username = input.readString();
            ipAddress = input.readString();
            port = input.readInt(true);
            password = input.readString();
        } else {
            throw new RuntimeException("Unknown version: " + ver);
        }
    }

    public String getIpAddress() {
        return ipAddress;
    }

    @XmlElement(name = "ip")
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
