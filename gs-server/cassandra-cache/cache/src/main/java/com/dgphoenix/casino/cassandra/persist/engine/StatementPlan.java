package com.abs.casino.cassandra.persist.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class StatementPlan {
    private final Map<Session, List<Statement>> statementsBySession = new HashMap<>();

    public List<Statement> getOrCreateStatements(Session session) {
        return statementsBySession.computeIfAbsent(session, ignored -> new LinkedList<>());
    }

    public Map<Session, List<Statement>> getStatementsBySession() {
        return statementsBySession;
    }

    public boolean isEmpty() {
        return statementsBySession.isEmpty();
    }
}
