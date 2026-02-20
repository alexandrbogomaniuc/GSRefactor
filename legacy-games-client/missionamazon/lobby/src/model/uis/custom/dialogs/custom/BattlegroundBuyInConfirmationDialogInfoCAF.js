import DialogInfo from '../DialogInfo';

class BattlegroundBuyInConfirmationDialogInfoCAF extends DialogInfo
{
	static get MODE_ID_RE_BUY() { return 0 }
	static get MODE_ID_BUY_IN() { return 1 }

	constructor(dialogId, priority)
	{

		console.log("is caf buy in INFO ");
		super(dialogId, priority);

		this._fBuyInCost_num = undefined;
		this._fTimeToStart_int = 60;
		this._fModeId_int = BattlegroundBuyInConfirmationDialogInfoCAF.MODE_ID_BUY_IN;
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

export default BattlegroundBuyInConfirmationDialogInfoCAF