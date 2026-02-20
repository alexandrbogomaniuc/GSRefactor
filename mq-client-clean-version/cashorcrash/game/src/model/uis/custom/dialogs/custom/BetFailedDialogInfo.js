import DialogInfo from '../DialogInfo';

class BetFailedDialogInfo extends DialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._fIsPlaceBetTimePassed_bl = false;
	}

	set isPlaceBetTimePassed (value)
	{
		this._fIsPlaceBetTimePassed_bl = value;
	}

	get isPlaceBetTimePassed ()
	{
		return this._fIsPlaceBetTimePassed_bl;
	}

	destroy()
	{
		this._fIsPlaceBetTimePassed_bl = undefined;

		super.destroy();
	}
}

export default BetFailedDialogInfo