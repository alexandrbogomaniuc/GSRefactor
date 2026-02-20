import SimpleInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/base/SimpleInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class FRBInfo extends SimpleInfo {

	static get MESSAGE_FRB_EXPIRED() 		{ return 'MESSAGE_FRB_EXPIRED'; }
	static get MESSAGE_FRB_CANCELLED()		{ return 'MESSAGE_FRB_CANCELLED'; }
	static get MESSAGE_FRB_FINISHED()		{ return 'MESSAGE_FRB_FINISHED'; }
	static get MESSAGE_FORCE_SIT_OUT()		{ return 'MESSAGE_FORCE_SIT_OUT'; }
	static get MESSAGE_FRB_LOBBY_INTRO()	{ return 'MESSAGE_FRB_LOBBY_INTRO'; }
	static get MESSAGE_FRB_ROOM_INTRO()		{ return 'MESSAGE_FRB_ROOM_INTRO'; }

	static get CLOSE_REASON_COMPLETED() 	{ return 'Completed';}
	static get CLOSE_REASON_EXPIRED()		{ return 'Expired'; }
	static get CLOSE_REASON_CANCELLED()		{ return 'Cancelled'; }
	static get CLOSE_REASON_FORCE_SIT_OUT()	{ return 'ForceSitOut'; }
	
	constructor() {
		super();

		this._fIsActivated_bl = false;

		this._fTotalFreeShotsCount_int = undefined;
		this._fCurrentFreeShotsCount_int = undefined;
		this._fMessageType_str = undefined;
		this._fNextModeFRB_bl = undefined;
		this._fWinSum_num = undefined;
		this._fStake_num = undefined;
		this._fWeapons_obj_arr = [];
		this._fIsLobbyRestartRequired_bl = false;
		this._fIsRoomRestartRequired_bl = false;
		this._fIsCompletionInProgress_bl = false;
		this._fNextRoomId_num = undefined;
		this._fRealWinSum_num = undefined;
		this._fId_num = undefined;
		this._fFRBEndedDuringGameLoad_bln = false;
		this._fFrbCompletionState_bln = false;
		this._fPlayerSatIn_bln = false;
		this._fIsWinLimitExceeded_bl = false;
		this._fGameStarted_bln = false;
	}

	i_clearAll()
	{
		this._fIsActivated_bl = false;

		this._fTotalFreeShotsCount_int = undefined;
		this._fCurrentFreeShotsCount_int = undefined;
		this._fMessageType_str = undefined;
		this._fNextModeFRB_bl = undefined;
		this._fWinSum_num = undefined;
		this._fStake_num = undefined;
		this._fWeapons_obj_arr = [];
		this._fIsLobbyRestartRequired_bl = false;
		this._fIsRoomRestartRequired_bl = false;
		this._fIsCompletionInProgress_bl = false;
		this._fNextRoomId_num = undefined;
		this._fRealWinSum_num = undefined;
		this._fId_num = undefined;
		this._fFRBEndedDuringGameLoad_bln = false;
		this._fFrbCompletionState_bln = false;
		this._fPlayerSatIn_bln = false;
		this._fIsWinLimitExceeded_bl = false;
		this._fGameStarted_bln = false;
	}

	set frbEndedDuringGameLoad(aVal_bln)
	{
		this._fFRBEndedDuringGameLoad_bln = aVal_bln;
	}

	get frbEndedDuringGameLoad()
	{
		return this._fFRBEndedDuringGameLoad_bln;
	}

	get id()
	{
		return this._fId_num;
	}

	set id(aVal_num)
	{
		this._fId_num = aVal_num;
	}

	get keepBonusSW()
	{
		return false;
	}

	set realWinSum(aVal_num)
	{
		this._fRealWinSum_num = aVal_num;
	}

	get realWinSum()
	{
		return this._fRealWinSum_num;
	}

	set nextRoomId(aValue_num)
	{
		this._fNextRoomId_num = aValue_num;
	}

	get nextRoomId()
	{
		return this._fNextRoomId_num;
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

	set isActivated(aValue_bl)
	{
		this._fIsActivated_bl = aValue_bl;
	}

	get isActivated()
	{
		return this._fIsActivated_bl;
	}

	set totalFreeShotsCount(aValue_int)
	{
		this._fTotalFreeShotsCount_int = aValue_int;
	}

	get totalFreeShotsCount()
	{
		return this._fTotalFreeShotsCount_int;
	}

	set currentFreeShotsCount(aValue_int)
	{
		this._fCurrentFreeShotsCount_int = aValue_int;
	}

	get currentFreeShotsCount()
	{
		return this._fCurrentFreeShotsCount_int;
	}

	set messageType(aValue_str)
	{
		this._fMessageType_str = aValue_str;
	}

	get messageType()
	{
		return this._fMessageType_str;
	}

	set nextModeFRB(aValue_bl)
	{
		this._fNextModeFRB_bl = aValue_bl;
	}

	get nextModeFRB()
	{
		return this._fNextModeFRB_bl;
	}

	set winSum(aValue_num)
	{
		this._fWinSum_num = aValue_num;
	}

	get winSum()
	{
		return this._fWinSum_num;
	}

	set stake(aValue_num)
	{
		this._fStake_num = aValue_num;
	}

	get stake()
	{
		return this._fStake_num;
	}

	set weapons(aWeapons_obj_arr)
	{
		this._fWeapons_obj_arr = aWeapons_obj_arr;
	}

	get weapons()
	{
		return this._fWeapons_obj_arr;
	}

	get frbCompletionState()
	{
		return this._fFrbCompletionState_bln;
	}

	set frbCompletionState(aVal_num)
	{
		this._fFrbCompletionState_bln = aVal_num;
	}

	get playerSatIn()
	{
		return this._fPlayerSatIn_bln;
	}

	set playerSatIn(aVal_bln)
	{
		this._fPlayerSatIn_bln = aVal_bln;
	}

	get gameFRBStarted()
	{
		return this._fGameStarted_bln;
	}

	set gameFRBStarted(aVal_bln)
	{
		this._fGameStarted_bln = aVal_bln;
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

		return lTotalShotsCount_int;
	}

	set isWinLimitExceeded(value)
	{
		this._fIsWinLimitExceeded_bl = value;
	}

	get isWinLimitExceeded()
	{
		return this._fIsWinLimitExceeded_bl;
	}

}

export default FRBInfo