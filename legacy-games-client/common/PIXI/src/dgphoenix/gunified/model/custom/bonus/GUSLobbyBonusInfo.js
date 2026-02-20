import BonusInfo from '../../../../unified/model/custom/bonus/BonusInfo';
import { APP } from '../../../../unified/controller/main/globals';

class GUSLobbyBonusInfo extends BonusInfo
{
	constructor()
	{
		super();

		this._fMessageType_str = undefined;
		this._fWinSum_num = undefined;
		this._fWeapons_obj_arr = [];
		this._fIsLobbyRestartRequired_bl = false;
		this._fIsRoomRestartRequired_bl = false; // if user wants to re-enter room in real money mode
		this._fIsCompletionInProgress_bl = false;
		this._fRealWinSum_num = undefined;
		this._fBonusEndedDuringGameLoad_bln = false;
	}

	i_clearAll()
	{
		super.i_clearAll();

		this._fMessageType_str = undefined;
		this._fWinSum_num = undefined;
		this._fWeapons_obj_arr = [];
		this._fIsLobbyRestartRequired_bl = false;
		this._fIsRoomRestartRequired_bl = false;
		this._fIsCompletionInProgress_bl = false;
		this._fRealWinSum_num = undefined;
		this._fBonusEndedDuringGameLoad_bln = false;
	}

	set realWinSum(aVal_num)
	{
		this._fRealWinSum_num = aVal_num;
	}

	get realWinSum()
	{
		return this._fRealWinSum_num;
	}

	set messageType(aValue_str)
	{
		this._fMessageType_str = aValue_str;
	}

	get messageType()
	{
		return this._fMessageType_str;
	}

	set weapons(aWeapons_obj_arr)
	{
		this._fWeapons_obj_arr = aWeapons_obj_arr;
	}

	get weapons()
	{
		return this._fWeapons_obj_arr;
	}

	get isLobbyRestartRequired()
	{
		return this._fIsLobbyRestartRequired_bl;
	}

	set isLobbyRestartRequired(aValue_bl)
	{
		this._fIsLobbyRestartRequired_bl = aValue_bl;
	}

	get isRoomRestartRequired()
	{
		return this._fIsRoomRestartRequired_bl;
	}

	set isRoomRestartRequired(aValue_bl)
	{
		this._fIsRoomRestartRequired_bl = aValue_bl;
	}

	get isRoomRestartPossible()
	{
		return !APP.lobbyScreen.visible && !this.nextModeFRB && (this.nextRoomId > -1);
	}

	set isCompletionInProgress(aValue_bl)
	{
		this._fIsCompletionInProgress_bl = aValue_bl;
	}

	get isCompletionInProgress()
	{
		return this._fIsCompletionInProgress_bl;
	}

	i_getWeapon(aWeaponId_int)
	{
		for (let lWeapon_obj of this._fWeapons_obj_arr)
		{
			if (lWeapon_obj.id == aWeaponId_int)
			{
				return lWeapon_obj;
			}
		}
		return null;
	}

	i_getWeaponShots(aWeaponId_int)
	{
		let lWeapon_obj = this.i_getWeapon(aWeaponId_int);
		if (!lWeapon_obj)
		{
			return 0;
		}
		return lWeapon_obj.shots;
	}

	get allWeaponsFreeShots()
	{
		let lTotalShotsCount_int = this.currentFreeShotsCount;
		for (let lWeapon_obj of this._fWeapons_obj_arr)
		{
			lTotalShotsCount_int += lWeapon_obj.shots;
		}

		return lTotalShotsCount_int;
	}

	set bonusEndedDuringGameLoad(aVal_bln)
	{
		this._fBonusEndedDuringGameLoad_bln = aVal_bln;
	}

	get bonusEndedDuringGameLoad()
	{
		return this._fBonusEndedDuringGameLoad_bln;
	}

	get keepBonusSW()
	{
		return false;
	}
}

export default GUSLobbyBonusInfo