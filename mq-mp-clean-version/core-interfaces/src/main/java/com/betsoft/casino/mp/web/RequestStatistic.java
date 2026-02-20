package com.betsoft.casino.mp.web;

/**
 * User: flsh
 * Date: 05.10.18.
 */
public class RequestStatistic {
    private Class requestClass;
    private volatile long lastRequestId;
    private volatile long serverClientDelta;
    private volatile long lastServerInputDate;
    private volatile long lastClientSendDate;
    private volatile int requestsCount;

    public RequestStatistic(Class requestClass, long lastRequestId, long serverClientDelta,
                            long lastServerInputDate, long lastClientSendDate) {
        this.requestClass = requestClass;
        this.lastRequestId = lastRequestId;
        this.serverClientDelta = serverClientDelta;
        this.lastServerInputDate = lastServerInputDate;
        this.lastClientSendDate = lastClientSendDate;
        this.requestsCount = 1;
    }

    public synchronized void doNext(long lastRequestId, long lastServerInputDate, long lastClientSendDate) {
        this.requestsCount++;
        this.lastRequestId = lastRequestId;
        if(lastServerInputDate > this.lastServerInputDate) {
            this.lastServerInputDate = lastServerInputDate;
        }
        if(lastClientSendDate > this.lastClientSendDate) {
            this.lastClientSendDate = lastClientSendDate;
        }
    }

    public Class getRequestClass() {
        return requestClass;
    }

    public void setRequestClass(Class requestClass) {
        this.requestClass = requestClass;
    }

    public long getLastRequestId() {
        return lastRequestId;
    }

    public void setLastRequestId(long lastRequestId) {
        this.lastRequestId = lastRequestId;
    }

    public long getServerClientDelta() {
        return serverClientDelta;
    }

    public void setServerClientDelta(long serverClientDelta) {
        this.serverClientDelta = serverClientDelta;
    }

    public long getLastServerInputDate() {
        return lastServerInputDate;
    }

    public void setLastServerInputDate(long lastServerInputDate) {
        this.lastServerInputDate = lastServerInputDate;
    }

    public long getLastClientSendDate() {
        return lastClientSendDate;
    }

    public void setLastClientSendDate(long lastClientSendDate) {
        this.lastClientSendDate = lastClientSendDate;
    }

    public int getRequestsCount() {
        return requestsCount;
    }

    public void setRequestsCount(int requestsCount) {
        this.requestsCount = requestsCount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RequestStatistics [");
        sb.append("requestClass=").append(requestClass);
        sb.append(", lastRequestId=").append(lastRequestId);
        sb.append(", serverClientDelta=").append(serverClientDelta);
        sb.append(", lastServerInputDate=").append(lastServerInputDate);
        sb.append(", lastClientSendDate=").append(lastClientSendDate);
        sb.append(", requestsCount=").append(requestsCount);
        sb.append(']');
        return sb.toString();
    }
}
