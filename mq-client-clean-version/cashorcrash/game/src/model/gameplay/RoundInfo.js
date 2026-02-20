import SimpleInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';

export const ROUND_STATES = 
{
	WAIT: 			"WAIT",
	QUALIFY: 		"QUALIFY",
	PLAY: 			"PLAY",
	BUY_IN:			"BUY_IN",
	PAUSE: 			"PAUSE"
}
export const MAX_COUNT_INACTIVE_ROUNDS = 3;

class RoundInfo extends SimpleInfo
{
	constructor(aParentInfo_usi)
	{
		super(undefined, aParentInfo_usi);

		this._fRoundState_str = undefined;
		this._fRoundStartTime_num = undefined;
		this._fBuyInStateStartTime_num = undefined;
		this._fPauseStateStartTime_num = undefined;
		this._fRoundEndTime_num = undefined;
		this._fRoundId_int = undefined;
		this._fRoundResultRecieved_bl = undefined;
		this._fRefundValue_num = undefined;
		this._fBattlegroundRoundWinValue_num = undefined;
		this._fBattlegroundRoundWinners_str_arr = null;
		this._fBattlegroundRoundEndTime_num = undefined;
		this._lastSecondsStarted = undefined;
	}

	set roundState(value)
	{
		this._fRoundState_str = value;
	}

	get roundState()
	{
		return this._fRoundState_str;
	}

	get isRoundStateDefined()
	{
		return this._fRoundState_str !== undefined;
	}

	get isRoundPlayState()
	{
		return this.roundState == ROUND_STATES.PLAY;
	}

	get isRoundPlayActive()
	{
		return this.isRoundPlayState && !this.isRoundEndTimeDefined
	}

	get isRoundQualifyState()
	{
		return this.roundState == ROUND_STATES.QUALIFY;
	}

	get isRoundWaitState()
	{
		return this.roundState == ROUND_STATES.WAIT;
	}

	get isRoundBuyInState()
	{
		return this.roundState == ROUND_STATES.BUY_IN;
	}

	get isRoundPauseState()
	{
		return this.roundState == ROUND_STATES.PAUSE;
	}

	get isRoundStateDefined()
	{
		return this.roundState !== undefined;
	}

	set roundStartTime(value)
	{
		this._fRoundStartTime_num = value;
	}

	set lastSecondsStarted(value)
	{
		this._lastSecondsStarted = value;
	}

	get lastSecondsStarted(){
		return this._lastSecondsStarted;
	}

	get roundStartTime()
	{
		return this._fRoundStartTime_num;
	}

	get isRoundStartTimeDefined()
	{
		return this.roundStartTime !== undefined;
	}

	set roundEndTime(value)
	{
		this._fRoundEndTime_num = value;
	}

	get roundEndTime()
	{
		return this._fRoundEndTime_num;
	}

	get isRoundEndTimeDefined()
	{
		return this.roundEndTime !== undefined;
	}

	set isRoundResultRecieved(value)
	{
		this._fRoundResultRecieved_bl = value;
	}

	get isRoundResultRecieved()
	{
		return this._fRoundResultRecieved_bl;
	}

	resetRoundEndTime()
	{
		this._fRoundEndTime_num = undefined;
	}

	resetRoundResultsRecieved()
	{
		this._fRoundResultRecieved_bl = undefined;
	}

	set buyInStateStartTime(value)
	{
		this._fBuyInStateStartTime_num = value;
	}

	get buyInStateStartTime()
	{
		return this._fBuyInStateStartTime_num;
	}

	get isBuyInStateStartTimeDefined()
	{
		return this._fBuyInStateStartTime_num !== undefined;
	}

	set pauseStateStartTime(value)
	{
		this._fPauseStateStartTime_num = value;
	}

	get pauseStateStartTime()
	{
		return this._fPauseStateStartTime_num;
	}

	get isPauseStateStartTimeDefined()
	{
		return this._fPauseStateStartTime_num !== undefined;
	}

	set roundId(value)
	{
		this._fRoundId_int = value;
	}

	get roundId()
	{
		return this._fRoundId_int;
	}

	get isRoundIdDefined()
	{
		return this.roundId !== undefined;
	}

	//BATTLEGROUND...
	get refundValue()
	{
		return this._fRefundValue_num;
	}

	set refundValue(value)
	{
		this._fRefundValue_num = value;
	}

	get battlegroundRoundWinValue()
	{
		return this._fBattlegroundRoundWinValue_num;
	}

	set battlegroundRoundWinValue(value)
	{
		this._fBattlegroundRoundWinValue_num = value;
	}

	get battlegroundRoundWinners()
	{
		return this._fBattlegroundRoundWinners_str_arr;
	}

	set battlegroundRoundWinners(value)
	{
		this._fBattlegroundRoundWinners_str_arr = value;
	}

	get battlegroundRoundEndTime()
	{
		return this._fBattlegroundRoundEndTime_num;
	}

	set battlegroundRoundEndTime(value)
	{
		this._fBattlegroundRoundEndTime_num = value;
	}

	get isBattlegroundRoundEndTimeDefined()
	{
		return this._fBattlegroundRoundEndTime_num !== undefined;
	}

	resetBattlegroundRoundResults()
	{
		this._fRefundValue_num = undefined;
		this._fBattlegroundRoundWinValue_num = undefined;
		this._fBattlegroundRoundWinners_str_arr = null;
		this._fBattlegroundRoundEndTime_num = undefined;
	}

	get isNoOneEjected()
	{
		return !(this.battlegroundRoundWinValue > 0);
	}

	get hasActualPreviousRoundResults()
	{
		return this.refundValue !== undefined || this.battlegroundRoundWinValue !== undefined;
	}
	//...BATTLEGROUND

	reset()
	{
		this._fRoundState_str = undefined;
		this._fRoundStartTime_num = undefined;
		this._fBuyInStateStartTime_num = undefined;
		this._fPauseStateStartTime_num = undefined;
		this._fRoundEndTime_num = undefined;
		this._fRoundId_int = undefined;
		this._fRoundResultRecieved_bl = undefined;
		this._fRefundValue_num = undefined;
		this._fBattlegroundRoundWinValue_num = undefined;
		this._fBattlegroundRoundWinners_str_arr = null;
		this._fBattlegroundRoundEndTime_num = undefined;
	}

	destroy()
	{
		this._fRoundState_str = undefined;
		this._fRoundStartTime_num = undefined;
		this._fBuyInStateStartTime_num = undefined;
		this._fPauseStateStartTime_num = undefined;
		this._fRoundEndTime_num = undefined;
		this._fRoundId_int = undefined;
		this._fRoundResultRecieved_bl = undefined;
		this._fRefundValue_num = undefined;
		this._fBattlegroundRoundWinValue_num = undefined;
		this._fBattlegroundRoundWinners_str_arr = null;
		this._fBattlegroundRoundEndTime_num = undefined;

		super.destroy();
	}
}
export default RoundInfo;