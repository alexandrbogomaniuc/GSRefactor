import GUDialogInfo from "../GUDialogInfo";

class GURoundAlreadyFinishedDialogInfo extends GUDialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._roomSelectionErrorCode = undefined;
	}
}

export default GURoundAlreadyFinishedDialogInfo