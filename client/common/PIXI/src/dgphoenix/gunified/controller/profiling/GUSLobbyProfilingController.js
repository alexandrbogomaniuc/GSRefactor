import ProfilingController from '../../../unified/controller/profiling/ProfilingController';
import GUSLobbyProfilingInfo from '../../model/profiling/GUSLobbyProfilingInfo';

class GUSLobbyProfilingController extends ProfilingController
{
	constructor()
	{
		super(new GUSLobbyProfilingInfo());
	}

	//add your own getters/functions, specific to the lobby level
}

export default GUSLobbyProfilingController;