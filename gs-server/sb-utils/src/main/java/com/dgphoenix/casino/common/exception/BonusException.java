package com.abs.casino.common.exception;

import com.abs.casino.common.web.bonus.BonusError;
import com.abs.casino.common.exception.CommonException;

/**
 * User: ktd
 * Date: 29.03.11
 */
public class BonusException extends CommonException {
    private BonusError bonusError;

    public BonusException(BonusError bonusError) {
        this.bonusError = bonusError;
    }

    public BonusException(String message){
        super(message);
    }

    public BonusException(String message, BonusError bonusError) {
        super(message);
        this.bonusError = bonusError;
    }

    public BonusException(Throwable cause){
        super(cause);
    }

    public BonusException(Throwable cause, BonusError bonusError){
        super(cause);
        this.bonusError = bonusError;
    }

    public BonusException(String message, Throwable cause){
        super(message,cause);
    }

    public BonusException() {
    }

    public BonusError getBonusError() {
        return bonusError;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        return message == null && bonusError != null ? bonusError.getDescription() : message;
    }

    @Override
    public String toString() {
        return "BonusException [" + super.toString() +
                "bonusError=" + bonusError +
                ']';
    }
}
