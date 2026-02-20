import SimpleInfo from "../../base/SimpleInfo";

/** Supported log levels. */
const SERVER_LOG_LEVELS = ["FATAL", "ERROR", "WARN", "INFO", "TRACE", "DEBUG"];

class LoggingInfo extends SimpleInfo
{
	static get TYPE_ERROR()		{return SERVER_LOG_LEVELS.indexOf("ERROR");}
	static get TYPE_WARNING()	{return SERVER_LOG_LEVELS.indexOf("WARN");}
	static get TYPE_DEBUG()		{return SERVER_LOG_LEVELS.indexOf("DEBUG");}

	/** Returns log level type by index.
	 * @param {number} aType_int
	 * @returns {string}
	 */
	i_getClassString(aType_int)
	{
		if (!aType_int)
		{
			throw new Error(`Type is not defined!`);
		}

		switch(aType_int)
		{
			case LoggingInfo.TYPE_ERROR:
				return "Error";
			case LoggingInfo.TYPE_WARNING:
				return "Warning";
			case LoggingInfo.TYPE_DEBUG:
				return "Debug";
		}
	}

	i_pushToQueue(aMessageInfo_obj)
	{
		if (Boolean(aMessageInfo_obj))
		{
			this._pushToQueue(aMessageInfo_obj);
		}
	}

	/**
	 * Set logging level. TYPE_ERROR will be applied if provided logging type is not supported.
	 * @param {string} aType_str - Expected logging level type.
	 */
	i_setNewLoggingLevel(aType_str)
	{	
		if (SERVER_LOG_LEVELS.includes(aType_str))
		{
			this._fCurrentLoggingLevel_int = SERVER_LOG_LEVELS.indexOf(aType_str);
		}
		else
		{
			this._fCurrentLoggingLevel_int = LoggingInfo.TYPE_ERROR;
		}
	}

	/**
	 * Index of logging level type.
	 */
	get loggingLevel()
	{
		return this._fCurrentLoggingLevel_int;
	}

	/**
	 * Checks if log messages queue is emply.
	 * @type {boolean}
	 */
	get isQueueEmpty()
	{
		return this._fMessagesQueue_obj_arr.length === 0;
	}

	/** Logged messages queue.
	 * @type {Object[]}
	 */
	get messagesQueue()
	{
		return this._fMessagesQueue_obj_arr;
	}

	constructor()
	{
		super();
		this._fCurrentLoggingLevel_int = null;
		this._fMessagesQueue_obj_arr = [];
	}

	_pushToQueue(aMessageInfo_obj)
	{
		let lIndexByTime_int = this._fMessagesQueue_obj_arr.findIndex(e=> e.date > aMessageInfo_obj.date);
		if (lIndexByTime_int < 0)
		{
			this._fMessagesQueue_obj_arr.push(aMessageInfo_obj);
		}
		else
		{
			this._fMessagesQueue_obj_arr.splice(lIndexByTime_int, 0, aMessageInfo_obj);
		}
	}

	/** Destroy logging info instance. */
	destroy()
	{
		
		this._fCurrentLoggingLevel_int = null;
		super.destroy();
	}
}
export default LoggingInfo;