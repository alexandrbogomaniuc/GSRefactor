package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;

/**
 * User: flsh
 * Date: 15.12.2022.
 */
public class AddWinRequest implements IAddWinRequest {
    private final String sessionId;
    private final long gameSessionId;
    private final long winAmount;
    private final long returnedBet;
    private final long accountId;
    private final IPlayerBet playerBet;
    private final IBattlegroundRoundInfo bgRoundInfo;
    private final long gsRoundId;
    private final boolean isSitOut;

    /**
     * @param sessionId session id
     * @param gameSessionId game session id
     * @param winAmount calculated win amount
     * @param returnedBet returned bet
     * @param accountId account id
     * @param playerBet IPlayerBet contains information about bet
     * @param bgRoundInfo round info for bg games
     * @param gsRoundId externalRoundId from gs
     * @param isSitOut if is true after processing win, player will be sit out from room.
     */
    public AddWinRequest(String sessionId, long gameSessionId, long winAmount, long returnedBet, long accountId, IPlayerBet playerBet,
                         IBattlegroundRoundInfo bgRoundInfo, long gsRoundId, boolean isSitOut) {
        this.sessionId = sessionId;
        this.gameSessionId = gameSessionId;
        this.winAmount = winAmount;
        this.returnedBet = returnedBet;
        this.accountId = accountId;
        this.playerBet = playerBet;
        this.bgRoundInfo = bgRoundInfo;
        this.gsRoundId = gsRoundId;
        this.isSitOut = isSitOut;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public long getGameSessionId() {
        return gameSessionId;
    }

    @Override
    public long getWinAmount() {
        return winAmount;
    }

    @Override
    public long getReturnedBet() {
        return returnedBet;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public IPlayerBet getPlayerBet() {
        return playerBet;
    }

    @Override
    public IBattlegroundRoundInfo getBgRoundInfo() {
        return bgRoundInfo;
    }

    @Override
    public long getGsRoundId() {
        return gsRoundId;
    }

    @Override
    public boolean isSitOut() {
        return this.isSitOut;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddWinRequest [");
        sb.append("sessionId='").append(sessionId).append('\'');
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", winAmount=").append(winAmount);
        sb.append(", returnedBet=").append(returnedBet);
        sb.append(", accountId=").append(accountId);
        sb.append(", playerBet=").append(playerBet);
        sb.append(", bgRoundInfo=").append(bgRoundInfo);
        sb.append(", gsRoundId=").append(gsRoundId);
        sb.append(", isSitOut=").append(isSitOut);
        sb.append(']');
        return sb.toString();
    }
}
