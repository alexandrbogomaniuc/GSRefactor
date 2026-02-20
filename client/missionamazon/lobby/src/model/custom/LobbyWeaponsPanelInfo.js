import SimpleInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';
import { WEAPONS } from '../../../../shared/src/CommonConstants';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class LobbyWeaponsPanelInfo extends SimpleInfo {

	constructor()
	{
		super();

		this._fWeaponsAmmo_obj = {};
		this._fSelectedWeaponId_int = 0;
		this._fIsFreeWeaponsQueueActivated_bl = null;
		this._fIsRoundStatePlay_bln = false;
	}

	get isRoundStatePlay()
	{
		return this._fIsRoundStatePlay_bln;
	}

	set isRoundStatePlay(aVal_bln)
	{
		this._fIsRoundStatePlay_bln = aVal_bln;
	}

	get selectedId()
	{
		return this._fSelectedWeaponId_int;
	}

	set selectedId(aVal_int)
	{
		this._fSelectedWeaponId_int = aVal_int;
	}

	get allWeaponsAmmo()
	{
		return this._fWeaponsAmmo_obj;
	}

	set allWeaponsAmmo(aWeapons_obj)
	{
		this._fWeaponsAmmo_obj = aWeapons_obj;
	}

	get isFreeWeaponsQueueActivated()
	{
		return this._fIsFreeWeaponsQueueActivated_bl;
	}

	set isFreeWeaponsQueueActivated(aValue_bl)
	{
		this._fIsFreeWeaponsQueueActivated_bl = aValue_bl;
	}

	get isAnyFreeSpecialWeaponExist()
	{
		for (let prop in WEAPONS)
		{
			let weaponId = WEAPONS[prop];
			if (weaponId !== WEAPONS.DEFAULT
				&& this._fWeaponsAmmo_obj[weaponId] > 0)
			{
				return true;
			}
		}
		return false;
	}

	get isCurrentWeaponSpecialAndFree()
	{
		return ( this.selectedId != WEAPONS.DEFAULT
					&& this.selectedId >= 0
					&& this._fWeaponsAmmo_obj[this.selectedId] > 0 );
	}

	get currentWeaponShots()
	{
		if (this.selectedId != WEAPONS.DEFAULT && this.selectedId >= 0)
			return this.allWeaponsAmmo[this.selectedId];
		return undefined;
	}

	get totalFreeWeaponsShot()
	{
		let lTotalCounter_int = 0;
		for (let weaponId in this.allWeaponsAmmo)
		{
			if (weaponId != WEAPONS.DEFAULT || APP.FRBController.info.isActivated)
			{
				let lFreeShots_int = this.allWeaponsAmmo[weaponId];
				lTotalCounter_int += lFreeShots_int > 0 ? lFreeShots_int : 0;
			}
		}

		return lTotalCounter_int;
	}

	get isPaidWeaponSelected()
	{
		return (this.selectedId === WEAPONS.DEFAULT) || !(this.currentWeaponShots > 0);
	}

	get isPaidSpecialWeaponSelected()
	{
		return !(this.currentWeaponShots > 0) && (this.selectedId !== WEAPONS.DEFAULT);
	}

	i_getWeaponShotPrice(aWeaponId_int)
	{
		const lCurrentStake_num = APP.playerController.info.currentStake;
		const lBetLevel_int =  APP.playerController.info.betLevel;
		if (aWeaponId_int === WEAPONS.DEFAULT)
		{
			return lCurrentStake_num * lBetLevel_int;
		}
		const lCostMultiplier_num = APP.playerController.info.getWeaponPaidCostMultiplier(aWeaponId_int);
		return lCurrentStake_num * lBetLevel_int * lCostMultiplier_num;

	}

	i_getCurrentWeaponShotPrice()
	{
		return this.i_getWeaponShotPrice(this.selectedId);
	}

	destroy()
	{
		super.destroy();

		this._fWeaponsAmmo_obj = null;
		this._fSelectedWeaponId_int = null;
		this._fIsFreeWeaponsQueueActivated_bl = null;
		this._fIsRoundStatePlay_bln = null;

	}
}

export default LobbyWeaponsPanelInfo;