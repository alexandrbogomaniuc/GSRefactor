import DialogInfo from '../DialogInfo';

class WaitPendingOperationDialogInfo extends DialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);
	}

	destroy()
	{
		super.destroy();
	}
}

export default WaitPendingOperationDialogInfo;