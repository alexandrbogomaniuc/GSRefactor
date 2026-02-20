import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import BattlegroundCoinBurstAnimation from './BattlegroundCoinBurstAnimation';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const BURST_DELAYS = [0, 10*2*17]

class BattlegroundCoinBurstsAnimation extends Sprite
{	
	constructor()
	{
		super();

		this._fCoinsBurstAnimations_bcba_arr = [];

		this._addContent();
	}

	adjust(aStartTime_num)
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let lCurGameplayTime_num = l_gpi.gameplayTime;

		if (lCurGameplayTime_num >= aStartTime_num)
		{
			this.visible = true;

			for (let i=0; i<BURST_DELAYS.length; i++)
			{
				let lCurRainStartTime_num = aStartTime_num + BURST_DELAYS[i];
				if (lCurRainStartTime_num >= lCurRainStartTime_num)
				{
					this._fCoinsBurstAnimations_bcba_arr[i].adjust(lCurRainStartTime_num);
				}
			}
		}
		else
		{
			this.drop();
		}
	}

	getTotalDuration()
	{
		let lTotalDuration_num = 0;

		for (let i=0; i<this._fCoinsBurstAnimations_bcba_arr.length; i++)
		{
			let lCurAnimDuration_num = this._fCoinsBurstAnimations_bcba_arr[i].getTotalDuration() + BURST_DELAYS[i];
			if (lCurAnimDuration_num > lTotalDuration_num)
			{
				lTotalDuration_num = lCurAnimDuration_num;
			}
		}

		return lTotalDuration_num;
	}

	drop()
	{
		if (this._fCoinsBurstAnimations_bcba_arr)
		{
			for (let i=0; i<this._fCoinsBurstAnimations_bcba_arr.length; i++)
			{
				this._fCoinsBurstAnimations_bcba_arr[i].drop();
			}
		}

		this.visible = false;
	}

	_addContent()
	{
		for (let i=0; i<BURST_DELAYS.length; i++)
		{
			let l_bcra = this.addChild(new BattlegroundCoinBurstAnimation());

			this._fCoinsBurstAnimations_bcba_arr.push(l_bcra);
		}
	}
}

export default BattlegroundCoinBurstsAnimation