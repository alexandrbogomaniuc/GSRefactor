import SimpleUIController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameField from '../../../main/GameField';
import GameScreen from '../../../main/GameScreen';
import EnemyIndicatorsInfo from '../../../model/uis/enemies/EnemyIndicatorsInfo';
import { ENEMY_TYPES } from '../../../../../shared/src/CommonConstants';

class EnemyIndicatorsController extends SimpleUIController
{
	static get EVENT_ON_ENERGY_UPDATED () { return 'EVENT_ON_ENERGY_UPDATED' }

	get visibility()
	{
		return this._fVisibility_bln;
	}

	constructor(aInfo_obj, aView_eiv)
	{
		super(new EnemyIndicatorsInfo(aInfo_obj), aView_eiv);
	}

	__init()
	{
		super.__init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		if (!APP.gameScreen.healthBarEnabled)
		{
			this._fVisibility_bln = false;
		}
		else
		{
			this._fVisibility_bln = true;
		}

		APP.currentWindow.gameField.on(GameField.EVENT_ON_ENEMY_HIT_ANIMATION, this._onEnemyHitAnnimation, this);
	}

	_onEnemyHitAnnimation(event)
	{
		
	}

	destroy()
	{
		APP.currentWindow.gameField.off(GameField.EVENT_ON_ENEMY_HIT_ANIMATION, this._onEnemyHitAnnimation, this);

		super.destroy();

		this._fVisibility_bln = null;
	}
}

export default EnemyIndicatorsController;