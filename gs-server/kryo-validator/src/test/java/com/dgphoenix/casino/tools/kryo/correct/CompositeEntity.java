package com.dgphoenix.casino.tools.kryo.correct;

import com.dgphoenix.casino.tools.annotations.Transient;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.ImmutableSet;

import java.sql.Date;
import java.time.ZoneId;
import java.util.*;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 13.10.2015
 */
public class CompositeEntity implements KryoSerializable {

    private int id;
    private SimpleEntity simpleEntity;
    private List<SimpleEntity> entityList;
    private Set<SimpleEntity> entitySet;
    private Map<String, SimpleEntity> entityMap;
    transient private NonSerializableEntity nonSerializableEntity;
    private EntityInterface entity;
    private ZoneId timezone;
    private EnumMap<SimpleEnum, String> enumMap;
    private Deque<SimpleEntity> entityDeque;
    private Locale locale;
    private ImmutableSet<String> entityImmutableSet;
    private Date date;
    @Transient
    private String transientField;

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(id);
        kryo.writeObjectOrNull(output, simpleEntity, SimpleEntity.class);
        kryo.writeClassAndObject(output, entityList);
        kryo.writeClassAndObject(output, entitySet);
        kryo.writeClassAndObject(output, entityMap);
        kryo.writeClassAndObject(output, entity);
        output.writeString(timezone.toString());
        kryo.writeClassAndObject(output, enumMap);
        kryo.writeClassAndObject(output, entityDeque);
        kryo.writeClassAndObject(output, locale);
        kryo.writeClassAndObject(output, entityImmutableSet);
        kryo.writeObjectOrNull(output, date, Date.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Kryo kryo, Input input) {
        id = input.readInt();
        simpleEntity = kryo.readObjectOrNull(input, SimpleEntity.class);
        entityList = (List<SimpleEntity>) kryo.readClassAndObject(input);
        entitySet = (Set<SimpleEntity>) kryo.readClassAndObject(input);
        entityMap = (Map<String, SimpleEntity>) kryo.readClassAndObject(input);
        entity = (EntityInterface) kryo.readClassAndObject(input);
        timezone = ZoneId.of(input.readString());
        enumMap = (EnumMap<SimpleEnum, String>) kryo.readClassAndObject(input);
        entityDeque = (Deque<SimpleEntity>) kryo.readClassAndObject(input);
        locale = (Locale) kryo.readClassAndObject(input);
        entityImmutableSet = (ImmutableSet<String>) kryo.readClassAndObject(input);
        date = kryo.readObjectOrNull(input, Date.class);
    }
}
