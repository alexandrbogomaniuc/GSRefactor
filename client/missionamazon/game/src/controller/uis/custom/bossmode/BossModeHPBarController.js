import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameField from '../../../../main/GameField';

class BossModeHPBarController extends SimpleUIController
{
	updateBoss(aEnemy_e)
	{
		this._updateBoss(aEnemy_e);
	}

	__init()
	{
		super.__init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.currentWindow.gameField.on(GameField.EVENT_ON_ENEMY_HIT_ANIMATION, this._onEnemyHitAnnimation, this);
	}

	_onEnemyHitAnnimation(aEvent_obj)
	{
		let lInfo_bmhpbi = this.info;
		let lId_num = aEvent_obj.enemyId;

		if (lInfo_bmhpbi && lId_num == lInfo_bmhpbi.id)
		{
			lInfo_bmhpbi.currentHealth = aEvent_obj.data.enemy.energy;
			if (lInfo_bmhpbi.currentHealth < 0) lInfo_bmhpbi.currentHealth = 0;
			this._update();
		}
	}

	_updateBoss(aEnemy_e)
	{
		let lInfo_bmhpbi = this.info;

		if (lInfo_bmhpbi)
		{
			lInfo_bmhpbi.fullHealth = aEnemy_e.fullEnergy;
			lInfo_bmhpbi.currentHealth = aEnemy_e.energy;
			lInfo_bmhpbi.name = aEnemy_e.name;
			lInfo_bmhpbi.id = aEnemy_e.id;
		}

		this._update();
	}

	_update()
	{
		let lView_bmhpbv = this.view;

		if (lView_bmhpbv)
		{
			lView_bmhpbv.update();
		}
	}

	destroy()
	{
		if (APP.currentWindow && APP.currentWindow.gameField)
		{
			APP.currentWindow.gameField.off(GameField.EVENT_ON_ENEMY_HIT_ANIMATION, this._onEnemyHitAnnimation, this);
		}

		super.destroy();
	}
}

export default BossModeHPBarController