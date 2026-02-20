import DialogInfo from '../DialogInfo';

class BattlegroundBuyInConfirmationDialogInfo extends DialogInfo
{
	static get MODE_ID_RE_BUY() { return 0 }
	static get MODE_ID_BUY_IN() { return 1 }

	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._fBuyInCost_num = undefined;
		this._fTimeToStart_int = 60;
		this._fModeId_int = BattlegroundBuyInConfirmationDialogInfo.MODE_ID_BUY_IN;
		this._observers = null;
		this._fTimeToStart_int = null;
	}

	setFullGameInfo(aData_obj)
	{
		if(!aData_obj) return;
		this._observers = aData_obj.observers;
		this._fTimeToStart_int = aData_obj.timeToStart;
	}

	get isBtgGameReadyToStart()
	{
		let moreThanOneReadyObserver = false; 
		for( let i=0; i<this.observers.length; i++)
		{
			const observer = this.observers[i];
			if(observer.status != "WAITING")
			{
				moreThanOneReadyObserver = true;
			}
		}
		return moreThanOneReadyObserver;
	}

	
	resetStatusesForObservers()
	{
		for( let i=0; i<this.observers.length; i++)
		{
			const observer = this.observers[i];
			observer.status = "WAITING";
		}
	}

	get observers()
	{
		return this._observers || [];
	}

	get timeToStart()
	{
		return this._fTimeToStart_int;
	}

	setMode(aModeId_int)
	{
		this._fModeId_int = aModeId_int;
	}

	getModeId()
	{
		return this._fModeId_int;
	}

	setBuyInCost(aBuyInCost_num)
	{
		this._fBuyInCost_num = aBuyInCost_num;
	}

	getBuyInCost()
	{
		return this._fBuyInCost_num;
	}
}

export default BattlegroundBuyInConfirmationDialogInfo