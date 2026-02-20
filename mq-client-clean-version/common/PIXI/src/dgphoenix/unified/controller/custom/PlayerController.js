import SimpleController from '../base/SimpleController';
import PlayerInfo from '../../model/custom/PlayerInfo';

/**
 * @class
 * @classdesc Base controller for player data
 */
class PlayerController extends SimpleController
{
	static get EVENT_ON_PLAYER_INFO_UPDATED() 	{return "onPlayerInfoUpdated";}
	//INIT...
	constructor(aOptInfo_ussi)
	{
		super(aOptInfo_ussi ? aOptInfo_ussi : new PlayerInfo());
		
		this._initPlayerController();
	}

	_initPlayerController()
	{

	}
	//...INIT
}

export default PlayerController;