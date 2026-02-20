import GUGameBaseDialogInfo from './GUGameBaseDialogInfo';

class GUGameBuyAmmoFailedDialogInfo extends GUGameBaseDialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._retryType = null;
	}

	set retryType(value)
	{
		this._retryType = value;
	}

	get retryType()
	{
		return this._retryType;
	}
}

export default GUGameBuyAmmoFailedDialogInfo