package com.abs.casino.cassandra.persist.engine;

import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.Assignment;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Ordering;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.datastax.driver.core.querybuilder.Using;

import java.util.Map;

public final class Cql {
    private Cql() {}
    public static Select.Builder select(String... columns) { return QueryBuilder.select(columns); }
    public static Select.Builder select(Object... columns) { return QueryBuilder.select(columns); }
    public static Select.Selection select() { return QueryBuilder.select(); }
    public static Insert insertInto(String table) { return QueryBuilder.insertInto(table); }
    public static Insert insertInto(String keyspace, String table) { return QueryBuilder.insertInto(keyspace, table); }
    public static Insert insertInto(TableMetadata table) { return QueryBuilder.insertInto(table); }
    public static Update update(String table) { return QueryBuilder.update(table); }
    public static Update update(String keyspace, String table) { return QueryBuilder.update(keyspace, table); }
    public static Update update(TableMetadata table) { return QueryBuilder.update(table); }
    public static Delete.Builder delete(String... columns) { return QueryBuilder.delete(columns); }
    public static Delete.Selection delete() { return QueryBuilder.delete(); }
    public static Batch batch(RegularStatement... statements) { return QueryBuilder.batch(statements); }
    public static Batch unloggedBatch(RegularStatement... statements) { return QueryBuilder.unloggedBatch(statements); }
    public static Clause eq(String name, Object value) { return QueryBuilder.eq(name, value); }
    public static Clause eq(Iterable<String> names, Iterable<?> values) { return QueryBuilder.eq(names, values); }
    public static Clause in(String name, Object... values) { return QueryBuilder.in(name, values); }
    public static Clause in(String name, Iterable<?> values) { return QueryBuilder.in(name, values); }
    public static Clause in(Iterable<String> names, Iterable<?> values) { return QueryBuilder.in(names, values); }
    public static Clause gte(String name, Object value) { return QueryBuilder.gte(name, value); }
    public static Clause gte(Iterable<String> names, Iterable<?> values) { return QueryBuilder.gte(names, values); }
    public static Clause lte(String name, Object value) { return QueryBuilder.lte(name, value); }
    public static Clause lte(Iterable<String> names, Iterable<?> values) { return QueryBuilder.lte(names, values); }
    public static Clause lt(String name, Object value) { return QueryBuilder.lt(name, value); }
    public static Clause lt(Iterable<String> names, Iterable<?> values) { return QueryBuilder.lt(names, values); }
    public static Ordering asc(String name) { return QueryBuilder.asc(name); }
    public static Ordering desc(String name) { return QueryBuilder.desc(name); }
    public static Using timestamp(long value) { return QueryBuilder.timestamp(value); }
    public static Using ttl(int value) { return QueryBuilder.ttl(value); }
    public static Assignment set(String name, Object value) { return QueryBuilder.set(name, value); }
    public static Assignment set(Object name, Object value) { return QueryBuilder.set(name, value); }
    public static Assignment add(String name, Object value) { return QueryBuilder.add(name, value); }
    public static Assignment put(String name, Object key, Object value) { return QueryBuilder.put(name, key, value); }
    public static Assignment putAll(String name, Map<?, ?> value) { return QueryBuilder.putAll(name, value); }
    public static Assignment incr(String name) { return QueryBuilder.incr(name); }
    public static Assignment incr(String name, long value) { return QueryBuilder.incr(name, value); }
}
