import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import MTimeLine from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import StarshipBaseView from '../StarshipBaseView';
import AtlasSprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../../config/AtlasConfig';

class StarshipHeatPreFlightSmokeAnimation extends Sprite
{
	constructor()
	{
		super();

		let lGameplayController_gpc = APP.gameController.gameplayController;
		this._fGameplayInfo_gpi = lGameplayController_gpc.info;

		this._fBottomSmoke_sprt = null;
		this._fAnimation_mtl = null;

		//SMOKE...
		let lBS_sprt = APP.library.getSprite("game/smoke_engine");
		this._fBottomSmoke_sprt = this.addChild(lBS_sprt);

		let lTS_sprt = APP.library.getSprite("game/smoke_engine");
		this._fTopSmoke_sprt = this.addChild(lTS_sprt);
		//...SMOKE

		//ANIMATION...
		let l_mtl = new MTimeLine();
		let lMult_num = APP.isBattlegroundGame ? 1 : 2;

		//BOTTOM SMOKE...
		l_mtl.addAnimation(
			lBS_sprt,
			MTimeLine.SET_ALPHA,
			0,
			[
				[1, 4*lMult_num],
				[0, 16*lMult_num],
			]);

		l_mtl.addAnimation(
			lBS_sprt,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				[-62, 20*lMult_num]
			]);

		l_mtl.addAnimation(
			lBS_sprt,
			MTimeLine.SET_X,
			0,
			[
				[25, 20*lMult_num]
			]);

		l_mtl.addAnimation(
			lBS_sprt,
			MTimeLine.SET_Y,
			0,
			[
				[40, 20*lMult_num]
			]);

		l_mtl.addAnimation(
			lBS_sprt,
			MTimeLine.SET_SCALE,
			1,
			[
				[1.85, 20*lMult_num]
			]);
		//...BOTTOM SMOKE

		//TOP SMOKE...
		l_mtl.addAnimation(
			lTS_sprt,
			MTimeLine.SET_ALPHA,
			0,
			[
				[1, 4*lMult_num],
				[0, 14*lMult_num],
			]);

		l_mtl.addAnimation(
			lTS_sprt,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			80,
			[
				[20, 18*lMult_num]
			]);

		l_mtl.addAnimation(
			lTS_sprt,
			MTimeLine.SET_X,
			0,
			[
				[-5, 18*lMult_num]
			]);

		l_mtl.addAnimation(
			lTS_sprt,
			MTimeLine.SET_Y,
			0,
			[
				[60, 18*lMult_num]
			]);

		l_mtl.addAnimation(
			lTS_sprt,
			MTimeLine.SET_SCALE,
			0.45,
			[
				[1.25, 18*lMult_num]
			]);
		//...TOP SMOKE
		
		this._fAnimation_mtl = l_mtl;

		this._fAnimationTotalDuration_num = this._fAnimation_mtl.getTotalDurationInMilliseconds();
		//...ANIMATION
	}

	get duration()
	{
		return this._fAnimationTotalDuration_num;
	}

	adjust(aAnimDuration_num)
	{
		let lMS_int = aAnimDuration_num;
		if (lMS_int > this._fAnimationTotalDuration_num*2)
		{
			lMS_int = 0;
		}
		else if (lMS_int > this._fAnimationTotalDuration_num && lMS_int <= this._fAnimationTotalDuration_num*2)
		{
			lMS_int -= this._fAnimationTotalDuration_num;
		}
		this._fAnimation_mtl.windToMillisecond(lMS_int);
	}
}
export default StarshipHeatPreFlightSmokeAnimation;