import SimpleInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';
import { WEAPONS } from '../../../../../shared/src/CommonConstants';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

const KEY_WEAPON_ID = 'id';
const KEY_WEAPON_SHOTS = 'shots';
const DEFAULT_WEAPON_ID_DEFAULT = 1;

class WeaponsInfo extends SimpleInfo {
	constructor()
	{
		super();

		this._fWeapons_obj_arr = [];
		this._fCurrentWeaponId_int = undefined;
		this._fCurrentDefaultWeaponId_int = undefined;
		this._fDefaultAmmo_num = 0;
		this._fAwardedWeapons_obj_arr = [];

		this._fIsFreeWeaponsQueueActivated_bl = false;
	}

	set isFreeWeaponsQueueActivated(aValue_bl)
	{
		this._fIsFreeWeaponsQueueActivated_bl = aValue_bl;
	}

	get isFreeWeaponsQueueActivated()
	{
		return this._fIsFreeWeaponsQueueActivated_bl;
	}

	set weapons(aWeapons_obj_arr)
	{
		this._fWeapons_obj_arr = aWeapons_obj_arr;
	}

	get weapons()
	{
		return this._fWeapons_obj_arr;
	}

	get autoEquipFreeSW()
	{
		return true; //so far it is always true, in the future this may change
	}

	//AWARDED/BOUGHT weapons...
	i_awardWeapon(aWeaponId_int, aShots_int)
	{
		let lAwardedWeapon_obj = {};
		lAwardedWeapon_obj[KEY_WEAPON_ID] = aWeaponId_int;
		lAwardedWeapon_obj[KEY_WEAPON_SHOTS] = aShots_int;
		this._fAwardedWeapons_obj_arr.push(lAwardedWeapon_obj);
	}

	i_extractAwardedWeapon()
	{
		if (this._fAwardedWeapons_obj_arr.length > 0)
		{
			return this._fAwardedWeapons_obj_arr.shift();
		}
		return null;
	}

	i_clearAwardedWeapons()
	{
		this._fAwardedWeapons_obj_arr = [];
	}

	i_clearWeapons()
	{
		this._fWeapons_obj_arr = [];
	}
	//...AWARDED/BOUGHT weapons

	i_getNextWeaponIdToShoot()
	{
		//BATTLEGROUND FS QUEUE SPECIAL ORDER...
		if(
			APP.isBattlegroundGame &&
			APP.gameScreen &&
			APP.gameScreen.gameField &&
			APP.gameScreen.gameField.spot
			)
		{
			let lSuggestedBattlegroundFreeWeaponId_int = APP.gameScreen.gameField.spot.getNextSuggestedBattlegroundFreeWeaponId();

			if(lSuggestedBattlegroundFreeWeaponId_int !== undefined)
			{
				for (let lWeapon_obj of this._fWeapons_obj_arr)
				{
					if (
						lWeapon_obj.id === lSuggestedBattlegroundFreeWeaponId_int &&
						lWeapon_obj.shots > 0
						)
					{
						return lSuggestedBattlegroundFreeWeaponId_int;
					}
				}
			}
		}
		//...BATTLEGROUND FS QUEUE SPECIAL ORDER


		for (let lWeapon_obj of this._fWeapons_obj_arr)
		{
			if (lWeapon_obj.shots > 0)
			{
				return lWeapon_obj[KEY_WEAPON_ID];
			}
		}
		return WEAPONS.DEFAULT;
	}

	i_getWeapon(aWeaponId_int)
	{
		for (let lWeapon_obj of this._fWeapons_obj_arr)
		{
			if (lWeapon_obj[KEY_WEAPON_ID] == aWeaponId_int)
			{
				return lWeapon_obj;
			}
		}
		return null;
	}

	i_addWeapon(aWeaponId_int, aShots_int)
	{
		let lWeapon_obj = this.i_getWeapon(aWeaponId_int);
		let lPrevWeaponShots_int = 0;
		if (!lWeapon_obj)
		{
			//throw new Error(`No special weapon with id ${this.currentWeaponId} found!`);
			lWeapon_obj = {};
			lWeapon_obj[KEY_WEAPON_ID] = aWeaponId_int;
			lWeapon_obj[KEY_WEAPON_SHOTS] = 0;
			
			if (aWeaponId_int == WEAPONS.HIGH_LEVEL)
			{
				this._fWeapons_obj_arr.unshift(lWeapon_obj);
			}
			else
			{
				this._fWeapons_obj_arr.push(lWeapon_obj);
			}
		}
		lPrevWeaponShots_int = lWeapon_obj[KEY_WEAPON_SHOTS];

		lWeapon_obj[KEY_WEAPON_SHOTS] += aShots_int;

		if (APP.isBattlegroundGame)
		{
			if (lPrevWeaponShots_int == 0 || aWeaponId_int == WEAPONS.HIGH_LEVEL)
			{// newly awarded or HIGH_LEVEL weapon
				let lWeaponIndex_int = this._fWeapons_obj_arr.indexOf(lWeapon_obj);
				if (lWeaponIndex_int > 0)
				{
					this._fWeapons_obj_arr.splice(lWeaponIndex_int, 1);

					this._fWeapons_obj_arr.unshift(lWeapon_obj);
				}
			}
		}
	}

	i_getWeaponShots(aWeaponId_int, aThrowException_bl = true)
	{
		let lWeapon_obj = this.i_getWeapon(aWeaponId_int);
		if (!lWeapon_obj)
		{
			if (aThrowException_bl)
			{
				throw new Error(`No special weapon with id ${aWeaponId_int} found!`);
			}
			else
			{
				return 0;
			}
		}
		return lWeapon_obj[KEY_WEAPON_SHOTS];
	}

	i_getWeaponFreeShots(aWeaponId_int)
	{
		if (aWeaponId_int === WEAPONS.DEFAULT)
		{
			return 0;
		}
		return this.i_getWeaponShots(aWeaponId_int, false);
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
		return this.i_getWeaponShotPrice(this.currentWeaponId);
	}

	i_getWeaponShotPriceConvertedIntoDefaultAmmo(aWeaponId_int)
	{
		const lBetLevel_int =  APP.playerController.info.betLevel;
		if (aWeaponId_int === WEAPONS.DEFAULT)
		{
			return lBetLevel_int;
		}
		const lCostMultiplier_num = APP.playerController.info.getWeaponPaidCostMultiplier(aWeaponId_int);

		return lBetLevel_int * lCostMultiplier_num;
	}

	i_getCurrentWeaponShotPriceConvertedIntoDefaultAmmo()
	{
		return this.i_getWeaponShotPriceConvertedIntoDefaultAmmo(this.currentWeaponId);
	}

	i_updateWeaponShots(aWeaponId_int, aShots_int)
	{
		if (aWeaponId_int == WEAPONS.DEFAULT)
		{
			this.realAmmo = aShots_int;
		}

		for (let lWeapon_obj of this._fWeapons_obj_arr)
		{
			if (lWeapon_obj[KEY_WEAPON_ID] == aWeaponId_int)
			{
				lWeapon_obj[KEY_WEAPON_SHOTS] = aShots_int;
				return true;
			}
		}
		return false
	}

	i_revertAmmoBack(aWeaponId_int, aRevertAmmoAmount_int)
	{
		if (aWeaponId_int == WEAPONS.DEFAULT)
		{
			if (APP.isBattlegroundGame)
			{
				// BTG game has unlimitied ammo - no need to decrease ammo for BTG game
			}
			else
			{
				this.realAmmo += aRevertAmmoAmount_int;
			}
		}
		else
		{
			this.i_updateWeaponShots(aWeaponId_int, this.i_getWeaponShots(aWeaponId_int) + 1);
		}
	}

	i_decreaseAmmo(aWeaponId_int, aDecreaseAmmoAmount_int)
	{
		if (aWeaponId_int == WEAPONS.DEFAULT)
		{
			if (APP.isBattlegroundGame)
			{
				// BTG game has unlimitied ammo - no need to decrease ammo for BTG game
			}
			else
			{
				this.realAmmo -= aDecreaseAmmoAmount_int;
			}
		}
		else
		{
			this.i_updateWeaponShots(aWeaponId_int, this.i_getWeaponShots(aWeaponId_int) - aDecreaseAmmoAmount_int);
		}
	}

	i_zeroDefaultShots()
	{
		this.realAmmo = 0;
	}

	set currentWeaponId(aWeaponId_int)
	{
		this._fCurrentWeaponId_int = aWeaponId_int;

		if (aWeaponId_int !== WEAPONS.DEFAULT && this.remainingSWShots > 0)
		{
			this.isFreeWeaponsQueueActivated = true;
		}
		else
		{
			this.isFreeWeaponsQueueActivated = false;
		}
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
		return this._fCurrentDefaultWeaponId_int ? this._fCurrentDefaultWeaponId_int: DEFAULT_WEAPON_ID_DEFAULT;
	}

	// ammo amount, that can be used for shots (integer)
	get ammo()
	{
		let lRealAmmo_num = Number(this._fDefaultAmmo_num.toFixed(2));
		return Math.floor(lRealAmmo_num);
	}

	// real ammo amount, can be double (win transferred to ammo leads to double ammo)
	get realAmmo()
	{
		return this._fDefaultAmmo_num;
	}

	set realAmmo(aValue_num)
	{
		this._fDefaultAmmo_num = aValue_num;
	}

	get remainingSWShots()
	{
		let lSpecialWeaponId_int = this.currentWeaponId;
		if (this.currentWeaponId == WEAPONS.DEFAULT || this.currentWeaponId === undefined)
		{
			return 0;
		}

		return this.i_getWeaponShots(lSpecialWeaponId_int, false /*do not throw exception*/);
	}

	get isAnyFreeSpecialWeaponExist()
	{
		for (let prop in WEAPONS)
		{
			let weaponId = WEAPONS[prop];
			if (weaponId !== WEAPONS.DEFAULT
				&& this.i_getWeaponFreeShots(weaponId) > 0)
			{
				return true;
			}
		}
		return false;
	}

	destroy()
	{
		if (this._fWeapons_obj_arr)
		{
			while (this._fWeapons_obj_arr.length)
			{
				delete this._fWeapons_obj_arr.pop();
			}
			this._fWeapons_obj_arr = null;
		}

		this._fCurrentWeaponId_int = undefined;
		this._fCurrentDefaultWeaponId_int = undefined;
		this._fDefaultAmmo_num = undefined;

		if (this._fAwardedWeapons_obj_arr)
		{
			while (this._fAwardedWeapons_obj_arr.length)
			{
				delete this._fAwardedWeapons_obj_arr.pop();
			}
			this._fAwardedWeapons_obj_arr = null;
		}

		super.destroy();
	}
}

export default WeaponsInfo;