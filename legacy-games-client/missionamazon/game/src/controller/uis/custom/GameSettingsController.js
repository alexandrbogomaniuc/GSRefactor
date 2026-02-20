import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import GameSettingsInfo from '../../../model/uis/custom/GameSettingsInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Game from '../../../Game';

class GameSettingsController extends SimpleController {

	constructor()
	{
		super(new GameSettingsInfo());
	}

	__initControlLevel()
	{
		super.__initControlLevel();
		APP.on(Game.EVENT_ON_GAME_SETTINGS_INITIALIZED, this._onGameSettingsInitialized, this);
	}

	_onGameSettingsInitialized(e)
	{
		this.info.weaponsSavingAllowed = e.weaponsSavingAllowed;
	}
}

export default GameSettingsController;