import SimpleInfo from '../../../../unified/model/base/SimpleInfo';

export const TOURNAMENT_STATES = {
	READY:		"READY",
	ACTIVE:		"STARTED",
	FINISHED:	"FINISHED",
	CANCELLED:	"CANCELLED"
};

class GUSTournamentModeInfo extends SimpleInfo 
{
	constructor() 
	{
		super();

		this._fResetBalanceAfterRebuy_bl = false;
	}

	get isTournamentMode()
	{
		return false;
	}

	get isKeepSWMode()
	{
		return false;
	}
	
	isSupportedTournamentState(aState_str)
	{
		for (let prop in TOURNAMENT_STATES)
		{
			let state = TOURNAMENT_STATES[prop];
			if (state == aState_str)
			{
				return true;
			}
		}
		return false;
	}
}

export default GUSTournamentModeInfo