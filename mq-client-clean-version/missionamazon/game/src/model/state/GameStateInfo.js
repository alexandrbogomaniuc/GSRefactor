import { GAME_ROUND_STATE } from '../../../../shared/src/CommonConstants';

const SUBROUND_STATE = {
	BOSS: 		"BOSS",
	BASE: 		"BASE"
};

const ROUND_STATE = GAME_ROUND_STATE;

class GameStateInfo
{
	//IL CONSTRUCTION...
	constructor()
	{
		this._init();
	}
	//...IL CONSTRUCTION

	//ILI INIT...
	_init()
	{
		this._fGameInProgress_bln = false;
		this._fPlayerSitIn_bln = false;
		this._fGameState_str = undefined;
		this._fSubroundState_str = undefined;
		this._fSubroundLasthand_bl = false;
		this._fFRBMode_bln = false;
	}
	//...ILI INIT
	
	//IL INTERFACE...
	get isGameInProgress()
	{
		return this._fGameInProgress_bln;
	}

	set isGameInProgress(isInProgress_bln)
	{
		this._fGameInProgress_bln = isInProgress_bln;
	}

	get gameState()
	{
		return this._fGameState_str;
	}

	set gameState(state_str)
	{
		this._fGameState_str = state_str;
	}

	get isWaitState()
	{
		return this.gameState === ROUND_STATE.WAIT;
	}

	get isPlayState()
	{
		return this.gameState === ROUND_STATE.PLAY;
	}

	get isQualifyState()
	{
		return this.gameState === ROUND_STATE.QUALIFY;
	}

	get isClosedState()
	{
		return this.gameState === ROUND_STATE.CLOSED;
	}

	get isPlayerSitIn()
	{
		return this._fPlayerSitIn_bln;
	}

	set isPlayerSitIn(isPlayerSitIn_bln)
	{
		this._fPlayerSitIn_bln = isPlayerSitIn_bln;
	}

	set subroundState(aState_str)
	{
		this._fSubroundState_str = aState_str;
	}

	get subroundState()
	{
		return this._fSubroundState_str;
	}

	get isBossSubround()
	{
		return this.subroundState == SUBROUND_STATE.BOSS;
	}

	set subroundLasthand(aValue_bln)
	{
		this._fSubroundLasthand_bl = aValue_bln;
	}

	get subroundLasthand()
	{
		return this._fSubroundLasthand_bl;
	}

	set extraBuyInAvailable(aValue_bln)
	{
		this._fExtraBuyInAvailable_bln = aValue_bln;
	}

	get extraBuyInAvailable()
	{
		return this._fExtraBuyInAvailable_bln;
	}

	set frbMode(aValue_bln)
	{
		this._fFRBMode_bln = aValue_bln;
	}

	get frbMode()
	{
		return this._fFRBMode_bln;
	}

	/*
	*	roundEndTime - time till RoundFinishSoon
	*/
	get roundEndTime()
	{
		return this._fRoundEndTime_int;
	}

	set roundEndTime(aValue_int)
	{
		this._fRoundEndTime_int = aValue_int;
	}

	resetRoundEndTime()
	{
		this._fRoundEndTime_int = undefined;
	}
	//...IL INTERFACE

	//IL IMPLEMENTATION...
	//...IL IMPLEMENTATION

	destroy()
	{
		this._fGameInProgress_bln = undefined;
		this._fPlayerSitIn_bln = undefined;
		this._fGameState_str = undefined;
		this._fSubroundState_str = undefined;
		this._fExtraBuyInAvailable_bln = undefined;
		this._fRoundEndTime_int = undefined;
		
		super.destroy();
	}
}

export {GameStateInfo, SUBROUND_STATE, ROUND_STATE};