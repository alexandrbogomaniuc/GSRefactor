import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

import Turret1FireEffect from './Turret1FireEffect';
import Turret2FireEffect from './Turret2FireEffect';
import Turret3FireEffect from './Turret3FireEffect';
import Turret4FireEffect from './Turret4FireEffect';
import Turret5FireEffect from './Turret5FireEffect';
import { COOP_FIRE_FX_ALPHA } from '../../../../../shared/src/CommonConstants';

class DefaultGunFireEffect extends Sprite 
{
	static get EVENT_ON_ANIMATION_COMPLETED() {return "EVENT_ON_ANIMATION_COMPLETED";}

	constructor(aDefaultWeaponId_num, aWeaponOffsetYFiringContinuously_int = 0, aIsMaster_bl)
	{
		super();

		this._fDefaultWeaponId_num = aDefaultWeaponId_num;
		this._fFireAnimation_spr = null;
		this._fWeaponOffsetYFiringContinuously_int = aWeaponOffsetYFiringContinuously_int;
		this._fIsMasterEffect_bl = aIsMaster_bl;


		this.once('added', () => {this._onAdded();});
	}

	_onAdded()
	{
		this._fFireAnimation_spr = this.addChild(this._getFireEffectInstance());
		this._fFireAnimation_spr.position = this._getFireEffectPosition();
		this._fFireAnimation_spr.on(this._getFireEffectCompletedEventName(), this._onAnimationCompleted, this);
		if (!this._fIsMasterEffect_bl)
		{
			this._fFireAnimation_spr.alpha = COOP_FIRE_FX_ALPHA;
		}
	}

	_getFireEffectInstance()
	{
		let lFireEffect_spr = null;
		switch(this._fDefaultWeaponId_num)
		{
			case 1:
				lFireEffect_spr = new Turret1FireEffect();
				break;
			case 2:
				lFireEffect_spr = new Turret2FireEffect();
				break;
			case 3:
				lFireEffect_spr = new Turret3FireEffect();
				break;
			case 4:
				lFireEffect_spr = new Turret4FireEffect();
				break;
			case 5:
				lFireEffect_spr = new Turret5FireEffect();
				break;
			default:
				throw new Error(`Wrong default weapon id ${this._fDefaultWeaponId_num} !`);
		}

		return lFireEffect_spr;
	}

	_getFireEffectPosition()
	{
		let lFireEffectPosition_obj = {x: 0, y: 0};
		switch(this._fDefaultWeaponId_num)
		{
			case 1:
				lFireEffectPosition_obj = {x: 0, y: 170 + this._fWeaponOffsetYFiringContinuously_int};
				break;
			case 2:
				lFireEffectPosition_obj = {x: 0, y: 170 + this._fWeaponOffsetYFiringContinuously_int};
				break;
			case 3:
				lFireEffectPosition_obj = {x: 0, y: 170 + this._fWeaponOffsetYFiringContinuously_int};
				break;
			case 4:
				lFireEffectPosition_obj = {x: 0, y: 165 + this._fWeaponOffsetYFiringContinuously_int};
				break;
			case 5:
				lFireEffectPosition_obj = {x: 0, y: 170 + this._fWeaponOffsetYFiringContinuously_int};
				break;
			default:
				throw new Error(`Wrong default weapon id ${this._fDefaultWeaponId_num} !`);
		}

		return lFireEffectPosition_obj;
	}

	_getFireEffectCompletedEventName()
	{
		let lFireEffectCompletedEventName_str = "";
		switch(this._fDefaultWeaponId_num)
		{
			case 1:
				lFireEffectCompletedEventName_str = Turret1FireEffect.EVENT_ON_ANIMATION_COMPLETED;
				break;
			case 2:
				lFireEffectCompletedEventName_str = Turret2FireEffect.EVENT_ON_ANIMATION_COMPLETED;
				break;
			case 3:
				lFireEffectCompletedEventName_str = Turret3FireEffect.EVENT_ON_ANIMATION_COMPLETED;
				break;
			case 4:
				lFireEffectCompletedEventName_str = Turret4FireEffect.EVENT_ON_ANIMATION_COMPLETED;
				break;
			case 5:
				lFireEffectCompletedEventName_str = Turret5FireEffect.EVENT_ON_ANIMATION_COMPLETED;
				break;
			default:
				throw new Error(`Wrong default weapon id ${this._fDefaultWeaponId_num} !`);
		}

		return lFireEffectCompletedEventName_str;
	}

	_onAnimationCompleted()
	{	
		this.emit(DefaultGunFireEffect.EVENT_ON_ANIMATION_COMPLETED);
	}

	destroy()
	{
		super.destroy();

		this._fDefaultWeaponId_num = null;
		this._fFireAnimation_spr = null;
		this._fWeaponOffsetYFiringContinuously_int = null;
		this._fIsMasterEffect_bl = undefined;
	}
}

export default DefaultGunFireEffect;