import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import MTimeLine from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

class StarshipTakeOffSmokeView extends Sprite
{
	constructor()
	{
		super();

		this._fSmokesContainer_sprt = null;
		this._fIntroAnimation_mtl = null;

		this._fSmokesContainer_sprt = this.addChild(new Sprite);
		this._fSmokesContainer_sprt.position.set(60, 70);

		this._fIntroAnimation_mtl = new MTimeLine();

		this._addSmoke(2, {x: 0, y: 0}, [0.7, 1, 0, 21], [0, 20, 22], [0.11, 2.2, 22], [0.15, 2, 22]);
		this._addSmoke(3, {x: 47, y: -13}, [0.7, 4, 0, 16], [0, 20, 19], [0.11, 1.5, 20], [0.15, 0.8, 20]);
		this._addSmoke(5, {x: 15, y: -70}, [0.7, 4, 0, 16], [0, 75, 20], [0.11, 0.85, 20], [0.15, 0.8, 20]);

		this._fIntroAnimationTotalDuration_num = this._fIntroAnimation_mtl.getTotalDurationInMilliseconds();
		
		this.visible = false;
	}

	_addSmoke(aInitialDelay_num, aPosition_p, aAlphaChangeSettings_arr, aRotationDegreesChangeSettings_arr, aScaleXChangeSettings_arr, aScaleYChangeSettings_arr)
	{
		let l_mtl = this._fIntroAnimation_mtl;

		//SMOKE
		let lSmoke_sprt = new Sprite();
		let lSmokeView_sprt = APP.library.getSprite("game/smoke_puff");
		lSmoke_sprt.addChild(lSmokeView_sprt);
		
		lSmokeView_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lSmokeView_sprt.anchor.set(0.5, 0.5);
		lSmokeView_sprt.scale.set(2);
		lSmokeView_sprt.position.set(aPosition_p.x, aPosition_p.y);

		this._fSmokesContainer_sprt.addChild(lSmoke_sprt);
		//...SMOKE

		l_mtl.addAnimation(
			lSmoke_sprt,
			MTimeLine.SET_ALPHA,
			0,
			[
				aInitialDelay_num,
				[aAlphaChangeSettings_arr[0], 1],
				aAlphaChangeSettings_arr[1],
				[aAlphaChangeSettings_arr[2], aAlphaChangeSettings_arr[3]]
			]);

		l_mtl.addAnimation(
			lSmoke_sprt,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			aRotationDegreesChangeSettings_arr[0],
			[
				aInitialDelay_num,
				[aRotationDegreesChangeSettings_arr[1], aRotationDegreesChangeSettings_arr[2]]
			]);

		l_mtl.addAnimation(
			lSmoke_sprt,
			MTimeLine.SET_SCALE_X,
			aScaleXChangeSettings_arr[0],
			[
				aInitialDelay_num,
				[aScaleXChangeSettings_arr[1], aScaleXChangeSettings_arr[2]]
			]);

		l_mtl.addAnimation(
			lSmoke_sprt,
			MTimeLine.SET_SCALE_Y,
			aScaleYChangeSettings_arr[0],
			[
				aInitialDelay_num,
				[aScaleYChangeSettings_arr[1], aScaleYChangeSettings_arr[2]]
			]);
	}

	adjust()
	{
		let l_gpc = APP.gameController.gameplayController;
		let l_gpi =	l_gpc.info;
		let l_gpv = l_gpc.view;

		let lIntroAnimTime_num = 0;
		if (l_gpi.roundInfo.isRoundPlayState)
		{
			lIntroAnimTime_num = l_gpi.preLaunchFlightDuration + l_gpi.multiplierRoundDuration;
		}
		else if (l_gpi.isPreLaunchTimePeriod)
		{
			lIntroAnimTime_num = l_gpi.preLaunchFlightDuration - l_gpi.multiplierChangeFlightRestTime;
		}
		
		if (lIntroAnimTime_num >= this._fIntroAnimationTotalDuration_num)
		{
			this.visible = false;
			this._fIntroAnimation_mtl.windToMillisecond(0);
		}
		else
		{
			this._fIntroAnimation_mtl.windToMillisecond(lIntroAnimTime_num);
		}

		this.position.set(l_gpv.getStarshipLaunchX(), l_gpv.getStarshipLaunchY());
	}

	deactivate()
	{
		this._fIntroAnimation_mtl.windToMillisecond(0);
	}
}
export default StarshipTakeOffSmokeView;