package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.IPlayerProfile;
import com.betsoft.casino.mp.service.IPlayerProfileService;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Collections;
import java.util.Set;

public class StubPlayerProfileService implements IPlayerProfileService {
    @Override
    public IPlayerProfile load(long bankId, long accountId) {
        return new IPlayerProfile() {
            @Override
            public void write(Kryo kryo, Output output) {
            }

            @Override
            public void read(Kryo kryo, Input input) {
            }

            @Override
            public Set<Integer> getBorders() {
                return Collections.EMPTY_SET;
            }

            @Override
            public void setBorders(Set<Integer> borders) {
            }

            @Override
            public Set<Integer> getHeroes() {
                return Collections.EMPTY_SET;
            }

            @Override
            public void setHeroes(Set<Integer> heroes) {

            }

            @Override
            public Set<Integer> getBackgrounds() {
                return Collections.EMPTY_SET;
            }

            @Override
            public void setBackgrounds(Set<Integer> backgrounds) {

            }

            @Override
            public Integer getBorder() {
                return 1;
            }

            @Override
            public void setBorder(Integer border) {

            }

            @Override
            public Integer getHero() {
                return 1;
            }

            @Override
            public void setHero(Integer hero) {

            }

            @Override
            public Integer getBackground() {
                return 1;
            }

            @Override
            public void setBackground(Integer background) {

            }

            @Override
            public boolean isDisableTooltips() {
                return false;
            }

            @Override
            public void setDisableTooltips(boolean disableTooltips) {

            }
        };
    }

    @Override
    public void save(long bankId, long accountId, IPlayerProfile profile) {

    }
}
