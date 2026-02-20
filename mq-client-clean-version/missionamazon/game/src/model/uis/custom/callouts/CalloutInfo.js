import SimpleUIInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo'

class CalloutInfo extends SimpleUIInfo
{
	
	constructor(calloutId, priority)
	{
		super(calloutId);

		this._priority = 0;
		this._activationTime = -1;
		this._isActive = false;
		this._isPresented = false;

		this._initCalloutInfo(priority);
	}

	destroy()
	{
		super.destroy();
	}

	get calloutId()
	{
		return this.i_getId();
	}

	get priority()
	{
		return this._priority;
	}

	set activationTime (value)
	{
		this._activationTime = value;
	}

	get activationTime ()
	{
		return this._activationTime;
	}

	set isActive (value)
	{
		this._isActive = value;
	}

	get isActive ()
	{
		return this._isActive;
	}

	set isPresented (value)
	{
		this._isPresented = value;
	}

	get isPresented ()
	{
		return this._isPresented;
	}

	_initCalloutInfo(priority)
	{
		if (priority !== undefined)
		{
			this._priority = priority;
		}
	}
}

export default CalloutInfo