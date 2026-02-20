import GUSTournamentModeInfo, { TOURNAMENT_STATES } from './GUSTournamentModeInfo';

class GUSGameTournamentModeInfo extends GUSTournamentModeInfo 
{	
	constructor() 
	{
		super();

		this._fTournamentServerState_str = undefined;
		this._fTournamentClientState_str = undefined;
		this._fIsTournamentMode_bl = false;
	}

	get isTournamentMode()
	{
		return this._fIsTournamentMode_bl;
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
}

export { TOURNAMENT_STATES }
export default GUSGameTournamentModeInfo