package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.IActiveCashBonusSession;

import java.util.List;

/**
 * User: flsh
 * Date: 15.07.2020.
 */
public interface IActiveCashBonusSessionService<SESSION extends IActiveCashBonusSession> {
    List<SESSION> getByAccountId(long accountId);

    SESSION get(Long id);

    void persist(SESSION activeSession);

    void remove(Long id);
}
