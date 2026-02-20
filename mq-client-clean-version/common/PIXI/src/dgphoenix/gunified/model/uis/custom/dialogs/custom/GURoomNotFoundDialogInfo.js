import GUDialogInfo from '../GUDialogInfo';

class GURoomNotFoundDialogInfo extends GUDialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._roomSelectionErrorCode = undefined;
	}

	set roomSelectionErrorCode(value)
	{
		this._roomSelectionErrorCode = value;
	}

	get roomSelectionErrorCode()
	{
		return this._roomSelectionErrorCode;
	}
}

export default GURoomNotFoundDialogInfo