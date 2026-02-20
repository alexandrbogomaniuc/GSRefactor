import GUSTournamentModeInfo, { TOURNAMENT_STATES } from './GUSTournamentModeInfo';

class GUSLobbyTournamentModeInfo extends GUSTournamentModeInfo 
{
	constructor() 
	{
		super();

		this._fTournamentState_str = undefined;
		this._lastStateUpdateTime_num = undefined;
		this._lastStateUpdateRid_num = undefined;
		this._fRebuyPrice_num = undefined;
		this._fRebuyAllowed_bl = false;
		this._fRebuysCount_num = undefined;
		this._fRebuysLimit_num = undefined;
		this._fRebuyAmount_num = undefined;
	}

	get isTournamentMode()
	{
		return this._fTournamentState_str !== undefined;
	}

	set tournamentState(aState_str)
	{
		if (!this.isSupportedTournamentState(aState_str))
		{
			throw new Error(`Unsupported tournament state provided: ${aState_str}`);
		}

		this._fTournamentState_str = aState_str;
	}

	get tournamentState()
	{
		return this._fTournamentState_str;
	}

	set lastStateUpdateTime(value)
	{
		this._lastStateUpdateTime_num = value;
	}

	get lastStateUpdateTime()
	{
		return this._lastStateUpdateTime_num;
	}

	set lastStateUpdateRID(value)
	{
		this._lastStateUpdateRid_num = value;
	}

	get lastStateUpdateRID()
	{
		return this._lastStateUpdateRid_num;
	}

	set rebuyPrice(value)
	{
		this._fRebuyPrice_num = value;
	}

	get rebuyPrice()
	{
		return this._fRebuyPrice_num;
	}

	get isFreerollMode()
	{
		return this.rebuyPrice === 0;
	}

	set rebuyAllowed(aValue_bl)
	{
		this._fRebuyAllowed_bl = aValue_bl;
	}

	get rebuyAllowed()
	{
		return this._fRebuyAllowed_bl;
	}

	set rebuyCount(value)
	{
		this._fRebuysCount_num = value;
	}

	// current rebuys happened
	get rebuyCount()
	{
		return this._fRebuysCount_num;
	}

	set rebuyLimit(value)
	{
		this._fRebuysLimit_num = value;
	}

	get rebuyLimit()
	{
		return this._fRebuysLimit_num;
	}

	get isRebuyLimitExceeded()
	{
		// rebuyLimit = -1 means that rebuys amount is unlimited
		return this.rebuyLimit != -1 && this.rebuyCount >= this.rebuyLimit;
	}

	set rebuyAmount(value)
	{
		this._fRebuyAmount_num = value;
	}

	get rebuyAmount()
	{
		return this._fRebuyAmount_num;
	}

	get isTournamentReady()
	{
		return this._fTournamentState_str == TOURNAMENT_STATES.READY;
	}

	get isTournamentActive()
	{
		return this._fTournamentState_str == TOURNAMENT_STATES.ACTIVE;
	}

	get isTournamentFinished()
	{
		return this._fTournamentState_str == TOURNAMENT_STATES.FINISHED;
	}

	get isTournamentCancelled()
	{
		return this._fTournamentState_str == TOURNAMENT_STATES.CANCELLED;
	}

	get isTournamentCompletedOrFailedState()
	{
		return this.isTournamentReady || this.isTournamentFinished || this.isTournamentCancelled;
	}
}

export { TOURNAMENT_STATES }
export default GUSLobbyTournamentModeInfo