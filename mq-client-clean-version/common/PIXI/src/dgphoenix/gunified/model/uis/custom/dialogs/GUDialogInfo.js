import SimpleUIInfo from '../../../../../unified/model/uis/SimpleUIInfo';

class GUDialogInfo extends SimpleUIInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId);

		this._priority = 0;
		this._activationTime = -1;
		this._isActive = false;
		this._isPresented = false;

		this._initDialogInfo(priority);
	}

	destroy()
	{
		super.destroy();
	}

	get dialogId()
	{
		return this.i_getId();
	}

	get priority()
	{
		return this._priority;
	}

	set activationTime(value)
	{
		this._activationTime = value;
	}

	get activationTime()
	{
		return this._activationTime;
	}

	set isActive(value)
	{
		this._isActive = value;
	}

	get isActive()
	{
		return this._isActive;
	}

	set isPresented(value)
	{
		this._isPresented = value;
	}

	get isPresented()
	{
		return this._isPresented;
	}

	_initDialogInfo(priority)
	{
		if (priority !== undefined)
		{
			this._priority = priority;
		}
	}
}

export default GUDialogInfo