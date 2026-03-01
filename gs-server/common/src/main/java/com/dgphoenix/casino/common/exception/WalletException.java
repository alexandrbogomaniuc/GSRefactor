package com.abs.casino.common.exception;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.gs.managers.payment.wallet.CWError;

/**
 * User: plastical
 * Date: 02.03.2010
 */
public class WalletException extends CommonException {
    private long accountId;
    private String errorCode;
    private boolean numericErrorCode;
    private CWError walletError;

    public WalletException(String message) {
        super(message);
    }

    public WalletException(String message, long accountId) {
        super(message);
        this.accountId = accountId;
    }

    public WalletException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public WalletException(Throwable cause) {
        super(cause);
    }

    public WalletException(String message, Throwable cause) {
        super(message, cause);
    }

    public WalletException(long accountId, String message, int errorCode, CWError walletError) {
        this(accountId, message, String.valueOf(errorCode), true, walletError);
    }

    public WalletException(long accountId, String message, String errorCode) {
        this(accountId, message, errorCode, false, null);
    }

    public WalletException(long accountId, String message, String errorCode, boolean numericErrorCode) {
        this(accountId, message, errorCode, numericErrorCode, null);
    }

    public WalletException(long accountId, String message, String errorCode, boolean numericErrorCode,
                           CWError walletError) {
        super(message);
        this.accountId = accountId;
        this.errorCode = errorCode;
        this.numericErrorCode = numericErrorCode;
        this.walletError = walletError;
    }

    public WalletException() {
    }

    public long getAccountId() {
        return accountId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isNumericErrorCode() {
        return numericErrorCode;
    }

    public Integer tryToGetNumericErrorCode() {
        Integer code = null;
        if (walletError != null) {
            code = walletError.getCode();
        } else if (getErrorCode() != null) {
            try {
                code = Integer.parseInt(errorCode);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return code;
    }

    public CWError getWalletError() {
        return walletError;
    }

    @Override
    public String toString() {
        return "WalletException[" +
                "accountId=" + accountId +
                ", errorCode='" + errorCode + '\'' +
                ", walletError=" + walletError +
                ", numericErrorCode=" + numericErrorCode +
                ']';
    }
}
