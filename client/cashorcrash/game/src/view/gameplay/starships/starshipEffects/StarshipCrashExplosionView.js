import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import MTimeLine from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import StarshipBaseView from '../StarshipBaseView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import StarshipTakeOffExplosionView from './StarshipTakeOffExplosionView';

class StarshipCrashExplosionView extends Sprite
{
	static get EVENT_ON_EXPLOSION_STARTED ()			{ return "EVENT_ON_EXPLOSION_STARTED"; }
	
	constructor()
	{
		super();


		this._fFireFramesAnimation_rcfav = null;
		this._fFlash_sprt = null;
		this._fExplosionAnimation_rctl = null;

		//FIRE FRAMES ANIMATION...
		let lFire_sprt = new Sprite();
		let lFireView_sprt = Sprite.createMultiframesSprite(StarshipBaseView.getFireTextures());
		lFire_sprt.addChild(lFireView_sprt);
		lFireView_sprt.anchor.set(0.5, 0.5);
		lFireView_sprt.scale.set(2);
		lFireView_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lFireView_sprt.play();
		this._fFireFramesAnimation_rcfav = this.addChild(lFire_sprt);
		//...FIRE FRAMES ANIMATION

		//FLASH...
		let l_sprt = new Sprite;
		l_sprt.textures = [StarshipTakeOffExplosionView.getFlashTextures()[0]];
		l_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		this._fFlash_sprt = this.addChild(l_sprt);
		//...FLASH

		//INTRO ANIMATION...
		let l_rctl = new MTimeLine();

		l_rctl.addAnimation(
			this,
			MTimeLine.SET_ALPHA,
			0,
			[
				[1, 1],
				9,
				[0, 1],
			]);

		//FIRE SPRITE ANIMATION...
		l_rctl.addAnimation(
			this._fFireFramesAnimation_rcfav,
			MTimeLine.SET_SCALE_X,
			0,
			[
				[6, 5],
				[0, 6],
			]);

		l_rctl.addAnimation(
			this._fFireFramesAnimation_rcfav,
			MTimeLine.SET_SCALE_Y,
			0,
			[
				[3.5, 5],
				[0, 6],
			]);
		//...FIRE SPRITE ANIMATION

		//FLASH...
		l_rctl.addAnimation(
			this._fFlash_sprt,
			MTimeLine.SET_SCALE,
			0,
			[
				[7, 5],
				[0, 15],
			]);
		//...FLASH

		this._fExplosionAnimation_rctl = l_rctl;

		this._fExplosionAnimationTotalDuration_num = this._fExplosionAnimation_rctl.getTotalDurationInMilliseconds();
		this._fLastAdjustExplosionAnimTime_num = undefined;
		//...INTRO ANIMATION

		this.visible = false;
	}

	_onExplosionAnimationStarted()
	{
		if (this.visible && this._fLastAdjustExplosionAnimTime_num < this._fExplosionAnimationTotalDuration_num/2)
		{
			this.emit(StarshipCrashExplosionView.EVENT_ON_EXPLOSION_STARTED);
		}
	}

	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_ri = l_gpi.roundInfo;

		if (l_ri.isRoundPlayActive)
		{
			this.visible = false;
			this._resetAnimation();
		}
		else
		{
			let lOutOfRoundDuration_num = l_gpi.outOfRoundDuration;
			this.visible = lOutOfRoundDuration_num > 0 && lOutOfRoundDuration_num <= this._fExplosionAnimationTotalDuration_num;

			if (this.visible)
			{
				if (!this._fExplosionAnimation_rctl.isPlaying() && !this._fExplosionAnimation_rctl.isCompleted())
				{
					if(lOutOfRoundDuration_num <= 20) this._onExplosionAnimationStarted();
					
					this._fExplosionAnimation_rctl.playFromMillisecond(lOutOfRoundDuration_num);
					this._fLastAdjustExplosionAnimTime_num = lOutOfRoundDuration_num;
				}
			}
			else
			{
				this._resetAnimation();
			}
		}
	}

	deactivate()
	{
		this._resetAnimation();
	}

	_resetAnimation()
	{
		this._fExplosionAnimation_rctl.reset();
		this._fLastAdjustExplosionAnimTime_num = 0;
	}
}
export default StarshipCrashExplosionView;