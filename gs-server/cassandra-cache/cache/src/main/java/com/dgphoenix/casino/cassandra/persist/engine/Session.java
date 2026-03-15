package com.abs.casino.cassandra.persist.engine;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Map;

/**
 * User: Grien
 * Date: 22.12.2014 13:54
 */
public class Session implements AutoCloseable {
    private String keySpace;
    private com.datastax.driver.core.Session session;

    public Session(String keySpace, com.datastax.driver.core.Session session) {
        this.keySpace = keySpace;
        this.session = session;
    }

    public String getKeySpace() {
        return keySpace;
    }

    public String getLoggedKeyspace() {
        return session.getLoggedKeyspace();
    }

    public com.datastax.driver.core.Session init() {
        return session.init();
    }

    public ListenableFuture<com.datastax.driver.core.Session> initAsync() {
        return session.initAsync();
    }

    public ResultSet execute(String query) {
        return ResultSet.wrap(session.execute(query));
    }

    public ResultSet executeWrapped(String query) {
        return execute(query);
    }

    public ResultSet execute(String query, Object... values) {
        return ResultSet.wrap(session.execute(query, values));
    }

    public ResultSet executeWrapped(String query, Object... values) {
        return execute(query, values);
    }

    public ResultSet execute(String query, Map<String, Object> values) {
        return ResultSet.wrap(session.execute(query, values));
    }

    public ResultSet execute(com.datastax.driver.core.Statement statement) {
        return ResultSet.wrap(session.execute(statement));
    }

    public ResultSet execute(Statement statement) {
        return execute(statement.unwrap());
    }

    public ResultSet executeWrapped(com.datastax.driver.core.Statement statement) {
        return execute(statement);
    }

    public ResultSet executeWrapped(Statement statement) {
        return execute(statement);
    }

    public ResultSetFuture executeAsync(String query) {
        return ResultSetFuture.wrap(session.executeAsync(query));
    }

    public ResultSetFuture executeAsync(String query, Object... values) {
        return ResultSetFuture.wrap(session.executeAsync(query, values));
    }

    public ResultSetFuture executeAsync(String query, Map<String, Object> values) {
        return ResultSetFuture.wrap(session.executeAsync(query, values));
    }

    public ResultSetFuture executeAsync(com.datastax.driver.core.Statement statement) {
        return ResultSetFuture.wrap(session.executeAsync(statement));
    }

    public ResultSetFuture executeAsync(Statement statement) {
        return ResultSetFuture.wrap(session.executeAsync(statement.unwrap()));
    }

    public PreparedStatement prepare(String query) {
        return PreparedStatement.wrap(session.prepare(query));
    }

    public PreparedStatement prepare(com.datastax.driver.core.RegularStatement statement) {
        return PreparedStatement.wrap(session.prepare(statement));
    }

    public ListenableFuture<com.datastax.driver.core.PreparedStatement> prepareAsync(String query) {
        return session.prepareAsync(query);
    }

    public ListenableFuture<com.datastax.driver.core.PreparedStatement> prepareAsync(com.datastax.driver.core.RegularStatement statement) {
        return session.prepareAsync(statement);
    }

    public CloseFuture closeAsync() {
        return CloseFuture.wrap(session.closeAsync());
    }

    public void close() {
        session.close();
    }

    public boolean isClosed() {
        return session.isClosed();
    }

    public com.datastax.driver.core.Cluster getCluster() {
        return session.getCluster();
    }

    public com.datastax.driver.core.Session.State getState() {
        return session.getState();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Session)) return false;

        Session session = (Session) o;

        if (!keySpace.equals(session.keySpace)) return false;

        return true;
    }

    public int hashCode() {
        return keySpace.hashCode();
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Session");
        sb.append("[keySpace='").append(keySpace).append('\'');
        sb.append(", session=").append(session);
        sb.append(']');
        return sb.toString();
    }
}
