import SimpleUIInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';

class WeaponSpotInfo extends SimpleUIInfo {
	constructor()
	{
		super();
		this._fCurrentWeaponId_int = undefined;
		this._fCurrentDefaultWeaponId_int = undefined;
		this._fIsMaster_bl = undefined;
	}

	set currentWeaponId(aValue_int)
	{
		this._fCurrentWeaponId_int = aValue_int;
	}

	get currentWeaponId()
	{
		return this._fCurrentWeaponId_int;
	}

	set currentDefaultWeaponId(aWeaponId_int)
	{
		this._fCurrentDefaultWeaponId_int = aWeaponId_int;
	}

	get currentDefaultWeaponId()
	{
		return this._fCurrentDefaultWeaponId_int;
	}

	set isMaster(aValue_bl)
	{
		this._fIsMaster_bl = aValue_bl;
	}

	get isMaster()
	{
		return this._fIsMaster_bl;
	}

	destroy()
	{
		this._fCurrentWeaponId_int = undefined;
		this._fCurrentDefaultWeaponId_int = undefined;
		this._fIsMaster_bl = undefined;
		
		super.destroy();
	}
}

export default WeaponSpotInfo;