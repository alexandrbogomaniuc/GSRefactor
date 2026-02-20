import SimpleUIInfo from '../../../../../unified/model/uis/SimpleUIInfo';

class GUSLobbyCommonPanelInfo extends SimpleUIInfo
{
	constructor()
	{
		super();

		this._init();
	}

	//HISTORY...
	set historyCallback(aValue_str)
	{
		this._fHistoryCallbackUrl_str = aValue_str;
	}

	get historyCallback()
	{
		return this._fHistoryCallbackUrl_str;
	}

	get historyButtonEnabled()
	{
		return this._fHistoryCallbackUrl_str !== undefined;
	}
	//...HISTORY

	//TIME...
	set timerFrequency(aValue_num)
	{
		this._fTimerFrequency_num = aValue_num;
	}

	get timerFrequency()
	{
		return this._fTimerFrequency_num;
	}

	set timeOffset(aValue_num)
	{
		this._fTimeOffset_num = aValue_num;
	}

	set timeFromServer(aValue_num)
	{
		this._fTimeFromServer_num = aValue_num;
	}

	get timeFromServer()
	{
		return this._fTimeFromServer_num;
	}

	getTime(aOnTick_bl = false)
	{
		if (aOnTick_bl)
		{
			this._fTimeOddTick_bl = !this._fTimeOddTick_bl;
		}

		let date = new Date();
		if (this._fTimeFromServer_num !== undefined)
		{
			date.setTime(this._fTimeFromServer_num);
		}
		date.setTime(date.getTime() + (this._fTimeOffset_num !== undefined ? this._fTimeOffset_num : -date.getTimezoneOffset()) * 60000);

		return this.formatTimePart(date.getUTCHours()) +
			(this._fTimeOddTick_bl ? ':' : ' ') +
			this.formatTimePart(date.getUTCMinutes());
	}

	formatTimePart(aValue_int)
	{
		var lRet_str = String(aValue_int);
		lRet_str = lRet_str.length < 2 ? new Array(3 - lRet_str.length).join("0") + lRet_str : lRet_str;
		return lRet_str;
	}

	get timeIndicatorEnabled()
	{
		return this._fTimerFrequency_num !== undefined;
	}
	//...TIME

	//HOME...
	set homeCallback(aValue_str)
	{
		this._fHomeCallbackUrl_str = aValue_str;
	}

	get homeCallback()
	{
		return this._fHomeCallbackUrl_str;
	}

	get homeButtonEnabled()
	{
		return this._fHomeCallbackUrl_str !== undefined;
	}
	//...HOME

	set isDialogActive(aValue_bl)
	{
		this._fIsDialogActive_bl = aValue_bl;
	}

	get isDialogActive()
	{
		return this._fIsDialogActive_bl;
	}

	//GAME DATA...
	set gameUIVisible(aValue_bl)
	{
		this._fGameUIVisible_bl = aValue_bl;
	}

	get gameUIVisible()
	{
		return this._fGameUIVisible_bl;
	}

	set gameBalance(aValue_num)
	{
		this._fGameBalance_num = aValue_num;
	}

	get gameBalance()
	{
		return this._fGameBalance_num;
	}

	set gameWin(aValue_num)
	{
		this._fGameWin_num = aValue_num;
	}

	get gameWin()
	{
		return this._fGameWin_num;
	}

	set gameIndicatorsUpdateTime(aValue_num)
	{
		this._fGameIndicatorsUpdateTime_num = aValue_num;
	}

	get gameIndicatorsUpdateTime()
	{
		return this._fGameIndicatorsUpdateTime_num;
	}

	set gameCostPerBullet(aValue_num)
	{
		this._fGameCostPerBullet_num = aValue_num;
	}

	get gameCostPerBullet()
	{
		return this._fGameCostPerBullet_num;
	}

	set gameCurrencySymbol(aValue_str)
	{
		this._fGameCurrencySymbol_str = aValue_str;
	}

	get gameCurrencySymbol()
	{
		return this._fGameCurrencySymbol_str;
	}

	set playerSatIn(aValue_bl)
	{
		this._fPlayerSatIn_bl = aValue_bl;
	}

	get playerSatIn()
	{
		return !!this._fPlayerSatIn_bl;
	}

	get isPlayerSatInDefined()
	{
		return this._fPlayerSatIn_bl !== undefined;
	}

	set refreshBalanceAvailable(aValue_bl)
	{
		this._fRefreshBalanceAvailable_bl = aValue_bl;
	}

	get refreshBalanceAvailable()
	{
		return this._fRefreshBalanceAvailable_bl;
	}

	set isWebGlContextLost(aValue_bl)
	{
		this._fIsWebglContextLost_bl = aValue_bl;
	}

	get isWebGlContextLost()
	{
		return this._fIsWebglContextLost_bl;
	}
	//...GAME DATA

	destroy()
	{
		super.destroy();
	}

	_init()
	{
		this._fHistoryCallbackUrl_str = undefined;
		this._fTimerFrequency_num = undefined;
		this._fTimeFromServer_num = undefined;
		this._fTimeOffset_num = undefined;
		this._fTimeOddTick_bl = false;
		this._fHomeCallbackUrl_str = undefined;

		this._fIsDialogActive_bl = false;
		this._fGameUIVisible_bl = false;
		this._fGameBalance_num = undefined;
		this._fGameWin_num = undefined;
		this._fGameIndicatorsUpdateTime_num = undefined;
		this._fGameCostPerBullet_num = undefined;
		this._fGameCurrencySymbol_str = undefined;
		this._fPlayerSatIn_bl = undefined;
		this._fRefreshBalanceAvailable_bl = false;
		this._fIsWebglContextLost_bl = false;
	}
}

export default GUSLobbyCommonPanelInfo