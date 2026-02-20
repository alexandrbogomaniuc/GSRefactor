import DialogInfo from '../DialogInfo';

class RoundAlreadyFinishedDialogInfo extends DialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._roomSelectionErrorCode = undefined;
	}
}

export default RoundAlreadyFinishedDialogInfo