import DialogInfo from '../DialogInfo';

class GameNEMDialogInfo extends DialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._isRealMoneyMode = false;
		this._isTechnicalBuyInState = false;
	}

	set isRealMoneyMode(value)
	{
		this._isRealMoneyMode = value;
	}

	get isRealMoneyMode()
	{
		return this._isRealMoneyMode;
	}

	get isFreeMoneyMode()
	{
		return !this._isRealMoneyMode;
	}

	set isTechnicalBuyInState(value)
	{
		this._isTechnicalBuyInState = value;
	}

	get isTechnicalBuyInState()
	{
		return this._isTechnicalBuyInState;
	}
}

export default GameNEMDialogInfo