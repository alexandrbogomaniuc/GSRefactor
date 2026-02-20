import GUGameBaseDialogInfo from './GUGameBaseDialogInfo';

class GUGameNetworkErrorDialogInfo extends GUGameBaseDialogInfo
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

export default GUGameNetworkErrorDialogInfo