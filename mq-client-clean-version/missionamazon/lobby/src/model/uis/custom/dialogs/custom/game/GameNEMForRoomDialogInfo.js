import GameBaseDialogInfo from './GameBaseDialogInfo';

class GameNEMForRoomDialogInfo extends GameBaseDialogInfo
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

export default GameNEMForRoomDialogInfo