import GameBaseDialogInfo from './GameBaseDialogInfo';

class GameMidRoundExitDialogInfo extends GameBaseDialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._fRoomId_int = 0;
	}

	get roomId()
	{
		return this._fRoomId_int;
	}

	set roomId(aValue_int)
	{
		this._fRoomId_int = aValue_int;
	}
}

export default GameMidRoundExitDialogInfo