import { Sprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';
import { Sequence } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation';
import Timer from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import MissEffect from '../../missEffects/MissEffect';
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

const ANIM_TYPES = {
	POWER_UP: 0,
	ADD_AMMO: 1
}
class LevelUpAnimation extends Sprite
{
	static get EVENT_ON_BADGE_APPEARED() 		{ return "onBadgeAppeared"; }
	static get EVENT_ON_BADGE_LANDED() 			{ return "onBadgeLanded"; }
	static get EVENT_ON_ANIMATION_COMPLETED() 	{ return "onAnimationCompleted"; }

	get turretTypeId()
	{
		return this._fTurretTypeId_num;
	}

	get isPowerUpAnimation()
	{
		return this._fAnimType_int == ANIM_TYPES.POWER_UP;
	}

	get isAddAmmoAnimation()
	{
		return this._fAnimType_int == ANIM_TYPES.ADD_AMMO;
	}

	get isBadgeLanded()
	{
		return this._isBadgeLanded_bl;
	}

	get shotsAmount()
	{
		return this._fShotsAmount_num;
	}

	constructor(aStartPosition_num, aFinalPosition_obj, aShotsAmount_num)
	{
		super();
		
		this._fStripesTimers_arr = [];

		this._fBadgeContainer_spr = null;
		this._fBadge_spr = null;
		this._fBadgeGlow_spr = null;

		this._fStartPosition_obj = aStartPosition_num;
		this._fFinalPosition_obj = aFinalPosition_obj;

		this._fTurretTypeId_num = undefined;
		this._fAnimType_int = undefined;

		this._isBadgeLanded_bl = false;

		this._fFlareContainer_sprt = null;
		this._fIsBottomSpot_bl = undefined;

		this._fShotsAmount_num = aShotsAmount_num;

		this._initView();
	}

	i_startLevelUpAnimation(aTurretTypeId_num=5, aPrevTurretTypeId_num, aIsBottomSpot_bl=true)
	{
		this._fTurretTypeId_num = aTurretTypeId_num;
		this._fIsBottomSpot_bl = aIsBottomSpot_bl;

		let lTitle_cta;

		if (aTurretTypeId_num !== aPrevTurretTypeId_num)
		{
			lTitle_cta = I18.generateNewCTranslatableAsset("TABattlegroundPowerUpTitle");
			this._fAnimType_int = ANIM_TYPES.POWER_UP;
		}
		else
		{
			lTitle_cta = I18.generateNewCTranslatableAsset("TABattlegroundPowerUpPlusAmmoTitle");
			this._fAnimType_int = ANIM_TYPES.ADD_AMMO;
		}

		this._fTitle_spr = this._fBadge_spr.addChild(lTitle_cta);
		this._fTitle_spr.position.set(0, 25);
		
		this._fWeapon_spr.textures = [APP.library.getSpriteFromAtlas(`weapons/DefaultGun/turret_${aTurretTypeId_num}/turret`)];
		this._startAppearAnimation();

	}

	_initView()
	{
		this._fBadgeContainer_spr = this.addChild(new Sprite());
		this._fBadgeContainer_spr.visible = false;

		this._fBadge_spr = this._fBadgeContainer_spr.addChild(APP.library.getSpriteFromAtlas("battleground/powerup/background"));
		
		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			let lBadgeGlow_sprt = this._fBadgeGlow_spr = this._fBadgeContainer_spr.addChild(APP.library.getSpriteFromAtlas("battleground/powerup/badge_glow"));
			lBadgeGlow_sprt.scale.set(2);
			lBadgeGlow_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		}
		
		// FILL BADGE ...
		this._fWeapon_spr = this._fBadge_spr.addChild(new Sprite());
		this._fWeapon_spr.scale.set(0.3);
		this._fWeapon_spr.position.set(-10, 0);

		const STRIPES_POSITIONS_Y = [6, -2, -10];
		for (let i = 0; i < 3; i++)
		{
			let lNormalStripe_spr = this._fBadge_spr.addChild(APP.library.getSpriteFromAtlas("battleground/powerup/stripe_normal"));
			lNormalStripe_spr.position.set(11, STRIPES_POSITIONS_Y[i]);

			let lAddStripe_spr =  this._fBadge_spr.addChild(APP.library.getSpriteFromAtlas("battleground/powerup/stripe_add"));
			lAddStripe_spr.position.set(11, STRIPES_POSITIONS_Y[i]);
			lAddStripe_spr.blendMode = PIXI.BLEND_MODES.ADD;
			lAddStripe_spr.alpha = 0;

			let l_seq = [
				{ tweens: [{prop: "alpha", to: 1}], duration: 4 * FRAME_RATE },
				{ tweens: [], duration: 3 * FRAME_RATE },
				{ tweens: [{prop: "alpha", to: 0}], duration: 5 * FRAME_RATE }
			];

			let lAddStripeAplhaTimer_t = new Timer(()=>{
				lAddStripe_spr && Sequence.start(lAddStripe_spr, l_seq)
			}, 19 * FRAME_RATE, true);
			lAddStripeAplhaTimer_t.tick((2 - i) * 4 * FRAME_RATE); // so the first stripe lights first, the last waits longer

			this._fStripesTimers_arr.push(lAddStripeAplhaTimer_t);
		}
		//...FILL BADGE
	}

	_startAppearAnimation()
	{
		this._fBadgeContainer_spr.visible = true;
		this._fBadgeContainer_spr.position.set(this._fStartPosition_obj.x, this._fStartPosition_obj.y);

		this._fBadgeGlow_spr && this._fBadgeGlow_spr.fadeTo(0, 7*FRAME_RATE);

		let lBadgeAppearing_seq = [
			{
				tweens: [
					{prop: 'scale.x', from: 0.5, to: 1.5},
					{prop: 'scale.y', from: 0.5, to: 1.5},
				],
				duration: 3 * FRAME_RATE,
			},
			{
				tweens: [
					{prop: 'scale.x', to: 1},
					{prop: 'scale.y', to: 1},
					{prop: 'position.x', to: this._fStartPosition_obj.x+3},
					{prop: 'position.y', to: this._fStartPosition_obj.y-2, ease: Easing.sine.easeIn},
				],
				duration: 2 * FRAME_RATE,
			},
			{
				tweens: [
					{prop: 'position.x', to: this._fStartPosition_obj.x},
					{prop: 'position.y', to: this._fStartPosition_obj.y-5, ease: Easing.sine.easeOut},
				],
				duration: 2 * FRAME_RATE,
				onfinish: ()=>{
					Sequence.destroy(Sequence.findByTarget(this._fBadgeContainer_spr));
					this._onBadgeAppeared();
				}
			}
		];
		Sequence.start(this._fBadgeContainer_spr, lBadgeAppearing_seq);
	}

	_onBadgeAppeared()
	{
		this.emit(LevelUpAnimation.EVENT_ON_BADGE_APPEARED);
		//maybe at this moment the badges will stuck
		this._startHidingAnimation(); 
	}

	_startHidingAnimation()
	{
		let lPathVector_obj = {
			x: this._fFinalPosition_obj.x - this._fStartPosition_obj.x,
			y: this._fStartPosition_obj.y - this._fFinalPosition_obj.y,
		}
		let l_seq = [
			// FLYING TO PLAYER ANIMATION...
			{
				tweens: [
					{prop: 'position.x', to: lPathVector_obj.x/2, ease: Easing.sine.easeIn },
					{prop: 'position.y', to: lPathVector_obj.y/2, ease: Easing.sine.easeIn },
					{prop: 'scale.x', to: 1.5 },
					{prop: 'scale.y', to: 1.5 },
				],
				duration: 4 * FRAME_RATE
			},
			{
				tweens: [
					{prop: 'position.x', to: this._fFinalPosition_obj.x, ease: Easing.sine.easeOut },
					{prop: 'position.y', to: this._fFinalPosition_obj.y, ease: Easing.sine.easeOut },
					{prop: 'scale.x', to: .2 },
					{prop: 'scale.y', to: .2 },
				],
				duration: 3 * FRAME_RATE,
				onfinish: ()=>{
					Sequence.destroy(Sequence.findByTarget(this._fBadgeContainer_spr));
					
					this._onBadgeLanded();
				}
			}
			// ...FLYING TO PLAYER ANIMATION
		];

		Sequence.start(this._fBadgeContainer_spr, l_seq);
	}

	_onBadgeLanded()
	{
		this._fBadge_spr && this._fBadge_spr.destroy();

		this._isBadgeLanded_bl = true;

		this.emit(LevelUpAnimation.EVENT_ON_BADGE_LANDED);

		if (APP.profilingController.info.isVfxProfileValueLowerOrGreater)
		{
			this._startFinalAnimation();
		}
		else
		{
			this._onAnimationCompleted();
		}
	}

	_startFinalAnimation()
	{
		this._addSmoke();
		
		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._addFlares();

			if (this.isPowerUpAnimation)
			{
				this._addYellowSmokes();
			}
		}
	}

	_addSmoke()
	{
		let lSmoke_spr = this._fSmoke_spr = this.addChild(new Sprite);
		let lSmokeView_sprt = lSmoke_spr.addChild(APP.library.getSpriteFromAtlas("battleground/powerup/grey_smoke"))
		lSmokeView_sprt.scale.set(2);
		lSmokeView_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;

		lSmoke_spr.position.set(this._fFinalPosition_obj.x, this._fFinalPosition_obj.y);
		lSmoke_spr.scale.set(this.isPowerUpAnimation ? 0.4 : 0.2);

		lSmoke_spr.scaleTo(this.isPowerUpAnimation ? 1.8 : 0.9, 16*FRAME_RATE, undefined, () => this._onSmokeCompleted());
		lSmoke_spr.fadeTo(1, 3*FRAME_RATE, undefined, () => {
			lSmoke_spr.fadeTo(0, 13*FRAME_RATE)
		});
	}

	_onSmokeCompleted()
	{
		this._fSmoke_spr.destroy();
		this._fSmoke_spr = null;

		this._tryToCompleteAnimation();
	}

	_addFlares()
	{
		let lFlareContainer_sprt = this._fFlareContainer_sprt = this.addChild(new Sprite);
		lFlareContainer_sprt.position.set(this._fFinalPosition_obj.x, this._fFinalPosition_obj.y);

		this._addFlare({x: 0, y: 0}, {from:20, to:40}, {from:0.6, to:0});
		
		if (this.isPowerUpAnimation)
		{
			let lYMult = this._fIsBottomSpot_bl ? 1 : -1;
			this._addFlare({x: 0+5, y: 10*lYMult}, {from:10, to:30}, {from:0.35, to:0});
			this._addFlare({x: 0-5, y: -35*lYMult}, {from:10, to:30}, {from:0.35, to:0});
			this._addFlare({x: 0, y: 35*lYMult}, {from:10, to:30}, {from:0.35, to:0});
		}
		else
		{
			this._addFlare({x: 0-5, y: 0}, {from:10, to:30}, {from:0.35, to:0});
			this._addFlare({x: 0-3, y: 0-5}, {from:10, to:30}, {from:0.35, to:0});
		}
	}

	_addFlare(aPos_obj, aAngle_obj, aScale_obj)
	{
		let lFlare_spr = this._fFlareContainer_sprt.addChild(new Sprite);
		let lFlareView_sprt = lFlare_spr.addChild(APP.library.getSpriteFromAtlas("common/weapon_awarding_flare"));
		lFlareView_sprt.scale.set(2.7);
		lFlareView_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;

		lFlare_spr.x = aPos_obj.x;
		lFlare_spr.y = aPos_obj.y;

		lFlare_spr.scale.set(aScale_obj.from);
		lFlare_spr.rotation = Utils.gradToRad(aAngle_obj.from);

		lFlare_spr.rotateTo(Utils.gradToRad(aAngle_obj.to), 15*FRAME_RATE);
		lFlare_spr.scaleTo(aScale_obj.to, 15*FRAME_RATE, Easing.sine.easeIn, () => { this._onFlareCompleted(lFlare_spr); });
	}

	_onFlareCompleted(aFlare_sprt)
	{
		aFlare_sprt.destroy();

		if (!this._fFlareContainer_sprt.children.length)
		{
			this._tryToCompleteAnimation()
		}
	}

	_addYellowSmokes()
	{
		this._fYellowSmokesContainer_sprt = this.addChild(new Sprite);

		this._addYellowSmoke(-80, {x: -0.75, y: 0.75});
		this._addYellowSmoke(105, {x: 0.75, y: 0.75});

	}

	_addYellowSmoke(aAngle_num, aScale_obj)
	{
		let lSmoke_spr = this._fYellowSmokesContainer_sprt.addChild(new Sprite);

		lSmoke_spr.textures = MissEffect.getSmokeTextures();
		lSmoke_spr.tint = 0xff6800;
		lSmoke_spr.scale.set(aScale_obj.x, aScale_obj.y);
		lSmoke_spr.rotation = Utils.gradToRad(aAngle_num);
		lSmoke_spr.anchor.set(0.5, 0.6);
		lSmoke_spr.blendMode = PIXI.BLEND_MODES.ADD;
		lSmoke_spr.animationSpeed = 0.5;		
		lSmoke_spr.on('animationend', () =>
		{
			this._onYellowSmokeCompleted(lSmoke_spr);
		});

		lSmoke_spr.play();
	}

	_onYellowSmokeCompleted(aSmoke_spr)
	{
		aSmoke_spr.destroy();

		if (!this._fYellowSmokesContainer_sprt.children.length)
		{
			this._tryToCompleteAnimation()
		}
	}

	_tryToCompleteAnimation()
	{
		if (
				(!this._fFlareContainer_sprt || !this._fFlareContainer_sprt.children.length)
				&& !this._fSmoke_spr
				&& (!this._fYellowSmokesContainer_sprt || !this._fYellowSmokesContainer_sprt.children.length)
			)
		{
			this._onAnimationCompleted();
		}
	}

	_onAnimationCompleted()
	{
		this.emit(LevelUpAnimation.EVENT_ON_ANIMATION_COMPLETED);
	}

	_getPlayerSpotPosition()
	{
		return APP.gameScreen.gameField.spot.position;
	}

	destroy()
	{
		super.destroy();

		if (
			this._fStripesTimers_arr
			&& Array.isArray(this._fStripesTimers_arr)
			&& this._fStripesTimers_arr.length)
		{
			Timer.destroy(this._fStripesTimers_arr);
		}
		this._fStripesTimers_arr = null;

		this._fBadgeContainer_spr && Sequence.destroy(Sequence.findByTarget(this._fBadgeContainer_spr));
		this._fBadgeContainer_spr && this._fBadgeContainer_spr.destroy();
		this._fBadgeContainer_spr = null;
		
		if (this._fBadge_spr && this._fBadge_spr.children)
		{
			while (this._fBadge_spr.children.length)
			{
				let lChld = this._fBadge_spr.children[0];
				Sequence.destroy(Sequence.findByTarget(lChld));
				lChld.destroy();
			}
		}
		this._fBadge_spr = null;
		this._fBadgeGlow_spr = null;

		this._fStartPosition_obj = null;
		this._fFinalPosition_obj = null;

		this._fTurretTypeId_num = undefined;
		this._fAnimType_int = undefined;

		this._isBadgeLanded_bl = undefined;
		this._fIsBottomSpot_bl = undefined;

		this._fSmoke_spr = null;
		this._fFlareContainer_sprt = null;
		this._fShotsAmount_num = undefined;
	}
}

export default LevelUpAnimation;