package com.dgphoenix.casino.controller.frbonus.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.gson.annotations.SerializedName;
import org.hibernate.validator.constraints.NotEmpty;

import java.beans.ConstructorProperties;
import java.io.Serializable;

@JsonRootName("auth")
public class Auth implements Serializable {
    @NotEmpty(message = "missing required parameter signature")
    @SerializedName("signature")
    private String hash;

    @ConstructorProperties({"signature"})
    public Auth(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "Auth{" +
                "hash='" + hash + '\'' +
                '}';
    }
}
