package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IPlayerProfile;

public interface IPlayerProfileService<PROFILE extends IPlayerProfile> {
    PROFILE load(long bankId, long accountId);

    void save(long bankId, long accountId, PROFILE profile);
}
