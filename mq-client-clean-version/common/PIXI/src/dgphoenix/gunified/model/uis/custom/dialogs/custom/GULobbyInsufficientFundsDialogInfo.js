import GUDialogInfo from '../GUDialogInfo';

class GULobbyInsufficientFundsDialogInfo extends GUDialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._fIsOkMode_bl = null;
		this._fIsCustomMode_bl = null;
	}

	setOkMode()
	{
		this._fIsOkMode_bl = true;
		this._fIsCustomMode_bl = false;
	}

	get isOkMode()
	{
		return this._fIsOkMode_bl;
	}

	setCustomMode()
	{
		this._fIsOkMode_bl = false;
		this._fIsCustomMode_bl = true;
	}

	get isCustomMode()
	{
		return this._fIsCustomMode_bl;
	}
}

export default GULobbyInsufficientFundsDialogInfo