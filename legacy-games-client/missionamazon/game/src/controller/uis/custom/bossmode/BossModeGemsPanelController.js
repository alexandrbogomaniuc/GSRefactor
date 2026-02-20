import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../../../interaction/server/GameWebSocketInteractionController';
import GameField from '../../../../main/GameField';
import Game from '../../../../Game';
import BossGemsAwardingController from '../../../../controller/uis/awarding/BossGemsAwardingController';

class BossModeGemsPanelController extends SimpleUIController
{
	static get EVENT_BOSS_GEMS_PAYOUTS_REQUEST()		{return "EVENT_BOSS_GEMS_PAYOUTS_REQUEST";}

	updateBoss(aEnemy_e)
	{
		this._updateBoss(aEnemy_e);
	}

	updateGemsCount(aGems_obj)
	{
		this._updateGemsCount(aGems_obj);
	}

	getPayoutByGemId(aGemId_num)
	{
		return this._getPayoutByGemId(aGemId_num);
	}

	__init()
	{
		super.__init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.on(Game.EVENT_ON_BOSS_GEMS_PAYOUTS_RESPONSE, this._updateGemsPayout, this);
		APP.gameScreen.gameField.bossGemsAwardingController.on(BossGemsAwardingController.EVENT_ON_GEM_ACHIEVED_PANEL, this._onBossGemAwarding, this);
	}

	_onBossGemAwarding(event)
	{
		let lInfo_bmgpi = this.info;

		if (lInfo_bmgpi)
		{
			lInfo_bmgpi.currentGemsCount[event.data.gemId] += 1;
		}

		let lView_bmgpv = this.view;

		if (lView_bmgpv)
		{
			lView_bmgpv.updateGemsCounters();
			lView_bmgpv.updateTotalWin();
		}
	}

	_updateBoss(aEnemy_e)
	{
		let lInfo_bmgpi = this.info;

		if (lInfo_bmgpi)
		{
			lInfo_bmgpi.skin = aEnemy_e.skin;
		}

		this.emit(BossModeGemsPanelController.EVENT_BOSS_GEMS_PAYOUTS_REQUEST);
	}

	_updateGemsPayout(event)
	{
		let lInfo_bmgpi = this.info;

		if (lInfo_bmgpi)
		{
			lInfo_bmgpi.gemsPayout = event.data;
		}

		let lView_bmgpv = this.view;

		if (lView_bmgpv)
		{
			lView_bmgpv.updatePayouts();
			lView_bmgpv.updateGemsCounters(true);
			lView_bmgpv.updateTotalWin(true);
		}
	}

	_getPayoutByGemId(aGemId_num)
	{
		let lInfo_bmgpi = this.info;
		let lPayout_num = 0

		if (lInfo_bmgpi)
		{
			lPayout_num = lInfo_bmgpi.getPayoutByGemId(aGemId_num);
		}

		return lPayout_num;
	}

	_updateGemsCount(aGems_obj)
	{
		let lInfo_bmgpi = this.info;

		if (lInfo_bmgpi)
		{
			for(let i = 0; i < lInfo_bmgpi.currentGemsCount.length; i++)
			{
				if(aGems_obj[i])
				{
					lInfo_bmgpi.currentGemsCount[i] = aGems_obj[i];
				}	
			}
		}
	}

	destroy()
	{
		super.destroy();
	}
}

export default BossModeGemsPanelController