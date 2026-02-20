import GUGameBaseDialogInfo from './GUGameBaseDialogInfo';

class GUGameNEMForRoomDialogInfo extends GUGameBaseDialogInfo
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

export default GUGameNEMForRoomDialogInfo