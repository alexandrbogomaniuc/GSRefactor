import DialogInfo from '../DialogInfo';

class TooLateEjectDialogInfo extends DialogInfo
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

export default TooLateEjectDialogInfo