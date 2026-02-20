import DialogInfo from '../DialogInfo';

export const WAIT_MESSAGE_TYPES = {
	GET_BATTLEGROUND_START_URL:			"GET_BATTLEGROUND_GAME_START_URL"
};

export const WAIT_EVENT_TYPES = {
	LOBBY: 	"LOBBY",
	ROOM: 	"ROOM"
};

class PleaseWaitDialogInfo extends DialogInfo
{
	constructor(dialogId, priority)
	{
		super(dialogId, priority);

		this._fTimersForEvents_obj = {};	// containes objects like { timerId: <TIMER_ID_INT>, type: <EVENT_TYPE_STR> }
	}

	i_getAllEvents(aOptEventType_str=undefined)
	{
		if (aOptEventType_str === undefined)
		{
			return Object.keys(this._fTimersForEvents_obj);
		}

		let lEvents_str_arr = [];
		for (const key in this._fTimersForEvents_obj)
		{
			if (this._fTimersForEvents_obj[key].type === aOptEventType_str)
			{
				lEvents_str_arr.push(key);
			}
		}

		return lEvents_str_arr;
	}

	/**
	 * @param {String} aEvent_str 
	 * @returns {Number} Returns timer id.
	 */
	i_getTimerByEvent(aEvent_str)
	{
		return this._fTimersForEvents_obj[aEvent_str].timerId;
	}

	/**
	 * @param {String} aEvent_str 
	 * @returns {Number} Returns event type.
	 */
	i_getTypeByEvent(aEvent_str)
	{
		return this._fTimersForEvents_obj[aEvent_str].type;
	}

	/**
	 * Adds a new timer ID with event as a key.
	 * @param {String} aEvent_str 
	 * @param {Number} aEventType_str 
	 * @param {Number} aTimerId_int 
	 */
	i_addEventTimer(aEvent_str, aEventType_str, aTimerId_int)
	{
		if (!WAIT_EVENT_TYPES[aEventType_str])
		{
			throw new Error (`Wrong wait event type: ${aEventType_str}`);
		}
		this._fTimersForEvents_obj[aEvent_str] = { timerId: aTimerId_int, type: aEventType_str };
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

export default PleaseWaitDialogInfo;