import GUGameBaseDialogInfo from './GUGameBaseDialogInfo';

class GUGameRebuyDialogInfo extends GUGameBaseDialogInfo
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

export default GUGameRebuyDialogInfo