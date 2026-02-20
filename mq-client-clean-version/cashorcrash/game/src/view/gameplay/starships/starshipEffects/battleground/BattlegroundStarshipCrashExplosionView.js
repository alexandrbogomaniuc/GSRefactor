import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import MTimeLine from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import AtlasSprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../../../config/AtlasConfig';

class BattlegroundStarshipCrashExplosionView extends Sprite
{
	static get EVENT_ON_EXPLOSION_STARTED ()			{ return "EVENT_ON_EXPLOSION_STARTED"; }
	
	get explosionTotalDuration()
	{
		return this._fExplosionAnimationTotalDuration_num;
	}
	
	constructor()
	{
		super();


		this._fExplosionFramesAnimation_rcfav = null;
		this._fFlash_sprt = null;
		this._fLightParticle_sprt = null;
		this._fExplosionAnimation_rctl = null;
		this._fLeftSmoke_sprt = null;
		this._fMiddleSmoke_sprt = null;
		this._fRightSmoke_sprt = null;
		this._fExplosionAdd_sprt = null;
		this._fExplosionView_sprt = null;

		//LIGHT_PARTICLE...
		let lLP_sprt = APP.library.getSprite("game/battleground/light_particle");
		lLP_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		this._fLightParticle_sprt = this.addChild(lLP_sprt);
		//...LIGHT_PARTICLE

		//FLASH...
		let lF_sprt = APP.library.getSprite("game/battleground/ship_explosion/light5_add");
		lF_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		this._fFlash_sprt = this.addChild(lF_sprt);
		//...FLASH

		//SMOKE...
		let lLS_sprt = APP.library.getSprite("game/battleground/ship_explosion/smoke_explosion");
		lLS_sprt.blendMode = PIXI.BLEND_MODES.NORMAL;
		this._fLeftSmoke_sprt = this.addChild(lLS_sprt);

		let lMS_sprt = APP.library.getSprite("game/battleground/ship_explosion/smoke_explosion");
		lMS_sprt.blendMode = PIXI.BLEND_MODES.NORMAL;
		this._fMiddleSmoke_sprt = this.addChild(lMS_sprt);

		let lRS_sprt = APP.library.getSprite("game/battleground/ship_explosion/smoke_explosion");
		lRS_sprt.blendMode = PIXI.BLEND_MODES.NORMAL;
		this._fRightSmoke_sprt = this.addChild(lRS_sprt);
		//...SMOKE

		//EXPLOSION_ADD...
		let lEA_sprt = APP.library.getSprite("game/battleground/ship_explosion/explosion_add");
		lEA_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		this._fExplosionAdd_sprt = this.addChild(lEA_sprt);
		//...EXPLOSION_ADD

		//EXPLOSION FRAMES ANIMATION...
		let lExplosion_sprt = new Sprite();
		let lExplosionView_sprt = this._fExplosionView_sprt = Sprite.createMultiframesSprite(BattlegroundStarshipCrashExplosionView.getFlameExplosionTextures());
		lExplosion_sprt.addChild(lExplosionView_sprt);
		lExplosionView_sprt.anchor.set(0.5, 0.5);
		lExplosionView_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lExplosionView_sprt.animationSpeed = 0.5;
		lExplosionView_sprt.loop = false;

		this._fExplosionFramesAnimation_rcfav = this.addChild(lExplosion_sprt);
		//...EXPLOSION FRAMES ANIMATION
		
		//INTRO ANIMATION...
		let l_rctl = new MTimeLine();
		let lMult_num = 2;

		//RIGHT SMOKE...
		l_rctl.addAnimation(
			lRS_sprt,
			MTimeLine.SET_ALPHA,
			0.35,
			[
				21,
				[0, 24, MTimeLine.EASE_IN_OUT]
			]);

		l_rctl.addAnimation(
			lRS_sprt,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			122,
			[
				5,
				[159.5, 38, MTimeLine.EASE_OUT]
			]);

		l_rctl.addAnimation(
			lRS_sprt,
			MTimeLine.SET_X,
			0,
			[
				5,
				[87, 39]
			]);

		l_rctl.addAnimation(
			lRS_sprt,
			MTimeLine.SET_Y,
			0,
			[
				5,
				[-35, 39]
			]);

		l_rctl.addAnimation(
			lRS_sprt,
			MTimeLine.SET_SCALE,
			0,
			[
				3,
				[1, 1],
				[1.38, 38, MTimeLine.EASE_OUT]
			]);
		//...RIGHT SMOKE

		//MIDDLE SMOKE...
		l_rctl.addAnimation(
			lMS_sprt,
			MTimeLine.SET_ALPHA,
			0.35,
			[
				23,
				[0, 24, MTimeLine.EASE_IN_OUT]
			]);

		l_rctl.addAnimation(
			lMS_sprt,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			93,
			[
				7,
				[159.5, 38, MTimeLine.EASE_OUT]
			]);

		l_rctl.addAnimation(
			lMS_sprt,
			MTimeLine.SET_X,
			0,
			[
				7,
				[54, 39]
			]);

		l_rctl.addAnimation(
			lMS_sprt,
			MTimeLine.SET_Y,
			0,
			[
				7,
				[139, 39]
			]);

		l_rctl.addAnimation(
			lMS_sprt,
			MTimeLine.SET_SCALE,
			0,
			[
				5,
				[1, 2],
				[1.38, 38, MTimeLine.EASE_OUT]
			]);
		//...MIDDLE SMOKE

		//LEFT SMOKE...
		l_rctl.addAnimation(
			lLS_sprt,
			MTimeLine.SET_ALPHA,
			0.35,
			[
				22,
				[0, 24, MTimeLine.EASE_IN_OUT]
			]);

		l_rctl.addAnimation(
			lLS_sprt,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				6,
				[37.5, 38, MTimeLine.EASE_OUT]
			]);

		l_rctl.addAnimation(
			lLS_sprt,
			MTimeLine.SET_X,
			0,
			[
				6,
				[-116, 39]
			]);

		l_rctl.addAnimation(
			lLS_sprt,
			MTimeLine.SET_Y,
			0,
			[
				6,
				[-21, 39]
			]);

		l_rctl.addAnimation(
			lLS_sprt,
			MTimeLine.SET_SCALE,
			0,
			[
				4,
				[1, 1],
				[1.38, 38, MTimeLine.EASE_OUT]
			]);
		//...LEFT SMOKE

		//EXPLOSION SPRITE ANIMATION...
		l_rctl.addAnimation(
			this._fExplosionFramesAnimation_rcfav,
			MTimeLine.SET_SCALE,
			0.56,
			[
				[2.52, 15, MTimeLine.EASE_IN_OUT],
				[0, 1]
			]);
		//...EXPLOSION SPRITE ANIMATION

		//EXPLOSION_ADD...
		l_rctl.addAnimation(
			this._fExplosionAdd_sprt,
			MTimeLine.SET_SCALE,
			0.05,
			[
				[1.27, 7, MTimeLine.EASE_OUT],
				[0, 1]
			]);
		//...EXPLOSION_ADD

		//LIGHT_PARTICLE...
		l_rctl.addAnimation(
			this._fLightParticle_sprt,
			MTimeLine.SET_SCALE,
			0,
			[
				2,
				[15.49, 1],
				2,
				[0, 25, MTimeLine.EASE_IN_OUT]
			]);
		//...LIGHT_PARTICLE

		//FLASH...
		l_rctl.addAnimation(
			this._fFlash_sprt,
			MTimeLine.SET_SCALE,
			0,
			[
				2,
				[3.47, 1],
				2,
				[0, 25, MTimeLine.EASE_IN_OUT]
			]);
		//...FLASH

		l_rctl.callFunctionAtFrame(this._onExplosionAnimationStarted, 1, this);

		this._fExplosionAnimation_rctl = l_rctl;

		this._fExplosionAnimationTotalDuration_num = this._fExplosionAnimation_rctl.getTotalDurationInMilliseconds();
		this._fLastAdjustExplosionAnimTime_num = undefined;
		//...INTRO ANIMATION

		this.visible = true;
	}

	_onExplosionAnimationStarted()
	{
		APP.forcedState = "QUALIFY";
		if (this.visible && this._fLastAdjustExplosionAnimTime_num < this._fExplosionAnimationTotalDuration_num/2)
		{
			this.emit(BattlegroundStarshipCrashExplosionView.EVENT_ON_EXPLOSION_STARTED);
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
				if (!this._fExplosionAnimation_rctl.isPlaying() && this._fExplosionView_sprt.currentFrame == 0)
				{
					this._fExplosionAnimation_rctl.playFromMillisecond(lOutOfRoundDuration_num);
					this._fLastAdjustExplosionAnimTime_num = lOutOfRoundDuration_num;
					this._fExplosionView_sprt.play();
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
		this._fExplosionView_sprt.gotoAndStop(0);

		this._fLastAdjustExplosionAnimTime_num = 0;
	}
}

BattlegroundStarshipCrashExplosionView.getFlameExplosionTextures = function()
{
	if (!BattlegroundStarshipCrashExplosionView.flame_explosion_textures)
	{
		BattlegroundStarshipCrashExplosionView.flame_explosion_textures = [];

		BattlegroundStarshipCrashExplosionView.flame_explosion_textures = AtlasSprite.getFrames([APP.library.getAsset('game/battleground/ship_explosion/flame_explosion')], [AtlasConfig.FlameExplosion], '');
		BattlegroundStarshipCrashExplosionView.flame_explosion_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return BattlegroundStarshipCrashExplosionView.flame_explosion_textures;
}

export default BattlegroundStarshipCrashExplosionView;