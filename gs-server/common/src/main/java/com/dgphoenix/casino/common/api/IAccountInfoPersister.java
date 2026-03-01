package com.abs.casino.common.api;

import com.abs.casino.common.cache.data.account.AccountInfo;

/**
 * User: flsh
 * Date: 28.05.13
 */
public interface IAccountInfoPersister {
    void persist(AccountInfo account);

    AccountInfo getById(long id);

    AccountInfo getByCompositeKey(long bankId, String externalId);
}
