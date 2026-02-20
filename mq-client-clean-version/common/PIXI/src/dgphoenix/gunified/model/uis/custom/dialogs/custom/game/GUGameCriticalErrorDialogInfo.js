import GUGameBaseDialogInfo from './GUGameBaseDialogInfo';

class GUGameCriticalErrorDialogInfo extends GUGameBaseDialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._errorCode = undefined;
		this._errorTime = undefined;
		this._timeOffset = undefined;
		this._rid = undefined;
	}

	get isActivationOverHiddenGameAvailable()
	{
		return true;
	}

	set errorCode(value)
	{
		this._errorCode = value;
	}

	get errorCode()
	{
		return this._errorCode;
	}

	set errorTime(value)
	{
		this._errorTime = value;
	}

	get errorTime()
	{
		return this._errorTime;
	}

	set timeOffset(value)
	{
		this._timeOffset = value;
	}

	get timeOffset()
	{
		return this._timeOffset;
	}

	set rid(value)
	{
		this._rid = value;
	}

	get rid()
	{
		return this._rid;
	}

	destroy()
	{
		this._errorCode = undefined;
		this._errorTime = undefined;
		this._timeOffset = undefined;
		this._rid = undefined;

		super.destroy();
	}
}

export default GUGameCriticalErrorDialogInfo