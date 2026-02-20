import DialogInfo from '../DialogInfo';

class RuntimeErrorDialogInfo extends DialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._errorMessage = null;
	}

	set errorMessage(value)
	{
		this._errorMessage = value;
	}

	get errorMessage()
	{
		return this._errorMessage;
	}

	destroy()
	{
		this._errorMessage = null;

		super.destroy();
	}
}

export default RuntimeErrorDialogInfo