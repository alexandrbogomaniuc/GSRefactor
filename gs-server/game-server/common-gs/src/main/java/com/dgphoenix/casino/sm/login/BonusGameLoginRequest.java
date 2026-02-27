package com.abs.casino.sm.login;

/**
 * User: isirbis
 * Date: 07.10.14
 */
public class BonusGameLoginRequest extends GameLoginRequest {
    Long bonusId;

    public Long getBonusId() {
        return bonusId;
    }

    public void setBonusId(Long bonusId) {
        this.bonusId = bonusId;
    }

    @Override
    public String toString() {
        return "BonusGameLoginRequest[" +
                "bonusId=" + bonusId +
                ']' + super.toString();
    }
}
