import GameBaseDialogInfo from './GameBaseDialogInfo';

class GameNetworkErrorDialogInfo extends GameBaseDialogInfo
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

export default GameNetworkErrorDialogInfo