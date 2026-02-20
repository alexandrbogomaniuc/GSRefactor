package com.dgphoenix.casino.common.web;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * User: plastical
 * Date: 24.02.2010
 */
@XStreamAlias("BasicGameServerResponse")
public class BasicGameServerResponse {
    private String status;
    private String description;
    private String bundleMapping;

    public BasicGameServerResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBundleMapping() {
        return bundleMapping;
    }

    public void setBundleMapping(String bundleMapping) {
        this.bundleMapping = bundleMapping;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AbstractGameServerResponse");
        sb.append("{status='").append(status).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", bundleMapping='").append(bundleMapping).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
