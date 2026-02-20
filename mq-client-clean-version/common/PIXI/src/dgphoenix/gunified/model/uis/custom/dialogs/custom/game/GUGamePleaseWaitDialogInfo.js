import GUGameBaseDialogInfo from './GUGameBaseDialogInfo';

class GUGamePleaseWaitDialogInfo extends GUGameBaseDialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._fTimersForEvents_obj = {};
	}

	i_getAllEvents()
	{
		return Object.keys(this._fTimersForEvents_obj);
	}

	/**
	 * @param {String} aEvent_str 
	 * @returns {Number} Returns timer id.
	 */
	i_getTimerByEvent(aEvent_str)
	{
		return this._fTimersForEvents_obj[aEvent_str];
	}

	/**
	 * Adds a new timer ID with event as a key.
	 * @param {String} aEvent_str 
	 * @param {Number} aTimerId_int 
	 */
	i_addEventTimer(aEvent_str, aTimerId_int)
	{
		this._fTimersForEvents_obj[aEvent_str] = aTimerId_int;
	}

	/**
	 * Removes timer by event.
	 * @param {String} aEvent_str
	 */
	i_removeEventTimer(aEvent_str)
	{
		delete this._fTimersForEvents_obj[aEvent_str];
	}

	i_doesTimerExist(aEvent_str)
	{
		return this._fTimersForEvents_obj.hasOwnProperty(aEvent_str);
	}

	get isTimerListEmpty()
	{
		return Object.keys(this._fTimersForEvents_obj).length == 0;
	}

	destroy()
	{
		this._fTimersForEvents_obj = null;

		super.destroy();
	}
}

export default GUGamePleaseWaitDialogInfo;