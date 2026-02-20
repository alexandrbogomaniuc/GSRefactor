import SimpleInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';
import { TOURNAMENT_STATES } from '../../../../../shared/src/CommonConstants';

class TournamentModeInfo extends SimpleInfo 
{	
	constructor() 
	{
		super();

		this._fTournamentServerState_str = undefined;
		this._fTournamentClientState_str = undefined;
		this._fIsTournamentMode_bl = false;
		this._fResetBalanceAfterRebuy_bl = false;
	}

	get isTournamentMode()
	{
		return this._fIsTournamentMode_bl;
	}

	get isKeepSWMode()
	{
		return false;
	}

	set isTournamentMode(value)
	{
		this._fIsTournamentMode_bl = !!value;
	}

	set tournamentServerState(aState_str)
	{
		if (!this.isSupportedTournamentState(aState_str))
		{
			throw new Error (`Unsupported tournament state provided: ${aState_str}`);
			return;
		}

		this._fTournamentServerState_str = aState_str;
	}

	get tournamentServerState()
	{
		return this._fTournamentServerState_str;
	}

	set tournamentClientState(aState_str)
	{
		if (!this.isSupportedTournamentState(aState_str))
		{
			throw new Error (`Unsupported tournament state provided: ${aState_str}`);
			return;
		}

		this._fTournamentClientState_str = aState_str;
	}

	get tournamentClientState()
	{
		return this._fTournamentClientState_str;
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

	get isTournamentOnServerActive()
	{
		return this._fTournamentServerState_str == TOURNAMENT_STATES.ACTIVE;
	}

	get isTournamentOnServerFinished()
	{
		return this._fTournamentServerState_str == TOURNAMENT_STATES.FINISHED;
	}

	get isTournamentOnServerCancelled()
	{
		return this._fTournamentServerState_str == TOURNAMENT_STATES.CANCELLED;
	}

	get isTournamentOnServerCompletedState()
	{
		return this.isTournamentOnServerFinished || this.isTournamentOnServerCancelled;
	}

	get isTournamentOnClientCompletedState()
	{
		return this._fTournamentClientState_str == TOURNAMENT_STATES.FINISHED || this._fTournamentClientState_str == TOURNAMENT_STATES.CANCELLED;
	}	

	set resetBalanceAfterRebuy(aValue_bl)
	{
		this._fResetBalanceAfterRebuy_bl = aValue_bl;
	}

	get resetBalanceAfterRebuy()
	{
		return this._fResetBalanceAfterRebuy_bl;
	}
}

export default TournamentModeInfo