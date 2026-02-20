import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { FRAME_RATE, WEAPONS } from '../../../../shared/src/CommonConstants';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import InstantKillGunFlare from '../animation/instant_kill/InstantKillGunFlare';
import InstantKillSmokeLoop from '../animation/instant_kill/InstantKillSmokeLoop';
import Cryogun from '../animation/cryogun/Cryogun';
import FlameThrowerGun from '../animation/flamethrower/FlameThrowerGun';
import { MUZZLE_DISTANCE } from '../animation/flamethrower/FlameThrowerGun';
import Gun from '../animation/Gun';
import GameField from '../../main/GameField';
import Timer from "../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer";
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import RicochetLaserGun from '../animation/ricochet_laser/RicochetLaserGun';
import PlazmaGun from '../animation/instant_kill/PlazmaGun';

const MUZZLE_TIP_OFFSETS = {
	[WEAPONS.DEFAULT]: [48, 55, 60, 62, 70],
	[WEAPONS.INSTAKILL]: 0,
	[WEAPONS.INSTAKILL_POWER_UP]: 0,
	[WEAPONS.CRYOGUN]: 0,
	[WEAPONS.CRYOGUN_POWER_UP]: 0,
	[WEAPONS.FLAMETHROWER]: MUZZLE_DISTANCE,
	[WEAPONS.FLAMETHROWER_POWER_UP]: MUZZLE_DISTANCE,
	[WEAPONS.ARTILLERYSTRIKE]: 0,
	[WEAPONS.ARTILLERYSTRIKE_POWER_UP]: 0,
	[WEAPONS.RICOCHET]: 60,
	[WEAPONS.RICOCHET_POWER_UP]: 60
}

class Weapon extends Sprite
{
	static get EVENT_ON_GUN_RELOADED() { return Gun.EVENT_ON_RELOADED }
	static get EVENT_ON_GUN_SHOT_COMPLETED() { return Gun.EVENT_ON_SHOT_COMPLETED }
	static get EVENT_ON_GUN_RESET() { return Gun.EVENT_ON_RESET }
	static get EVENT_ON_REMOVE_WEAPON_ANIMATION_FINISH() { return "removeWeaponAnimationFinish"; }

	get gun()
	{
		return this.weaponSprite;
	}

	get isIdleState()
	{
		return this.gun.isIdleState;
	}

	get isReloadState()
	{
		return this.gun.isReloadState;
	}

	get isShotState()
	{
		return this.gun.isShotState;
	}

	get muzzleTipOffset()
	{
		let lCurWeaponId = this.id;
		let tipOffset = MUZZLE_TIP_OFFSETS[lCurWeaponId];

		if (lCurWeaponId == WEAPONS.DEFAULT)
		{
			tipOffset = tipOffset[this._fDefaultWeaponId_int - 1]
		}
		else if (lCurWeaponId == WEAPONS.HIGH_LEVEL)
		{
			tipOffset = MUZZLE_TIP_OFFSETS[WEAPONS.DEFAULT][this._fDefaultWeaponId_int - 1];
		}

		return tipOffset;
	}

	get currentDefaultWeaponId()
	{
		return this._fDefaultWeaponId_int;
	}

	playRemoveWeaponAnimation()
	{
		this._playRemoveWeaponAnimation();
	}

	showPowerUPGlow()
	{
		this._showPowerUPGlow();
	}

	shotCompleted()
	{
		this.weaponSprite && this.weaponSprite.shotCompleted();
	}

	constructor(id, aIsMaster_bl, aCurrentDefaultWeaponId, aSeatId_num, aIsSkipAnimation_bl = false)
	{
		super();

		this.id = id;
		this.isMaster = aIsMaster_bl;
		this.weaponSprite = null;
		this.shotlight = null;
		this._fSpecColorAmmo_sprt = null;
		this._fWeaponShootingEffect_spr = null;
		this._fFinishWeaponShootingEffectTimer_tmr = null;
		this._fWeaponGlowEffect_spr = null;
		this._fSeatId_num = aSeatId_num;
		this._fIsSkipAnimation_bl = aIsSkipAnimation_bl;
		this.hitArea = null;

		this._fDefaultWeaponId_int = aCurrentDefaultWeaponId ? aCurrentDefaultWeaponId : 1;

		this.once('added', () =>
		{
			this.createView();
		});

		var cursorController = APP.currentWindow.cursorController;
		this.mouseover = () => cursorController.setOverRestrictedZone(true);
		this.mouseout = () => cursorController.setOverRestrictedZone(false);
	}

	createView()
	{
		!this._fIsSkipAnimation_bl && this._animateWeaponChange();
		this.weaponSprite = this.addChild(this.getWeaponSprite());
		this._addDefaultWeaponShootingEffect();
		this.weaponSprite.scale.set(this.i_getWeaponScale(this.id));
		let anchor = this.getWeaponAnchor();
		this.weaponSprite.anchor.set(anchor.x, anchor.y);

		let lBounds_obj = this.getLocalBounds();
		this.hitArea = new PIXI.Rectangle(lBounds_obj.x, lBounds_obj.y, lBounds_obj.width, lBounds_obj.height);
		this.on('pointerdown', (e)=>e.stopPropagation());
	}

	_animateWeaponChange()
	{
		this._animateWeaponAppear();
	}

	_animateWeaponAppear()
	{
		this.scale.set(0);
		let lScale_seq = [
			{ tweens: [{ prop: "scale.x", to: 1 }, { prop: "scale.y", to: 1 }],		duration: 3 * FRAME_RATE, ease: Easing.quartic.easeIn},
		];

		Sequence.start(this, lScale_seq);
	}

	_playRemoveWeaponAnimation()
	{
		this._animateLightSweep();
		this._animateWeaponDisappear();
	}

	_animateLightSweep()
	{
		this._fSweep_sprt = this.weaponSprite.addChild(APP.library.getSprite("light_sweep"));
		this._fSweep_sprt.scale.set(0.8, 2.4);
		this._fSweep_sprt.rotation = Utils.gradToRad(70);
		let lMask_ta = this.weaponSprite.addChild(this.getWeaponAsset());
		lMask_ta.position = this._getWeaponMaskPosition();
		this._fSweep_sprt.mask = lMask_ta;

		let lStartSweepY_num = -this._fSweep_sprt.height / 2 - this._fSweep_sprt.width / 2 - 10;
		let lEndSweepY_num = -lStartSweepY_num;
		this._fSweep_sprt.position.set(0, lStartSweepY_num);

		let l_seq = [{ tweens: [{ prop: 'position.y', to: lEndSweepY_num },], duration: 10 * FRAME_RATE, onfinish: () => this._destroySweep() }];
		Sequence.start(this._fSweep_sprt, l_seq);
	}

	_animateWeaponDisappear()
	{
		let lScale_seq = [
			{ tweens: [],		duration: 2 * FRAME_RATE},
			{ tweens: [{ prop: "scale.x", to: 0 }, { prop: "scale.y", to: 0 }],		duration: 5 * FRAME_RATE, onfinish: () => this._onFinishDisappearAnimation(), ease: Easing.quartic.easeIn },
		];

		Sequence.start(this, lScale_seq);
	}

	_onFinishDisappearAnimation()
	{
		this._destroySweep();
		Sequence.destroy(Sequence.findByTarget(this));
		this.emit(Weapon.EVENT_ON_REMOVE_WEAPON_ANIMATION_FINISH);
	}

	_destroySweep()
	{
		if (this._fSweep_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fSweep_sprt));
			this._fSweep_sprt = null;
		}
	}

	_addDefaultWeaponShootingEffect()
	{
		if ((this.id == WEAPONS.DEFAULT || this.id == WEAPONS.HIGH_LEVEL) && APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			let lCurSkin_int = this._fDefaultWeaponId_int;
			switch (lCurSkin_int)
			{
				case 4:
					{
						this._fWeaponShootingEffect_spr = this.weaponSprite.addChild(this._getBulletsAnimation());
						this._fWeaponGlowEffect_spr = this.weaponSprite.addChild(APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_4/glow"));
						this._fWeaponGlowEffect_spr.position.set(0, -20);
						this._fWeaponGlowEffect_spr.blendMode = PIXI.BLEND_MODES.SCREEN;
						this._fWeaponGlowEffect_spr.alpha = 0;
						APP.currentWindow.gameField.on(GameField.DEFAULT_GUN_SHOW_FIRE, this._onDefaultWeaponShowFire, this);
					}
			}
		}
	}

	_getBulletsAnimation()
	{
		let l_spr = new Sprite();

		l_spr.textures = [APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_4/bullets_1").textures[0], APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_4/bullets_2").textures[0]];
		l_spr.animationSpeed = 30 / 60;
		l_spr.blendMode = PIXI.BLEND_MODES.SCREEN;
		l_spr.position.set(0, -6);
		l_spr.scale.set(0.2, 0.15);
		l_spr.alpha = 0;

		return l_spr;
	}

	_onDefaultWeaponShowFire(event)
	{
		if (this._fWeaponShootingEffect_spr && this._fWeaponGlowEffect_spr && event.seat == this._fSeatId_num)
		{
			this._fFinishWeaponShootingEffectTimer_tmr && this._fFinishWeaponShootingEffectTimer_tmr.destructor();
			this._fFinishWeaponShootingEffectTimer_tmr = null;
			Sequence.destroy(Sequence.findByTarget(this._fWeaponShootingEffect_spr));
			Sequence.destroy(Sequence.findByTarget(this._fWeaponGlowEffect_spr));

			this._fWeaponShootingEffect_spr.alpha = 1;
			this._fWeaponShootingEffect_spr.play();

			let lSequence_arr = [
				{ tweens: [{ prop: "alpha", to: 1 }], duration: 2 * FRAME_RATE }
			];

			Sequence.start(this._fWeaponGlowEffect_spr, lSequence_arr);

			this._fFinishWeaponShootingEffectTimer_tmr = new Timer(() => { this._stopWeaponShootingEffect(); }, 14 * FRAME_RATE);
		}
	}

	_stopWeaponShootingEffect()
	{
		this._fFinishWeaponShootingEffectTimer_tmr && this._fFinishWeaponShootingEffectTimer_tmr.destructor();
		this._fFinishWeaponShootingEffectTimer_tmr = null

		if (this._fWeaponShootingEffect_spr)
		{
			this._fWeaponShootingEffect_spr.stop();

			let lSequence_arr = [
				{ tweens: [{ prop: "alpha", to: 0 }], duration: 5 * FRAME_RATE },
			];

			Sequence.start(this._fWeaponShootingEffect_spr, lSequence_arr);
		}

		if (this._fWeaponGlowEffect_spr)
		{
			Sequence.destroy(Sequence.findByTarget(this._fWeaponGlowEffect_spr));

			let lSequence_arr = [
				{ tweens: [{ prop: "alpha", to: 0 }], duration: 5 * FRAME_RATE },
			];

			Sequence.start(this._fWeaponGlowEffect_spr, lSequence_arr);
		}
	}

	_showPowerUPGlow()
	{
		let lGlow_sprt = this.weaponSprite.addChild(this.getWeaponAsset());
		lGlow_sprt.tint = 0xEFAC43;
		lGlow_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		let anchor = this.getWeaponAnchor();
		lGlow_sprt.anchor.set(anchor.x, anchor.y);

		lGlow_sprt.fadeTo(0, 22*FRAME_RATE, undefined, () => { lGlow_sprt.destroy(); })
	}

	destroy()
	{
		this._destroySweep();

		this.id = undefined;
		this._fSeatId_num = null;
		this.hitArea = null;

		APP.currentWindow.gameField.off(GameField.DEFAULT_GUN_SHOW_FIRE, this._onDefaultWeaponShowFire, this);

		this._fFinishWeaponShootingEffectTimer_tmr && this._fFinishWeaponShootingEffectTimer_tmr.destructor();
		this._fFinishWeaponShootingEffectTimer_tmr = null;

		this.weaponSprite && this.weaponSprite.off(Gun.EVENT_ON_RELOADED, this.emit, this);
		this.weaponSprite = null;
		this.shotlight = null;
		Sequence.destroy(Sequence.findByTarget(this._fWeaponShootingEffect_spr));
		this._fWeaponShootingEffect_spr = null;
		Sequence.destroy(Sequence.findByTarget(this._fWeaponGlowEffect_spr));
		this._fWeaponGlowEffect_spr = null;

		this._fSpecColorAmmo_sprt = null;
		this._fDefaultWeaponId_int = null;
		Sequence.destroy(Sequence.findByTarget(this));
		this.removeAllListeners();

		super.destroy();
	}

	shot()
	{
		switch (this.id)
		{
			case WEAPONS.CRYOGUN:
			case WEAPONS.CRYOGUN_POWER_UP:
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
			case WEAPONS.FLAMETHROWER:
			case WEAPONS.FLAMETHROWER_POWER_UP:
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				this.weaponSprite.on(Gun.EVENT_ON_SHOT_COMPLETED, this.emit, this);
				this.weaponSprite.shot();
				break;
		}
	}

	reload()
	{
		switch (this.id)
		{
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				this.weaponSprite.on(Gun.EVENT_ON_RELOADED, this.emit, this);
				this.weaponSprite.reload();
				break;
		}
	}

	get isCharged()
	{
		return this.weaponSprite.isCharged;
	}

	hideAlternate()
	{
		switch (this.id)
		{
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				this.weaponSprite.hideAlternate();
				break;
		}
	}

	resetGun()
	{
		if (this.weaponSprite && this.weaponSprite instanceof Gun)
		{
			this.weaponSprite.once(Gun.EVENT_ON_RESET, this.emit, this);
			this.weaponSprite.reset();
		}
	}

	showShotlight()
	{
		if (this.shotlight)
		{
			return;
		}
		let lShotlightAsset_str, lAnchor_obj;

		switch (this.id)
		{
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
				lShotlightAsset_str = "weapons/RicochetGun/RicochetGunShotlight";
				lAnchor_obj = {x: 0.53, y: 1.09};
				break;
		}

		if (lShotlightAsset_str)
		{
			this.shotlight = this.addChild(APP.library.getSpriteFromAtlas(lShotlightAsset_str));
			if (lAnchor_obj)
			{
				this.shotlight.anchor.set(lAnchor_obj.x, lAnchor_obj.y);
			}
		}
	}

	hideShotlight()
	{
		if (this.shotlight)
		{
			this.shotlight.destroy();
			this.shotlight = null;
		}
	}

	i_getWeaponScale(aWeaponId)
	{
		let lWeaponId = aWeaponId ? aWeaponId : this.id;
		let lScaleValue = 1;
		switch (lWeaponId)
		{
			case WEAPONS.DEFAULT:
				lScaleValue = 1;
				break;
			case WEAPONS.FLAMETHROWER:
			case WEAPONS.FLAMETHROWER_POWER_UP:
				lScaleValue = 1.15;
				break;
			case WEAPONS.ARTILLERYSTRIKE:
			case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
				lScaleValue = 1.15;
				break;
			case WEAPONS.CRYOGUN:
			case WEAPONS.CRYOGUN_POWER_UP:
				lScaleValue = 1.15;
				break;
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
				lScaleValue = 1.15;
				break;
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				lScaleValue = 1.15;
				break;
			default:
				lScaleValue = 1;
				break;
		}
		return lScaleValue;
	}

	getWeaponAnchor()
	{
		let anchor = { x: 0.5, y: 0.585 };
		switch (this.id)
		{
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				anchor = { x: 0.428571, y: 0.474048 };
				break;
			case WEAPONS.ARTILLERYSTRIKE:
			case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
				anchor = { x: 29 / 79, y: 0.45 };
				break;
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
				anchor = {x: 0.421053, y: 0.433692};
				break;
		}
		return anchor;
	}

	getWeaponSprite()
	{
		let lAssetName_str = "weapons/DefaultGun/turret_1/turret";
		switch (this.id)
		{
			case WEAPONS.DEFAULT:
			case WEAPONS.HIGH_LEVEL:
				lAssetName_str = "weapons/DefaultGun/turret_" + this._fDefaultWeaponId_int + "/turret";
				return APP.library.getSpriteFromAtlas(lAssetName_str);
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				return new PlazmaGun(this.isMaster);
			case WEAPONS.CRYOGUN:
			case WEAPONS.CRYOGUN_POWER_UP:
				return new Cryogun(this.isMaster);
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
				return new RicochetLaserGun();
			case WEAPONS.FLAMETHROWER:
			case WEAPONS.FLAMETHROWER_POWER_UP:
				return new FlameThrowerGun();
			case WEAPONS.ARTILLERYSTRIKE:
			case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
				lAssetName_str = "weapons/ArtilleryStrike/artillery_grenade_tossed";
				return APP.library.getSpriteFromAtlas(lAssetName_str);
				break;
		}
		return APP.library.getSprite(lAssetName_str);
	}

	getWeaponAsset()
	{
		let lAssetName_str = "weapons/DefaultGun/turret_1/turret";
		switch (this.id)
		{
			case WEAPONS.DEFAULT:
			case WEAPONS.HIGH_LEVEL:
				lAssetName_str = "weapons/DefaultGun/turret_" + this._fDefaultWeaponId_int + "/turret";
				return APP.library.getSpriteFromAtlas(lAssetName_str);
				;
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				lAssetName_str = "weapons/InstantKill/gun_off";
				return APP.library.getSpriteFromAtlas(lAssetName_str);
				break;
			case WEAPONS.CRYOGUN:
			case WEAPONS.CRYOGUN_POWER_UP:
				lAssetName_str = "weapons/Cryogun/cryogun_frozen";
				return APP.library.getSpriteFromAtlas(lAssetName_str);
				break;
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
				lAssetName_str = "weapons/RicochetGun/RicocjetGun";
				return APP.library.getSpriteFromAtlas(lAssetName_str);
				break;
			case WEAPONS.FLAMETHROWER:
			case WEAPONS.FLAMETHROWER_POWER_UP:
				lAssetName_str = "weapons/FlameThrower/flamethrower";
				return APP.library.getSpriteFromAtlas(lAssetName_str);
				break;
			case WEAPONS.ARTILLERYSTRIKE:
			case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
				lAssetName_str = "weapons/ArtilleryStrike/artillery_grenade_tossed";
				return APP.library.getSpriteFromAtlas(lAssetName_str);
				break;
		}
		return APP.library.getSprite(lAssetName_str);
	}

	_getWeaponMaskPosition()
	{
		let l_obj = { x: 0, y: 0 };
		switch (this.id)
		{
			case WEAPONS.DEFAULT:
				l_obj = { x: 0, y: -8 };
				break;
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				l_obj = { x: 4, y: 4 };
				break;
			case WEAPONS.CRYOGUN:
			case WEAPONS.CRYOGUN_POWER_UP:
				l_obj = { x: 0, y: 0 };
				break;
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
				l_obj = { x: 0, y: 0 };
				break;
			case WEAPONS.FLAMETHROWER:
			case WEAPONS.FLAMETHROWER_POWER_UP:
				l_obj = { x: 0, y: -15 };
				break;
			case WEAPONS.ARTILLERYSTRIKE:
			case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
				l_obj = { x: 5, y: 3 };
				break;
		}
		return l_obj;
	}
}

export default Weapon;