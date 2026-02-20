import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import BattlegroundConfettiAnimation from './BattlegroundConfettiAnimation';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const CONFETTI_PARAMS = [ {delay: 0, offsetX: 0}, {delay: 7*2*17, offsetX: 20}]

class BattlegroundConfettisAnimation extends Sprite
{	
	constructor()
	{
		super();

		this._fConfettiAnimations_bca_arr = [];

		this._addContent();

		this.blendMode = PIXI.BLEND_MODES.ADD;
	}

	adjust(aStartTime_num)
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let lCurGameplayTime_num = l_gpi.gameplayTime;

		if (lCurGameplayTime_num >= aStartTime_num)
		{
			this.visible = true;

			for (let i=0; i<CONFETTI_PARAMS.length; i++)
			{
				let lCurConfettiStartTime_num = aStartTime_num + CONFETTI_PARAMS[i].delay;
				if (lCurConfettiStartTime_num >= lCurConfettiStartTime_num)
				{
					this._fConfettiAnimations_bca_arr[i].adjust(lCurConfettiStartTime_num);
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

		for (let i=0; i<this._fConfettiAnimations_bca_arr.length; i++)
		{
			let lCurAnimDuration_num = this._fConfettiAnimations_bca_arr[i].getTotalDuration() + CONFETTI_PARAMS[i].delay;
			if (lCurAnimDuration_num > lTotalDuration_num)
			{
				lTotalDuration_num = lCurAnimDuration_num;
			}
		}

		return lTotalDuration_num;
	}

	drop()
	{
		if (this._fConfettiAnimations_bca_arr)
		{
			for (let i=0; i<this._fConfettiAnimations_bca_arr.length; i++)
			{
				this._fConfettiAnimations_bca_arr[i].drop();
			}
		}

		this.visible = false;
	}

	_addContent()
	{
		for (let i=0; i<CONFETTI_PARAMS.length; i++)
		{
			let l_bcra = this.addChild(new BattlegroundConfettiAnimation());
			l_bcra.position.x = CONFETTI_PARAMS[i].offsetX;
			
			this._fConfettiAnimations_bca_arr.push(l_bcra);
		}
	}
}

export default BattlegroundConfettisAnimation