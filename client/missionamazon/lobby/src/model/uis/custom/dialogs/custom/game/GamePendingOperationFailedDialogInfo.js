import GameBaseDialogInfo from './GameBaseDialogInfo';

class GamePendingOperationFailedDialogInfo extends GameBaseDialogInfo
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

export default GamePendingOperationFailedDialogInfo