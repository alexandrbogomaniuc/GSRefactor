package com.abs.casino.configuration;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Created by nkurtushin on 15.12.15.
 */
@XmlRootElement
public class GameServerDetails {
    private String clusterName;
    private String serverId;
    private String label;
    private boolean online;
    private String host;
    private Date updateTime;
    private boolean statusChanging;
    private boolean empty;
    private Integer pid;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = new Date(updateTime);
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameServerDetails details = (GameServerDetails) o;

        if (online != details.online) return false;
        if (statusChanging != details.statusChanging) return false;
        if (empty != details.empty) return false;
        if (clusterName != null ? !clusterName.equals(details.clusterName) : details.clusterName != null) return false;
        if (serverId != null ? !serverId.equals(details.serverId) : details.serverId != null) return false;
        if (label != null ? !label.equals(details.label) : details.label != null) return false;
        if (host != null ? !host.equals(details.host) : details.host != null) return false;
        if (updateTime != null ? !updateTime.equals(details.updateTime) : details.updateTime != null) return false;
        return !(pid != null ? !pid.equals(details.pid) : details.pid != null);

    }

    @Override
    public int hashCode() {
        int result = clusterName != null ? clusterName.hashCode() : 0;
        result = 31 * result + (serverId != null ? serverId.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (online ? 1 : 0);
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        result = 31 * result + (statusChanging ? 1 : 0);
        result = 31 * result + (empty ? 1 : 0);
        result = 31 * result + (pid != null ? pid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GameServerDetails{" +
                "clusterName='" + clusterName + '\'' +
                ", serverId='" + serverId + '\'' +
                ", label='" + label + '\'' +
                ", online=" + online +
                ", host='" + host + '\'' +
                ", updateTime=" + updateTime +
                ", statusChanging=" + statusChanging +
                ", empty=" + empty +
                ", pid=" + pid +
                '}';
    }

    public boolean isEmpty() {
        return host == null || label == null;
    }

    public boolean isStatusChanging() {
        return statusChanging;
    }

    public void setStatusChanging(boolean statusChanging) {
        this.statusChanging = statusChanging;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }
}
