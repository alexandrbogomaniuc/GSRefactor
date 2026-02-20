import SimpleUIInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo'
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class FireSettingsInfo extends SimpleUIInfo
{
	constructor()
	{
		super();

		if(APP.playerController.info.isDisableAutofiring)
		{
			this._fAutoFire_bln = false;
		}
		else
		{
			this._fAutoFire_bln = true;
		}
	
		this._fLockOnTarget_bln = true;
		this._fTargetPriority_int = 3;
		this._fFireSpeed_int = 3;
	}

	get targetPriority()
	{
		return this._fTargetPriority_int;
	}

	set targetPriority(aVal_int)
	{
		aVal_int = +aVal_int;
		if (isNaN(aVal_int) || aVal_int < 1 || aVal_int > 3) return;

		this._fTargetPriority_int = aVal_int;
	}

	get lockOnTarget()
	{
		return this._fLockOnTarget_bln;
	}

	set lockOnTarget(aVal_bln)
	{
		this._fLockOnTarget_bln = !!aVal_bln;
	}

	get autoFire()
	{
		return this._fAutoFire_bln;
	}

	set autoFire(aVal_bln)
	{
		this._fAutoFire_bln = !!aVal_bln;
	}

	get fireSpeed()
	{
		return this._fFireSpeed_int;
	}

	set fireSpeed(aVal_int)
	{
		aVal_int = +aVal_int;
		if (isNaN(aVal_int) || aVal_int < 1 || aVal_int > 3) return;

		this._fFireSpeed_int = aVal_int;
	}

	destroy()
	{
		super.destroy();

		this._fLockOnTarget_bln = null;
		this._fTargetPriority_int = null;
		this._fAutoFire_bln = null;
		this._fFireSpeed_int = null;
	}
}

export default FireSettingsInfo;