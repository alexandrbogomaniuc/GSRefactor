import SimpleController from '../../../../unified/controller/base/SimpleController';
import GUSGameTournamentModeInfo from '../../../model/custom/tournament/GUSGameTournamentModeInfo';

class GUSGameTournamentModeController extends SimpleController 
{
	static get EVENT_ON_TOURNAMENT_SERVER_STATE_CHANGED() 		{ return 'EVENT_ON_TOURNAMENT_SERVER_STATE_CHANGED'; }
	static get EVENT_ON_TOURNAMENT_CLIENT_STATE_CHANGED() 		{ return 'EVENT_ON_TOURNAMENT_CLIENT_STATE_CHANGED'; }

	constructor(aOptInfo) 
	{
		super(aOptInfo || new GUSGameTournamentModeInfo());
	}

	destroy()
	{
		super.destroy();
	}
}

export default GUSGameTournamentModeController