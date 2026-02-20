import SimpleUIInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo'
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class RoundResultScreenInfo extends SimpleUIInfo
{
	constructor()
	{
		super();

		this._fPlayerNickname_str = null;

		this._fTotalDamage_num = null;
		this._fMaxDamage_num = null;
		this._fWinnersCount_num = null;
		this._fbattlegroundBuyIn_num = null;

		this._fTotalKillsCount_num = null;

		this._fTotalTreasuresCount_num = null;
		
		this._fPlayerAvatarData_obj = null;

		this._fListData_arr = [];

		this._fRoundId_num = undefined;
		this._fRoundResultResponseRecieved_bln = null;

		this._pendingFinalPlayerStats = null;

		this._fQuestsCompletedCount_num = null;
		this._fQuestsPayouts_num = null;

		this._fWeaponsSurplus_num = 0;

		this._fBattlegroundTotalPot_num = 0;

		this._fTimeToStart_int = null;

		this._fIsScreenActive_bl = null;
		this._fIsBattlegroundRoundCancelled_bl = null;
	}

	get battlegroundBuyIn()
	{
		return this._fBattlegroundBuyIn_num;
	}

	set battlegroundBuyIn(aAmount_num)
	{
		this._fBattlegroundBuyIn_num = aAmount_num;
	}

	get isBattlegroundRoundCancelled()
	{
		return this._fIsBattlegroundRoundCancelled_bl;
	}

	set isBattlegroundRoundCancelled(aAmount_num)
	{
		this._fIsBattlegroundRoundCancelled_bl = aAmount_num;
	}

	getBattlegroundTotalPot()
	{
		return this._fBattlegroundTotalPot_num;
	}

	setBattlegroundTotalPot(aAmount_num)
	{
		this._fBattlegroundTotalPot_num = aAmount_num;
	}

	get isScreenActive()
	{
		return this._fIsScreenActive_bl;
	}

	set isScreenActive(aVal_bl)
	{
		this._fIsScreenActive_bl = aVal_bl;
	}

	get isActiveScreenMode()
	{
		return APP.gameScreen.isBattlegroundMode;
	}

	get roundId()
	{
		return this._fRoundId_num;
	}

	set roundId(aVal_num)
	{
		this._fRoundId_num = aVal_num;
	}

	resetRoundId()
	{
		this._fRoundId_num = undefined;
	}

	get roundResultResponseRecieved()
	{
		return this._fRoundResultResponseRecieved_bln;
	}

	set roundResultResponseRecieved(aVal_bln)
	{
		this._fRoundResultResponseRecieved_bln = aVal_bln;
	}

	get playerNickname()
	{
		return this._fPlayerNickname_str;
	}

	set playerNickname(aValue_str)
	{
		this._fPlayerNickname_str = aValue_str;
	}

	get pendingFinalPlayerStats()
	{
		return this._pendingFinalPlayerStats;
	}

	set pendingFinalPlayerStats(aVal_obj)
	{
		this._pendingFinalPlayerStats = aVal_obj;
	}

	get totalDamage()
	{
		return this._fTotalDamage_num;
	}

	set totalDamage(aVal_num)
	{
		this._fTotalDamage_num = aVal_num;
	}

	get maxDamage()
	{
		return this._fMaxDamage_num;
	}

	set maxDamage(aVal_num)
	{
		this._fMaxDamage_num = aVal_num;
	}

	get winnersCount()
	{
		return this._fWinnersCount_num;
	}

	set winnersCount(aVal_num)
	{
		this._fWinnersCount_num = aVal_num;
	}

	get totalKillsCount()
	{
		return this._fTotalKillsCount_num;
	}

	set totalKillsCount(aVal_num)
	{
		this._fTotalKillsCount_num = aVal_num;
	}

	get totalTreasuresCount()
	{
		return this._fTotalTreasuresCount_num;
	}

	set totalTreasuresCount(aVal_num)
	{
		this._fTotalTreasuresCount_num = aVal_num;
	}

	get playerAvatarData()
	{
		return this._fPlayerAvatarData_obj;
	}

	set playerAvatarData(aVal_obj)
	{
		this._fPlayerAvatarData_obj = aVal_obj;
	}

	get listData()
	{
		return this._fListData_arr;
	}

	set listData(aVal_arr)
	{
		this._fListData_arr = aVal_arr;
	}

	get questsCompletedCount()
	{
		return this._fQuestsCompletedCount_num;
	}

	set questsCompletedCount(aVal_num)
	{
		this._fQuestsCompletedCount_num = aVal_num;
	}

	get questsPayouts()
	{
		return this._fQuestsPayouts_num;
	}

	set questsPayouts(aVal_num)
	{
		this._fQuestsPayouts_num = aVal_num;
	}

	get weaponsSurplus()
	{
		return this._fWeaponsSurplus_num;
	}

	set weaponsSurplus(aVal_num)
	{
		aVal_num = Number(aVal_num || 0);
		this._fWeaponsSurplus_num = aVal_num;
	}

	setTimeToStart(aTime_int)
	{
		this._fTimeToStart_int = aTime_int;
	}

	getTimeToStartInMillis()
	{
		if(this._fTimeToStart_int === undefined)
		{
			return undefined;
		}

		let lDelta_num = this._fTimeToStart_int - APP.currentWindow.accurateCurrentTime;

		if(lDelta_num < 0)
		{
			return 0;
		}

		return lDelta_num;
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

		if (hh == 0 && mm == 0 && ss == 0)
		{
			return null;
		}

		if(aOptIsHHRequired_bl)
		{
			return hhPrefix_str + hh + ":" + mmPrefix_str + mm + ":" + ssPrefix_str + ss;
		}

		return mmPrefix_str + mm + ":" + ssPrefix_str + ss;
	}

	destroy()
	{
		this._fPlayerNickname_str = null;

		this._fTotalDamage_num = null;

		this._fMaxDamage_num = null;
		this._fWinnersCount_num = null;

		this._fbattlegroundBuyIn_num = null;

		this._fTotalKillsCount_num = null;

		this._fTotalTreasuresCount_num = null;
		
		this._fListData_arr = null;

		this._pendingFinalPlayerStats = null;

		this._fQuestsCompletedCount_num = null;
		this._fQuestsPayouts_num = null;

		this._fWeaponsSurplus_num = null;

		this._fBattlegroundTotalPot_num = null;

		this._fIsBattlegroundRoundCancelled_bl = null;

		super.destroy();
	}
}

export default RoundResultScreenInfo