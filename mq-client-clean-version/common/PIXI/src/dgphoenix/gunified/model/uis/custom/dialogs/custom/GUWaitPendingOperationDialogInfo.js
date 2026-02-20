import GUDialogInfo from '../GUDialogInfo';

class GUWaitPendingOperationDialogInfo extends GUDialogInfo
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

export default GUWaitPendingOperationDialogInfo;