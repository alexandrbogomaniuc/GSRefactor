package com.betsoft.casino.mp.model.gameconfig;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


public class PaysDeserializer implements JsonDeserializer<Pays> {

    @Override
    public Pays deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Pays res;
        try{
            int num = Integer.parseInt(json.getAsString());
            res =  new WinPays(num);
        } catch (Exception e) {
            JsonObject asJsonObject = json.getAsJsonObject();
            Map<String, Map<String, String>> data = context.deserialize(asJsonObject, HashMap.class);
            res = new WeaponPays(data);
        }
        return res;
    }
}
