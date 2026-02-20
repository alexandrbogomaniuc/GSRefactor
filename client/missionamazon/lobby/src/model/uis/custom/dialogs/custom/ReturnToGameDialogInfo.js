import DialogInfo from '../DialogInfo';

class ReturnToGameDialogInfo extends DialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._fRoomId_int = 0;
		this._fAlreadySitInStake_num = 0;
	}

	get roomId()
	{
		return this._fRoomId_int;
	}

	set roomId(aValue_int)
	{
		this._fRoomId_int = aValue_int;
	}

	set alreadySitInStake(aValue_num)
	{
		this._fAlreadySitInStake_num = aValue_num;
	}

	get alreadySitInStake()
	{
		return this._fAlreadySitInStake_num;
	}

	resetLasthandInfo()
	{
		this.roomId = 0;
		this.alreadySitInStake = 0;
	}

	get isLasthandInfoDefined()
	{
		return this.roomId > 0;
	}
}

export default ReturnToGameDialogInfo