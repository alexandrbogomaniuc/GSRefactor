import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { WEAPONS } from '../../../../shared/src/CommonConstants';
import PlayerSpotWeaponsBackFithWeapon from './PlayerSpotWeaponsBackFithWeapon';

class PlayerSpotWeaponsBack extends Sprite
{
	update(aWeaponid_num, aCurrentDefaultWeaponId_num)
	{
		this._update(aWeaponid_num, aCurrentDefaultWeaponId_num);
	}

	hideGlow()
	{
		this._hideGlow();
	}

	showGlow()
	{
		this._showGlow();
	}

	constructor(aPlayer_obj)
	{
		super();

		this._fPlayer_obj = aPlayer_obj;
		this._fWeaponBack_spr = null;
	}

	_getWeaponSpotBack(aWeaponid_num, aCurrentDefaultWeaponId_num)
	{
		let lSpecViewType_int = undefined;
		let lCurrentDefaultWeaponId_num = aCurrentDefaultWeaponId_num ? aCurrentDefaultWeaponId_num: 1;
		let lAssetName_str = "player_spot/weapon_spot_back";

		if (aWeaponid_num == WEAPONS.DEFAULT || aWeaponid_num == WEAPONS.HIGH_LEVEL)
		{
			lSpecViewType_int = lCurrentDefaultWeaponId_num;
		}
		else if (aWeaponid_num == WEAPONS.HIGH_LEVEL)
		{
			lSpecViewType_int = APP.playerController.info.getTurretSkinId(APP.playerController.info.betLevel);
		}

		if (lSpecViewType_int !== undefined)
		{
			switch(lSpecViewType_int)
			{
				case 1:
					lAssetName_str = "weapons/DefaultGun/turret_1/weapon_spot_back";
				break
				case 2:
					lAssetName_str = "weapons/DefaultGun/turret_2/weapon_spot_back";
				break
				case 3:
					lAssetName_str = "weapons/DefaultGun/turret_3/weapon_spot_back";
				break
				case 4:
					lAssetName_str = "weapons/DefaultGun/turret_4/weapon_spot_back";
				break
				case 5:
				return new PlayerSpotWeaponsBackFithWeapon(this._fPlayer_obj);
				default:
					throw new Error(`Wrong default weapon id ${lCurrentDefaultWeaponId_num} !`);
			}
		}
	
		return APP.library.getSpriteFromAtlas(lAssetName_str);
		
	}

	_update(aWeaponid_num, aCurrentDefaultWeaponId_num)
	{
		this._fWeaponBack_spr && this._fWeaponBack_spr.destroy();
		this._fWeaponBack_spr = this.addChild(this._getWeaponSpotBack(aWeaponid_num, aCurrentDefaultWeaponId_num));
	}

	_hideGlow()
	{
		this._fWeaponBack_spr && this._fWeaponBack_spr.hideGlow && this._fWeaponBack_spr.hideGlow();
	}

	_showGlow()
	{
		this._fWeaponBack_spr && this._fWeaponBack_spr.showGlow && this._fWeaponBack_spr.showGlow();
	}

	destroy()
	{
		super.destroy();

		this._fPlayer_obj = null;
		this._fWeaponBack_spr = null;
	}

}

export default PlayerSpotWeaponsBack