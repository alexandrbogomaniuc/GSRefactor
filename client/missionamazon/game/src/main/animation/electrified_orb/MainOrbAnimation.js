import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Sprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';
import { Sequence } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import GameProfilingInfo from '../../../model/profiling/GameProfilingInfo';

const CIRCLE_LIGHTS_SETTINGS = {
	"1": {
		"scale": 0.5,
		"baseAngle": 205,
		"radius": 58
	},
	"2": {
		"scale": 0.5,
		"baseAngle": 140,
		"radius": 64
	}
};

const INNER_LIGHTS_LIMIT = 5;

const INNER_LIGHTS_SETTINGS = {
	"1": {
		"scale": 1.1,
		"baseAngle": 205
	},
	"2": {
		"scale": 1,
		"baseAngle": 140
	},
	"3": {
		"scale": 0.85,
		"baseAngle": 50
	}
};

const TIME_INTRVAL_MULTIPLIERS =
{
	[GameProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.LOW]: {
		startCircleLights: 2,
		nextCircleLights: 2,
		startNextInnerLight: 10,
		removeInnerLight: 1,
	},
	[GameProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.LOWER]: {
		startCircleLights: 2,
		nextCircleLights: 2,
		startNextInnerLight: 5,
		removeInnerLight: 1,
	},
	[GameProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM]: {
		startCircleLights: 1,
		nextCircleLights: 1,
		startNextInnerLight: 1,
		removeInnerLight: 1,
	},
	[GameProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.HIGH]: {
		startCircleLights: 1,
		nextCircleLights: 1,
		startNextInnerLight: 1,
		removeInnerLight: 1,
	},
};

class MainOrbAnimation extends Sprite
{
	static get EVENT_ON_GLOW_COMPLETED() { return "onGlowCompleted"; }

	get timeIntrvalMultipliers()
	{
		let lVFXProfile_str = GameProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.HIGH;
		if (APP && APP.profilingController && APP.profilingController.info && APP.profilingController.info.vfxProfileValue)
		{
			lVFXProfile_str = APP.profilingController.info.vfxProfileValue;
		}

		return TIME_INTRVAL_MULTIPLIERS[lVFXProfile_str];
	}

	startAnimation()
	{
		this._startAnimation();
	}

	startGlowAnimation()
	{
		this._startGlowAnimation();
	}

	startDisappearAnimation()
	{
		this._startOrbDisappearAnimation();
	}

	onDeathRequired()
	{
		this._onDeathRequired();
	}

	constructor()
	{
		super();

		this._fOrbContainer_sprt = null;
		this._fGlowCircle_sprt = null;
		this._fLastCircleAngle_num = null;
		this._fCircleLightsCount_num = null;
		this._fInnerLightsCount_num = null;

		this._fGlowTimer_t = null;
		this._fTimers_arr = null;
		this._fCircleTimers_arr = null;
		this._fCircleLightsAllowed_bln = null;
		this._fGlowSettings_obj = null;

		this._init();
	}

	_init()
	{
		this._fTimers_arr = [];
		this._fCircleTimers_arr = [];

		this._fLastCircleAngle_num = 0;
		this._fCircleLightsCount_num = 0;
		this._fInnerLightsCount_num = 0;

		this._fCircleLightsAllowed_bln = true;

		this._fGlowSettings_obj = {
			alpha: 0.5
		};

		this._fOrbContainer_sprt = this.addChild(new Sprite());
		this._fOrbContainer_sprt.scale.set(1.02);

		let lTint_sprt = this._fOrbContainer_sprt.addChild(APP.library.getSprite("enemies/blue_orbs/fx_electrified_orb/tint"));
		lTint_sprt.alpha = 0.4;
		lTint_sprt.position.set(2, -4);

		let lTint2_sprt = lTint_sprt.addChild(APP.library.getSprite("enemies/blue_orbs/fx_electrified_orb/tint"));
		lTint2_sprt.alpha = 0.4;

		let lCircle_sprt = this._fOrbContainer_sprt.addChild(APP.library.getSprite("enemies/blue_orbs/fx_electrified_orb/circle"));
		lCircle_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		this._fGlowCircle_sprt = lCircle_sprt.addChild(APP.library.getSprite("enemies/blue_orbs/fx_electrified_orb/circle_glow"));
		this._fGlowCircle_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		this._fGlowCircle_sprt.alpha = this._fGlowSettings_obj.alpha;

		if (APP.isMobile)
		{
			let lMask_gr = lCircle_sprt.addChild(new PIXI.Graphics());
			lCircle_sprt.mask = lMask_gr;
			lMask_gr.beginFill(0x000000).drawRect(-128 + 2, -128 + 2, 256 - 4, 256 - 4).endFill();
		}
	}

	_startAnimation()
	{
		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._startNextOrbAnimation();
		}
		this._startCircleLights();
		this._startNextInnerLight();
	}

	_startNextOrbAnimation()
	{
		if (!this._fOrbContainer_sprt) return;

		let lSeq_arr = [
			{ tweens: [{ prop: 'scale.x', to: 0.98 }, { prop: 'scale.y', to: 0.98 }], duration: 10 * FRAME_RATE, ease: Easing.quadratic.easeIn },
			{
				tweens: [{ prop: 'scale.x', to: 1.02 }, { prop: 'scale.y', to: 1.02 }], duration: 10 * FRAME_RATE, onfinish: () =>
				{
					this._startNextOrbAnimation();
				}
			}
		];

		Sequence.start(this._fOrbContainer_sprt, lSeq_arr);
	}

	_startCircleLights()
	{
		if (!this._fCircleLightsAllowed_bln) return;

		this._startNextCircleLight();
		let lCreateTimer_t;

		for (let i = 0; i < 5; ++i)
		{
			lCreateTimer_t = new Timer(() => this._startNextCircleLight(), (2 * this.timeIntrvalMultipliers.startCircleLights * i + 2) * FRAME_RATE);
			this._fCircleTimers_arr.push(lCreateTimer_t);
		}
	}

	_startNextCircleLight()
	{
		if (!this._fOrbContainer_sprt) return;

		let lLight_sprt = this._fOrbContainer_sprt.addChild(this._generateCircleLight());
		++this._fCircleLightsCount_num;

		let lRemoveTimer_t = new Timer(() =>
		{
			--this._fCircleLightsCount_num;
			if (lLight_sprt)
			{
				this._fOrbContainer_sprt && this._fOrbContainer_sprt.removeChild(lLight_sprt);
				lLight_sprt.destroy();
			}

			if (this._fCircleLightsCount_num == 0)
			{
				let lNextTimer_t = new Timer(() => this._startCircleLights(), 4 * this.timeIntrvalMultipliers.nextCircleLights * FRAME_RATE);
				this._fTimers_arr.push(lNextTimer_t);
			}
		}, 5 * FRAME_RATE);

		this._fTimers_arr.push(lRemoveTimer_t);
	}

	_generateCircleLight()
	{
		let lId_num = Utils.random(1, 2);
		let lSettings_obj = CIRCLE_LIGHTS_SETTINGS[lId_num];

		let lScaleY_num = Math.random() > 0.5 ? -1 : 1;
		let lBaseAngle_num = lScaleY_num == -1 ? 360 - lSettings_obj.baseAngle : lSettings_obj.baseAngle;
		let lRadius_num = lSettings_obj.radius;
		let lScale_num = lSettings_obj.scale;

		let lSrc_str = "enemies/blue_orbs/fx_electrified_orb/lightning_" + lId_num;
		let lLight_sprt = APP.library.getSprite(lSrc_str);
		lLight_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		if (APP.isMobile)
		{
			let lMask_gr = lLight_sprt.addChild(new PIXI.Graphics());
			lLight_sprt.mask = lMask_gr;
			let lWidth_num = lId_num == 2 ? 84 : 64;
			let lHeight_num = lId_num == 2 ? 136 : 128;
			lMask_gr.beginFill(0x000000).drawRect(-lWidth_num / 2 + 1, -lHeight_num / 2 + 1, lWidth_num - 2, lHeight_num - 2).endFill();
		}

		let lAngle_num = this._fLastCircleAngle_num;
		lLight_sprt.rotation = Utils.gradToRad(lBaseAngle_num + lAngle_num);
		lLight_sprt.scale.set(lScale_num);
		lLight_sprt.scale.y *= lScaleY_num;

		lLight_sprt.position.x = lRadius_num * Math.cos(Utils.gradToRad(lAngle_num));
		lLight_sprt.position.y = lRadius_num * Math.sin(Utils.gradToRad(lAngle_num));

		this._fLastCircleAngle_num += 60;
		if (this._fLastCircleAngle_num >= 360) this._fLastCircleAngle_num = 0;

		return lLight_sprt;
	}

	_startNextInnerLight()
	{
		if (!this._fOrbContainer_sprt) return;

		let lCreateTimer_t = new Timer(() => this._startNextInnerLight(), Utils.random(5 * this.timeIntrvalMultipliers.startNextInnerLight * FRAME_RATE, 13 * this.timeIntrvalMultipliers.startNextInnerLight * FRAME_RATE));
		this._fTimers_arr.push(lCreateTimer_t);

		if (this._fInnerLightsCount_num > INNER_LIGHTS_LIMIT)
		{
			return;
		}

		let lLight_sprt = this._fOrbContainer_sprt.addChild(this._generateInnerLight());
		++this._fInnerLightsCount_num;

		let lRemoveTimer_t = new Timer(() =>
		{
			if (lLight_sprt)
			{
				this._fOrbContainer_sprt && this._fOrbContainer_sprt.removeChild(lLight_sprt);
				lLight_sprt.destroy();
			}
			--this._fInnerLightsCount_num;
		}, Utils.random(10 * this.timeIntrvalMultipliers.removeInnerLight * FRAME_RATE, 19 * this.timeIntrvalMultipliers.removeInnerLight * FRAME_RATE));
		this._fTimers_arr.push(lRemoveTimer_t);
	}

	_generateInnerLight()
	{
		let lId_num = Utils.random(1, 3);
		let lSettings_obj = INNER_LIGHTS_SETTINGS[lId_num];

		let lScaleY_num = Math.random() > 0.5 ? -1 : 1;
		let lBaseAngle_num = lScaleY_num == -1 ? 360 - lSettings_obj.baseAngle : lSettings_obj.baseAngle;
		let lScale_num = lSettings_obj.scale;

		let lContainer_sprt = new Sprite();

		let lSrc_str = "enemies/blue_orbs/fx_electrified_orb/lightning_" + lId_num;
		let lLight_sprt = APP.library.getSprite(lSrc_str);
		lLight_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		lContainer_sprt.addChild(lLight_sprt);

		if (APP.isMobile)
		{
			let lLightMask_gr = lLight_sprt.addChild(new PIXI.Graphics());
			lLight_sprt.mask = lLightMask_gr;
			let lWidth_num = lId_num == 3 ? 114 : (lId_num == 1 ? 64 : 84);
			let lHeight_num = lId_num == 3 ? 154 : (lId_num == 1 ? 128 : 136);
			lLightMask_gr.beginFill(0x000000).drawRect(-lWidth_num / 2 + 1.5, -lHeight_num / 2 + 1.5, lWidth_num - 3, lHeight_num - 3).endFill();
		}

		let lAngle_num = Utils.random(0, 360);
		lContainer_sprt.rotation = Utils.gradToRad(lBaseAngle_num + lAngle_num);
		lContainer_sprt.scale.set(lScale_num);
		lContainer_sprt.scale.y *= lScaleY_num;

		let lGlowLight_sprt = lContainer_sprt.addChild(this._generateGlowLight(lId_num));
		lGlowLight_sprt.alpha = this._fGlowSettings_obj.alpha;

		return lContainer_sprt;
	}

	_generateGlowLight(aId_num)
	{
		let lSrc_str = "enemies/blue_orbs/fx_electrified_orb/lightning_glow_" + aId_num;
		let lLight_sprt = APP.library.getSprite(lSrc_str);
		lLight_sprt.scale.set(2);
		lLight_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		if (aId_num == 1)
		{
			lLight_sprt.scale.set(2.1);
			lLight_sprt.position.set(0, -2);
		}
		if (aId_num == 2)
		{
			lLight_sprt.scale.set(1.95);
			lLight_sprt.position.set(1, 1);
		}
		if (aId_num == 3)
		{
			lLight_sprt.scale.set(2.05);
			lLight_sprt.position.set(0, 0);
		}

		if (APP.isMobile)
		{
			let lLightMask_gr = lLight_sprt.addChild(new PIXI.Graphics());
			lLight_sprt.mask = lLightMask_gr;
			let lWidth_num = aId_num == 3 ? 97 : (aId_num == 1 ? 80 : 92);
			let lHeight_num = aId_num == 3 ? 99 : (aId_num == 1 ? 74 : 88);
			lLightMask_gr.beginFill(0x000000).drawRect(-lWidth_num / 2 + 1.5, -lHeight_num / 2 + 1.5, lWidth_num - 3, lHeight_num - 3).endFill();
		}

		return lLight_sprt;
	}

	_startGlowAnimation()
	{
		if (this._fCircleTimers_arr)
		{
			for (let lTimer_t of this._fCircleTimers_arr)
			{
				lTimer_t && lTimer_t.destructor();
			}
		}
		this._fCircleTimers_arr = [];
		this._fCircleLightsAllowed_bln = false;

		this._fGlowTimer_t && this._fGlowTimer_t.destructor();
		this._fGlowTimer_t = new Timer(() => this._updateGlow(), 1, true);

		let lSeq_arr = [{ tweens: [{ prop: 'alpha', to: 1 }], duration: 20 * FRAME_RATE, onfinish: () => this._onGlowAnimationEnded() }];
		Sequence.start(this._fGlowSettings_obj, lSeq_arr);
	}

	_updateGlow()
	{
		if (!this._fGlowSettings_obj) return;

		if (this._fOrbContainer_sprt && this._fOrbContainer_sprt.children)
		{
			for (let lCont_sprt of this._fOrbContainer_sprt.children)
			{
				if (lCont_sprt && lCont_sprt.children)
				{
					let lGlow_sprt = lCont_sprt.children[1];
					if (lGlow_sprt)
					{
						lGlow_sprt.alpha = this._fGlowSettings_obj.alpha;
					}
				}
			}
		}

		if (this._fGlowCircle_sprt)
		{
			this._fGlowCircle_sprt.alpha = this._fGlowSettings_obj.alpha;
		}
	}

	_onGlowAnimationEnded()
	{
		this._fGlowTimer_t && this._fGlowTimer_t.destructor();
		this._fGlowTimer_t = null;

		this.emit(MainOrbAnimation.EVENT_ON_GLOW_COMPLETED);
	}

	_startOrbDisappearAnimation()
	{
		if (!this._fOrbContainer_sprt) return;

		let lSeq_arr = [
			{ tweens: [{ prop: 'scale.x', to: 1.19 }, { prop: 'scale.y', to: 1.19 }], duration: 5 * FRAME_RATE, ease: Easing.quartic.easeOut },
			{
				tweens: [{ prop: 'scale.x', to: 0 }, { prop: 'scale.y', to: 0 }], duration: 6 * FRAME_RATE, onfinish: () =>
				{
					if (this._fOrbContainer_sprt)
					{
						this._fEffectContainer_sprt && this._fEffectContainer_sprt.removeChild(this._fOrbContainer_sprt);
						this._fOrbContainer_sprt.destroy();
						this._fOrbContainer_sprt = null;
					}
				}
			}
		];

		Sequence.destroy(Sequence.findByTarget(this._fOrbContainer_sprt));
		Sequence.start(this._fOrbContainer_sprt, lSeq_arr);
	}

	_destroyAnimations()
	{
		this._fGlowTimer_t && this._fGlowTimer_t.destructor();

		if (this._fTimers_arr)
		{
			for (let lTimer_t of this._fTimers_arr)
			{
				lTimer_t && lTimer_t.destructor();
			}
		}

		if (this._fCircleTimers_arr)
		{
			for (let lTimer_t of this._fCircleTimers_arr)
			{
				lTimer_t && lTimer_t.destructor();
			}
		}

		if (this._fGlowCircle_sprt)
		{
			this._fGlowCircle_sprt.destroy();
			this._fGlowCircle_sprt = null;
		}

		if (this._fOrbContainer_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fOrbContainer_sprt));
			this._fOrbContainer_sprt.destroy();
			this._fOrbContainer_sprt = null;
		}

		if (this._fGlowSettings_obj)
		{
			Sequence.destroy(Sequence.findByTarget(this._fGlowSettings_obj));
		}

		this._fLastCircleAngle_num = 0;
		this._fCircleLightsCount_num = 0;
		this._fInnerLightsCount_num = 0;

		this._fTimers_arr = [];
		this._fCircleTimers_arr = [];

		this._fGlowTimer_t = null;

		this.emit(MainOrbAnimation.EVENT_ON_ANIMATIONS_DESTROYED);
	}

	destroy()
	{
		this._destroyAnimations();

		super.destroy();

		this._fOrbContainer_sprt = null;
		this._fGlowCircle_sprt = null;
		this._fLastCircleAngle_num = null;
		this._fCircleLightsCount_num = null;
		this._fInnerLightsCount_num = null;

		this._fGlowTimer_t = null;
		this._fTimers_arr = null;
		this._fCircleTimers_arr = null;
		this._fCircleLightsAllowed_bln = null;
		this._fGlowSettings_obj = null;
	}
}

export default MainOrbAnimation;