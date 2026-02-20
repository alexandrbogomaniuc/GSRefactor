import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import MTimeLine from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import StarshipBaseView from '../StarshipBaseView';
import AtlasSprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../../config/AtlasConfig';

class StarshipTakeOffExplosionView extends Sprite
{
	static get EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED ()			{ return "EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED"; }

	constructor()
	{
		super();

		this._fFlashesContainer_sprt = null;
		this._fFireFramesAnimation_rcfav = null;
		this._fFlash_sprt = null;
		this._fIntroAnimation_mtl = null;

		this._fFlashesContainer_sprt = this.addChild(new Sprite);

		//FIRE FRAMES ANIMATION...
		let lFire_sprt = new Sprite();
		let lFireView_sprt = Sprite.createMultiframesSprite(StarshipBaseView.getFireTextures());
		lFire_sprt.addChild(lFireView_sprt);
		lFireView_sprt.anchor.set(0.5, 0.5);
		lFireView_sprt.scale.set(2);
		lFireView_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lFireView_sprt.rotation = Utils.gradToRad(-90);
		lFireView_sprt.play();
		this._fFireFramesAnimation_rcfav = this._fFlashesContainer_sprt.addChild(lFire_sprt);
		//...FIRE FRAMES ANIMATION

		//FLASH...
		let l_rcdo = new Sprite;
		l_rcdo.textures = [StarshipTakeOffExplosionView.getFlashTextures()[0]];
		l_rcdo.blendMode = PIXI.BLEND_MODES.ADD;
		l_rcdo.position.set(-50, 0);
		this._fFlash_sprt = this._fFlashesContainer_sprt.addChild(l_rcdo);
		//...FLASH

		//INTRO ANIMATION...
		let l_mtl = new MTimeLine();

		l_mtl.addAnimation(
			this._fFlashesContainer_sprt,
			MTimeLine.SET_ALPHA,
			0,
			[
				[1, 1],
				9,
				[0, 1],
			]);

		//FIRE SPRITE ANIMATION...
		l_mtl.addAnimation(
			this._fFireFramesAnimation_rcfav,
			MTimeLine.SET_SCALE_X,
			0,
			[
				[15, 5],
				[0, 6],
			]);

		l_mtl.addAnimation(
			this._fFireFramesAnimation_rcfav,
			MTimeLine.SET_SCALE_Y,
			0,
			[
				[7, 5],
				[0, 6],
			]);
		//...FIRE SPRITE ANIMATION

		//FLASH...
		l_mtl.addAnimation(
			this._fFlash_sprt,
			MTimeLine.SET_SCALE,
			0,
			[
				[7, 5],
				[0, 6],
			]);
		//...FLASH

		this._fIntroAnimation_mtl = l_mtl;
		this._fIntroAnimation_mtl.callFunctionAtFrame(this._onIntroAnimationStarted, 1, this);

		this._fIntroAnimationTotalDuration_num = this._fIntroAnimation_mtl.getTotalDurationInMilliseconds();
		this._fLastAdjustIntroAnimTime_num = undefined;
		//...INTRO ANIMATION

		this.visible = false;
	}

	_onIntroAnimationStarted()
	{
		if (this.visible && this._fLastAdjustIntroAnimTime_num < this._fIntroAnimationTotalDuration_num/2)
		{
			this.emit(StarshipTakeOffExplosionView.EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED);
		}
	}

	adjust(aZoomScale_num=1, dx_num=0, dy_num=0)
	{
		let l_gpc = APP.gameController.gameplayController;
		let l_gpi =	l_gpc.info;
		let l_gpv = l_gpc.view;

		let lIntroAnimTime_num = 0;
		
		if (APP.isBattlegroundGame)
		{
			if (l_gpi.isPreLaunchTimePeriod)
			{
			lIntroAnimTime_num = l_gpi.preLaunchFlightDuration - l_gpi.multiplierChangeFlightRestTime;
			}
			else if (l_gpi.roundInfo.isRoundPlayState)
			{
				lIntroAnimTime_num = l_gpi.preLaunchFlightDuration + l_gpi.multiplierRoundDuration;
			}
		}
		else
		{
			if (l_gpi.roundInfo.isRoundPlayState)
			{
				lIntroAnimTime_num = l_gpi.preLaunchFlightDuration + l_gpi.multiplierRoundDuration;
			}
			else if (l_gpi.isPreLaunchTimePeriod)
			{
				lIntroAnimTime_num = l_gpi.preLaunchFlightDuration - l_gpi.multiplierChangeFlightRestTime;
			}
		}

		if (lIntroAnimTime_num >= this._fIntroAnimationTotalDuration_num)
		{
			this.visible = false;
			this._fLastAdjustIntroAnimTime_num = 0;
			this._fIntroAnimation_mtl.windToMillisecond(0);
		}
		else
		{
			this._fLastAdjustIntroAnimTime_num = lIntroAnimTime_num;
			this._fIntroAnimation_mtl.windToMillisecond(lIntroAnimTime_num);
		}

		this.position.set(l_gpv.getStarshipLaunchX()+dx_num, l_gpv.getStarshipLaunchY()+dy_num);
		this.rotation = l_gpi.isPreLaunchFlightRequired ? -Math.PI/2 : 0;
		if (aZoomScale_num > 1)
		{
			this.scale.set(aZoomScale_num*1.5);
		}
		else
		{
			this.scale.set(1);
		}
	}

	deactivate()
	{
		this._fIntroAnimation_mtl.windToMillisecond(0);
	}
}
export default StarshipTakeOffExplosionView;

StarshipTakeOffExplosionView.getFlashTextures = function()
{
	if (!StarshipTakeOffExplosionView.flash_textures)
	{
		StarshipTakeOffExplosionView.flash_textures = [];

		StarshipTakeOffExplosionView.flash_textures = AtlasSprite.getFrames([APP.library.getAsset('game/gameplay_assets')], [AtlasConfig.GameplayAssets], 'flash');
		StarshipTakeOffExplosionView.flash_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return StarshipTakeOffExplosionView.flash_textures;
}