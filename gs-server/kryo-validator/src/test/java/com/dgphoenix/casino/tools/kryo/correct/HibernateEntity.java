package com.abs.casino.tools.kryo.correct;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class HibernateEntity implements KryoSerializable {
    @Id
    private int id;

    @Transient
    private String transientField;

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(id);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        id = input.readInt();
    }
}
