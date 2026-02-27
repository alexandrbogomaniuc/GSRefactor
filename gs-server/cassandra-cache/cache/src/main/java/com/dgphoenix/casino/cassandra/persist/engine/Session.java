package com.abs.casino.cassandra.persist.engine;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Map;

/**
 * User: Grien
 * Date: 22.12.2014 13:54
 */
public class Session implements com.datastax.driver.core.Session {
    private String keySpace;
    private com.datastax.driver.core.Session session;

    public Session(String keySpace, com.datastax.driver.core.Session session) {
        this.keySpace = keySpace;
        this.session = session;
    }

    public String getKeySpace() {
        return keySpace;
    }

    @Override
    public String getLoggedKeyspace() {
        return session.getLoggedKeyspace();
    }

    @Override
    public com.datastax.driver.core.Session init() {
        return session.init();
    }

    @Override
    public ListenableFuture<com.datastax.driver.core.Session> initAsync() {
        return session.initAsync();
    }

    @Override
    public com.datastax.driver.core.ResultSet execute(String query) {
        return session.execute(query);
    }

    @Override
    public com.datastax.driver.core.ResultSet execute(String query, Object... values) {
        return session.execute(query, values);
    }

    @Override
    public com.datastax.driver.core.ResultSet execute(String query, Map<String, Object> values) {
        return session.execute(query, values);
    }

    @Override
    public com.datastax.driver.core.ResultSet execute(com.datastax.driver.core.Statement statement) {
        return session.execute(statement);
    }

    @Override
    public com.datastax.driver.core.ResultSetFuture executeAsync(String query) {
        return session.executeAsync(query);
    }

    @Override
    public com.datastax.driver.core.ResultSetFuture executeAsync(String query, Object... values) {
        return session.executeAsync(query, values);
    }

    @Override
    public com.datastax.driver.core.ResultSetFuture executeAsync(String query, Map<String, Object> values) {
        return session.executeAsync(query, values);
    }

    @Override
    public com.datastax.driver.core.ResultSetFuture executeAsync(com.datastax.driver.core.Statement statement) {
        return session.executeAsync(statement);
    }

    @Override
    public com.datastax.driver.core.PreparedStatement prepare(String query) {
        return session.prepare(query);
    }

    @Override
    public com.datastax.driver.core.PreparedStatement prepare(com.datastax.driver.core.RegularStatement statement) {
        return session.prepare(statement);
    }

    @Override
    public ListenableFuture<com.datastax.driver.core.PreparedStatement> prepareAsync(String query) {
        return session.prepareAsync(query);
    }

    @Override
    public ListenableFuture<com.datastax.driver.core.PreparedStatement> prepareAsync(com.datastax.driver.core.RegularStatement statement) {
        return session.prepareAsync(statement);
    }

    @Override
    public com.datastax.driver.core.CloseFuture closeAsync() {
        return session.closeAsync();
    }

    @Override
    public void close() {
        session.close();
    }

    @Override
    public boolean isClosed() {
        return session.isClosed();
    }

    @Override
    public com.datastax.driver.core.Cluster getCluster() {
        return session.getCluster();
    }

    @Override
    public State getState() {
        return session.getState();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Session)) return false;

        Session session = (Session) o;

        if (!keySpace.equals(session.keySpace)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return keySpace.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("com.datastax.driver.core.Session");
        sb.append("[keySpace='").append(keySpace).append('\'');
        sb.append(", session=").append(session);
        sb.append(']');
        return sb.toString();
    }
}
