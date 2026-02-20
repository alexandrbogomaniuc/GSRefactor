import GUSubloadingView from './GUSubloadingView';
import { APP } from '../../../unified/controller/main/globals';

class GUSLobbySubloadingView extends GUSubloadingView
{
	constructor()
	{
		super();
	}

	get __waitScreenContainer()
	{
		return APP.lobbyWaitScreen;
	}

	destroy()
	{
		super.destroy();
	}
}

export default GUSLobbySubloadingView;