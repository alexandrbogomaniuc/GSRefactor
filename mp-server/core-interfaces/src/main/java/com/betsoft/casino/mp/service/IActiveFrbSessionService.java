package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IActiveFrbSession;

import java.util.List;

/**
 * User: flsh
 * Date: 22.08.18.
 */
public interface IActiveFrbSessionService {
    List<IActiveFrbSession> getByAccountId(long accountId);

    IActiveFrbSession get(Long id);

    void persist(IActiveFrbSession activeFrbSession);

    void remove(Long frbId);
}
