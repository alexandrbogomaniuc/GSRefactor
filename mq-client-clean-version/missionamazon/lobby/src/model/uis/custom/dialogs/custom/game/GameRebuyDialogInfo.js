import GameBaseDialogInfo from './GameBaseDialogInfo';

class GameRebuyDialogInfo extends GameBaseDialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);
	}

	get isActivationOverHiddenGameAvailable()
	{
		return true;
	}
}

export default GameRebuyDialogInfo