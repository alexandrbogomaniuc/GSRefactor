package com.abs.casino.common.cache;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;

public interface IAccountManager {
    AccountInfo getByAccountId(long accountId) throws CommonException;

    Pair<Integer, String> getBankIdExternalIdByAccountId(long accountId) throws CommonException;
}
