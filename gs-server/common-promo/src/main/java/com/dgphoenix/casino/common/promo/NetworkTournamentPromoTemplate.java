package com.abs.casino.common.promo;

import com.abs.casino.common.promo.TournamentObjective;
import com.abs.casino.common.promo.TournamentPromoTemplate;
import com.abs.casino.common.promo.TournamentRankQualifier;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * User: flsh
 * Date: 03.12.2020.
 */
public class NetworkTournamentPromoTemplate extends TournamentPromoTemplate {
    private static final byte VERSION = 1;
    private static final Set<TournamentObjective> ALLOWED_OBJECTIVES = Collections.singleton(TournamentObjective.MQ_NETWORK_TOURNAMENT);

    public NetworkTournamentPromoTemplate() {
        super(TournamentObjective.MQ_NETWORK_TOURNAMENT, TournamentRankQualifier.SCORE_ONLY);
    }

    public Set<TournamentObjective> getAllowedObjectives() {
        return ALLOWED_OBJECTIVES;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        super.write(kryo, output);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        super.read(kryo, input);
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        super.serializeObject(gen, serializers);
    }

    @Override
    public NetworkTournamentPromoTemplate deserializeObject(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        super.deserializeObject(p, ctxt);
        return this;
    }
}
