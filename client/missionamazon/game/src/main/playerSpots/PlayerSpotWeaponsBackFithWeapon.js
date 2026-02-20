import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';
import GameField from '../../main/GameField';

class PlayerSpotWeaponsBackFithWeapon extends Sprite
{
	showGlow()
	{
		this._showGlow();
	}

	hideGlow()
	{
		this._hideGlow();
	}

	constructor(aPlayer_obj)
	{
		super();

		this._fPlayer_obj = aPlayer_obj;
		this._fGlow_spr = null;
		this._fShotGlow_spr = null;

		this._init();
	}

	_init()
	{
		this._addBack();
		this._addGlow();

		APP.profilingController.info.isVfxProfileValueMediumOrGreater && APP.currentWindow.gameField.on(GameField.DEFAULT_GUN_SHOW_FIRE, this._onDefaultWeaponShowFire, this);
	}

	_addBack()
	{
		let lBack_spr = this.addChild(APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_5/weapon_spot_back"));
	}

	_addGlow()
	{
		this._fGlow_spr = this.addChild(APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_5/weapon_spot_back_glow"));
		this._fGlow_spr.blendMode = PIXI.BLEND_MODES.ADD;
		this._fGlow_spr.alpha = 0.6;
		APP.profilingController.info.isVfxProfileValueMediumOrGreater && this._startPulsation();

		this._fShotGlow_spr = this.addChild(APP.library.getSpriteFromAtlas("weapons/DefaultGun/turret_5/weapon_spot_back_glow"));
		this._fShotGlow_spr.blendMode = PIXI.BLEND_MODES.ADD;
		this._fShotGlow_spr.alpha = 0;
	}

	_startPulsation()
	{
		let lSequence_arr = [
			{tweens: [{ prop: "alpha", to: 1 }],		duration: 12 * FRAME_RATE},
			{tweens: [{ prop: "alpha", to: 0.6 }],		duration: 12 * FRAME_RATE, onfinish: (e) => {this._startPulsation()}}
		];

		Sequence.start(this._fGlow_spr, lSequence_arr);
	}

	_onDefaultWeaponShowFire(event)
	{
		Sequence.destroy(Sequence.findByTarget(this._fShotGlow_spr));

		if(event.seat == this._fPlayer_obj.seatId)
		{
			let lSequence_arr = [
				{tweens: [{ prop: "alpha", to: 1 }],		duration: 1 * FRAME_RATE},
				{tweens: [{ prop: "alpha", to: 0 }],		duration: 5 * FRAME_RATE}
			];
	
			Sequence.start(this._fShotGlow_spr, lSequence_arr);
		}
	}

	_hideGlow()
	{
		this._fGlow_spr && (this._fGlow_spr.visible = false);
		this._fShotGlow_spr && (this._fShotGlow_spr.visible = false);
	}

	_showGlow()
	{
		this._fGlow_spr && (this._fGlow_spr.visible = true);
		this._fShotGlow_spr && (this._fShotGlow_spr.visible = true);
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this._fGlow_spr));
		Sequence.destroy(Sequence.findByTarget(this._fShotGlow_spr));

		super.destroy();

		APP.currentWindow.gameField.off(GameField.DEFAULT_GUN_SHOW_FIRE, this._onDefaultWeaponShowFire, this);

		this._fPlayer_obj = null;
		this._fGlow_spr = null;
		this._fShotGlow_spr = null;
	}
}

export default PlayerSpotWeaponsBackFithWeapon