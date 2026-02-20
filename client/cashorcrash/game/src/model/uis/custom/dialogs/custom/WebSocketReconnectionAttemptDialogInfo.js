import DialogInfo from '../DialogInfo';

class WebSocketReconnectionAttemptDialogInfo extends DialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._reconnectionCount_num = 1;
	}

	get reconnectionCount()
	{
		return this._reconnectionCount_num;
	}

	set reconnectionCount(aValue)
	{
		this._reconnectionCount_num = aValue;
	}

	increaseReconnectionCount()
	{
		this._reconnectionCount_num++;
	}

	destroy()
	{
		super.destroy();
	}
}

export default WebSocketReconnectionAttemptDialogInfo