import DialogInfo from '../../DialogInfo';

class GameBaseDialogInfo extends DialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._isSwitchToGameAwaiting = false;
	}

	get isActivationOverHiddenGameAvailable()
	{
		return false;
	}

	set isSwitchToGameAwaiting(value)
	{
		this._isSwitchToGameAwaiting = value;
	}

	get isSwitchToGameAwaiting()
	{
		return this._isSwitchToGameAwaiting;
	}	
}

export default GameBaseDialogInfo