import { Sprite } from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import { APP } from "../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import AtlasSprite from "../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite";
import AtlasConfig from "../../../../config/AtlasConfig";
import MTimeLine from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';

class BattlegroundWinCoinBaseAnimation extends Sprite
{
	adjust(aStartTime_num)
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let lCurGameplayTime_num = l_gpi.gameplayTime;

		if (lCurGameplayTime_num >= aStartTime_num)
		{
			this.visible = true;

			if (!this._fBurstAnimation_mtl.isPlaying() && !this._fBurstAnimation_mtl.isCompleted())
			{
				this._fBurstAnimation_mtl.playFromMillisecond(lCurGameplayTime_num - aStartTime_num);
			}
		}
		else
		{
			this.drop();
		}
	}

	getTotalDuration()
	{
		return this._fBurstAnimtionDuration_num;
	}

	drop()
	{
		this.visible = false;

		this._fBurstAnimation_mtl.reset();

		for (let i = 0; i < this._fAnimationConfig_obj_arr.length; ++i)
		{
			let lConfig_obj = this._fAnimationConfig_obj_arr[i];
			if (lConfig_obj.delay > 0)
			{
				let lCoin_sprt = this.getChildAt(i);
				if (lCoin_sprt)
				{
					lCoin_sprt.visible = false;
				}
			}
		}
	}

	constructor(aAnimationConfig_obj_arr)
	{
		super();

		this._fAnimationConfig_obj_arr = aAnimationConfig_obj_arr;
		this._fBurstAnimation_mtl = null;
		this._fBurstAnimtionDuration_num = undefined;

		this._generateAnimation();
	
		this.visible = false;
	}

	_generateAnimation()
	{
		let l_mtl = this._fBurstAnimation_mtl = new MTimeLine();

		let lMaxCoinStartDelay_int = 0;
		for (let i = 0; i < this._fAnimationConfig_obj_arr.length; ++i)
		{
			let lConfig_obj = this._fAnimationConfig_obj_arr[i];
			this._generateCoinAnimation(lConfig_obj, i);

			if (lConfig_obj.delay > lMaxCoinStartDelay_int)
			{
				lMaxCoinStartDelay_int = lConfig_obj.delay;
			}
		}

		if (lMaxCoinStartDelay_int > 0)
		{
			l_mtl.addAnimation(
				this._checkCoinsAppearing,
				MTimeLine.EXECUTE_METHOD,
				0,
				[
					[lMaxCoinStartDelay_int, lMaxCoinStartDelay_int]
				],
				this
			);
		}

		this._fBurstAnimtionDuration_num = this._fBurstAnimation_mtl.getTotalDurationInMilliseconds();
	}

	_generateCoinAnimation(aConfig_obj, aCoinViewIndex_int=0)
	{
		let lCoinContainer_sprt = this.addChild(new Sprite());

		let lCoin_sprt = lCoinContainer_sprt.addChild(new Sprite());
		lCoin_sprt.textures = [BattlegroundWinCoinBaseAnimation.getCoinTexture(aCoinViewIndex_int)];
		lCoin_sprt.anchor.set(0.5);

		let l_mtl = this._fBurstAnimation_mtl;

		let lInitialDelay_int = aConfig_obj.delay;

		if (!!aConfig_obj.curvePoint)
		{
			l_mtl.addAnimation(
				lCoin_sprt,
				MTimeLine.SET_X,
				aConfig_obj.posStart.x,
				[
					lInitialDelay_int,
					[aConfig_obj.curvePoint.x, aConfig_obj.duration/2, MTimeLine.EASE_OUT],
					[0, aConfig_obj.duration/2, MTimeLine.EASE_IN]
				],
				this);

			l_mtl.addAnimation(
				lCoin_sprt,
				MTimeLine.SET_Y,
				aConfig_obj.posStart.y,
				[
					lInitialDelay_int,
					[aConfig_obj.curvePoint.y, aConfig_obj.duration/2, MTimeLine.EASE_OUT],
					[0, aConfig_obj.duration/2, MTimeLine.EASE_IN]
				],
				this);
		}
		else
		{
			l_mtl.addAnimation(
				lCoin_sprt,
				MTimeLine.SET_X,
				aConfig_obj.posStart.x,
				[
					lInitialDelay_int,
					[aConfig_obj.posEnd.x, aConfig_obj.duration, MTimeLine.EASE_OUT]
				],
				this);

			l_mtl.addAnimation(
				lCoin_sprt,
				MTimeLine.SET_Y,
				aConfig_obj.posStart.y,
				[
					lInitialDelay_int,
					[aConfig_obj.posEnd.y, aConfig_obj.duration, MTimeLine.EASE_OUT]
				],
				this);
		}

		l_mtl.addAnimation(
			lCoin_sprt,
			MTimeLine.SET_SCALE,
			aConfig_obj.scaleStart,
			[
				lInitialDelay_int,
				[aConfig_obj.scaleEnd, aConfig_obj.duration]
			],
			this);

		l_mtl.addAnimation(
			lCoin_sprt,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			aConfig_obj.rotationStart,
			[
				lInitialDelay_int,
				[aConfig_obj.rotationEnd, aConfig_obj.duration]
			],
			this);

		l_mtl.addAnimation(
			lCoinContainer_sprt,
			MTimeLine.SET_X,
			0,
			[
				lInitialDelay_int,
				[aConfig_obj.posEnd.x, aConfig_obj.duration]
			],
			this);

		l_mtl.addAnimation(
			lCoinContainer_sprt,
			MTimeLine.SET_Y,
			0,
			[
				lInitialDelay_int,
				[aConfig_obj.posEnd.y, aConfig_obj.duration]
			],
			this);

		if (lInitialDelay_int > 0)
		{
			lCoinContainer_sprt.visible = false;
		}
	}

	_checkCoinsAppearing(aFrame_num)
	{
		let lDelayFrame_int = Math.floor(aFrame_num)+1;
		for (let i = 0; i < this._fAnimationConfig_obj_arr.length; ++i)
		{
			let lConfig_obj = this._fAnimationConfig_obj_arr[i];
			if (lConfig_obj.delay <= lDelayFrame_int)
			{
				let lCoin_sprt = this.getChildAt(i);
				if (lCoin_sprt)
				{
					lCoin_sprt.visible = true;
				}
			}
		}
	}

	destroy()
	{
		this._fBurstAnimation_mtl && this._fBurstAnimation_mtl.destroy();
		this._fBurstAnimation_mtl = null;
		this._fBurstAnimtionDuration_num = undefined;

		this._fAnimationConfig_obj_arr = null;

		super.destroy();
	}
}

export default BattlegroundWinCoinBaseAnimation;

BattlegroundWinCoinBaseAnimation.getCoinTexture = function(aIndex_int)
{
	if (!BattlegroundWinCoinBaseAnimation.coin_textures)
	{
		BattlegroundWinCoinBaseAnimation.coin_textures = AtlasSprite.getFrames([APP.library.getAsset("game/coin_spin")], AtlasConfig.WinCoin, "");
	}

	let lCoinIndex_int = aIndex_int%BattlegroundWinCoinBaseAnimation.coin_textures.length;

	return BattlegroundWinCoinBaseAnimation.coin_textures[lCoinIndex_int];
}