import SimpleInfo from '../../../../unified/model/base/SimpleInfo';
import { APP } from '../../../../unified/controller/main/globals';

export const BATTLEGROUND_ROOM_STATE = 
{
	PLAY: "PLAY",
	QUALIFY: "QUALIFY",
	WAIT: "WAIT",
	CLOSED: "CLOSED",
}

class GUSLobbyBattlegroundInfo extends SimpleInfo {
	
	constructor()
	{
		super();

		this._fIsStartOnEnterLobbyInProgress_bl = !!APP.appParamsInfo.battlegroundBuyIn;
		this._fStartGameUrl_str = "";
		this._fRoomId_int = -1;
		this._fTimeToStartInMilliseconds_num = 0;
		this._fSelectedBuyInCost_num = undefined;
		this._fConfirmedBuyInCost_num = undefined;
		this._fTimeToStart_int = Date.now() + 60 * 1000;
		this._fReconnectionToPlayingRound_bl = false;
		this._isBattlegroundGameStarted_bl = null;
		this._fisGameStartUrlOnContinueWaitingUpdateExpected_bl = null;
		this._fisGameStartUrlOnPlayAgainUpdateExpected_bl = null;

		this._fisConfirmBuyinDialogExpectedOnLastHand_bl = null;
		this._fIsRoundResultWasActivated_bl = null;
		this._fIsLobbyBattlegroundNotEnoughPlayersDialogActivated_bl = null;
		this._fIsConfirmBuyinDialogRequired_bl = null;
		this._fAcurateClientTimeDiff_num = 0;
		this._fStartGameLevelWasInitiated_bl = null;
		this._fIsNotEnoughPlayersDialogRequired_bl = null;
		this._fIsRoundInProgress = null;
		this._fIsNoWeaponsFiredDialogRequired_bl = null;
		
	}

	set isConfirmBuyinDialogRequired(aVal_bl)
	{
		this._fIsConfirmBuyinDialogRequired_bl = aVal_bl;
	}

	get isConfirmBuyinDialogRequired()
	{
		return this._fIsConfirmBuyinDialogRequired_bl;
	}

	set isStartGameLevelWasInitiated(aVal_bl)
	{
		this._fStartGameLevelWasInitiated_bl = aVal_bl;
	}

	set isNotEnoughPlayersDialogRequired(aVal_bl)
	{
		this._fIsNotEnoughPlayersDialogRequired_bl = aVal_bl;
	}

	get isNotEnoughPlayersDialogRequired()
	{
		return this._fIsNotEnoughPlayersDialogRequired_bl;
	}

	set isNoWeaponsFiredDialogRequired(aVal_bl)
	{
		this._fIsNoWeaponsFiredDialogRequired_bl = aVal_bl;
	}

	get isNoWeaponsFiredDialogRequired()
	{
		return this._fIsNoWeaponsFiredDialogRequired_bl;
	}

	get isStartGameLevelWasInitiated()
	{
		return this._fStartGameLevelWasInitiated_bl;
	}

	set isBattlegroundGameStarted(aVal_bl)
	{
		this._isBattlegroundGameStarted_bl = aVal_bl;
	}

	get isBattlegroundGameStarted()
	{
		return this._isBattlegroundGameStarted_bl;
	}

	set isGameStartUrlOnContinueWaitingUpdateExpected(aVal_bl)
	{
		this._fisGameStartUrlOnContinueWaitingUpdateExpected_bl = aVal_bl;
	}

	get isGameStartUrlOnContinueWaitingUpdateExpected()
	{
		return this._fisGameStartUrlOnContinueWaitingUpdateExpected_bl;
	}	

	set isGameStartUrlOnPlayAgainUpdateExpected(aVal_bl)
	{
		this._fisGameStartUrlOnPlayAgainUpdateExpected_bl = aVal_bl;
	}

	get isGameStartUrlOnPlayAgainUpdateExpected()
	{
		return this._fisGameStartUrlOnPlayAgainUpdateExpected_bl;
	}

	set isConfirmBuyinDialogExpectedOnLastHand(aVal_bl)
	{
		this._fisConfirmBuyinDialogExpectedOnLastHand_bl = aVal_bl;
	}

	get isConfirmBuyinDialogExpectedOnLastHand()
	{
		return this._fisConfirmBuyinDialogExpectedOnLastHand_bl;
	}

	set isRoundResultWasActivated(aVal_bl)
	{
		this._fIsRoundResultWasActivated_bl = aVal_bl;
	}

	get isRoundResultWasActivated()
	{
		return this._fIsRoundResultWasActivated_bl;
	}

	set isLobbyBattlegroundNotEnoughPlayersDialogActivated(aVal_bl)
	{
		this._fIsLobbyBattlegroundNotEnoughPlayersDialogActivated_bl = aVal_bl;
	}

	get isLobbyBattlegroundNotEnoughPlayersDialogActivated()
	{
		return this._fIsLobbyBattlegroundNotEnoughPlayersDialogActivated_bl;
	}

	get isRoundInProgress()
	{
		return this._fIsRoundInProgress;
	}

	set isRoundInProgress(aVal_bl)
	{
		this._fIsRoundInProgress = aVal_bl;
	}

	setConfirmedBuyInCost(aConfirmedBuyInCost_num)
	{
		this._fConfirmedBuyInCost_num = aConfirmedBuyInCost_num;
	}

	getConfirmedBuyInCost()
	{
		return this._fConfirmedBuyInCost_num;
	}

	isConfirmedBuyInCostDefined()
	{
		return this._fConfirmedBuyInCost_num !== undefined;
	}

	setTimeToStart(aTime_int)
	{
		this._fTimeToStart_int = aTime_int;
	}

	syncTime(aTime_int)
	{
		let lCutTime_num = Date.now();
		this._fAcurateClientTimeDiff_num = aTime_int - lCutTime_num;
	}

	getTimeToStartInMillis()
	{
		if(this._fTimeToStart_int === undefined)
		{
			return undefined;
		}

		let lDelta_num = this._fTimeToStart_int - Date.now() - this._fAcurateClientTimeDiff_num;

		if(lDelta_num < 0)
		{
			return 0;
		}

		return lDelta_num;
	}


	setSelectedBuyInCost(aBuyInCost_num)
	{
		this._fSelectedBuyInCost_num = aBuyInCost_num;
	}

	getSelectedBuyInCost()
	{
		return this._fSelectedBuyInCost_num;
	}

	setStartOnEntnerLobbyInProgress(aIsInProgress_bl)
	{
		this._fIsStartOnEnterLobbyInProgress_bl = aIsInProgress_bl;
	}

	isStartOnEnterLobbyInProgress()
	{
		return this._fIsStartOnEnterLobbyInProgress_bl;
	}

	setStartGameURL(aStartGameUrl_str)
	{
		this._fStartGameUrl_str = aStartGameUrl_str;

		let lParams_str_arr = aStartGameUrl_str.split("?")[1].split("&");

		for( let i = 0; i < lParams_str_arr.length; i++ )
		{

			let lKeyValue_str_arr = lParams_str_arr[i].split("=");

			if(lKeyValue_str_arr[0] === "roomId")
			{
				this._fRoomId_int = parseInt(lKeyValue_str_arr[1]);
			}
			else if(lKeyValue_str_arr[0] === "stake")
			{
				this._fConfirmedBuyInCost_num = parseInt(lKeyValue_str_arr[1]);
			}
		}
	}

	getStartGameURL()
	{
		return this._fStartGameUrl_str;
	}

	get isStartGameURLDefined()
	{
		return !!this._fStartGameUrl_str && !!this._fStartGameUrl_str.length;
	}

	getRoomId()
	{
		return this._fRoomId_int;
	}

	getFormattedTimeToStart(aOptIsHHRequired_bl)
	{
		if(this._fTimeToStart_int === undefined)
		{
			return aOptIsHHRequired_bl ? "--:--:--" : "--:--"
		}

		let lSecondsCount_int = Math.round(this.getTimeToStartInMillis() / 1000);

		let hh = Math.floor(lSecondsCount_int / 60 / 60);
		let mm = Math.floor(lSecondsCount_int / 60 - hh * 60);
		let ss = lSecondsCount_int % 60;

		let ssPrefix_str = ss < 10 ? "0" : "";
		let mmPrefix_str = mm < 10 ? "0" : "";
		let hhPrefix_str = hh < 10 ? "0" : "";

		if(aOptIsHHRequired_bl)
		{
			return hhPrefix_str + hh + ":" + mmPrefix_str + mm + ":" + ssPrefix_str + ss;
		}

		return mmPrefix_str + mm + ":" + ssPrefix_str + ss;
	}

}

export default GUSLobbyBattlegroundInfo