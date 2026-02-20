import LoggingInfo from "../../../model/interaction/server/LoggingInfo";
import SimpleController from "../../base/SimpleController";
import { APP } from "../../main/globals";

/**
 * @class
 * @classdesc Sends log messages to server.
 */
class LoggingController extends SimpleController
{
	/** Log message of type TYPE_ERROR. */
	i_pushError(aMessage_str)
	{
		//this.__pushMessage(LoggingInfo.TYPE_ERROR, aMessage_str);
	}

	/** Log message of type TYPE_WARNING. */
	i_pushWarning(aMessage_str)
	{
		//this.__pushMessage(LoggingInfo.TYPE_WARNING, aMessage_str);
	}

	/** Log message of type TYPE_DEBUG. */
	i_pushDebug(aMessage_str)
	{
		//this.__pushMessage(LoggingInfo.TYPE_DEBUG, aMessage_str);
	}

	/** Apply expected logging level. */
	i_setNewLoggingLevel(aType_str)
	{
		/*this.info.i_setNewLoggingLevel(aType_str);

		if (this.info.loggingLevel >= LoggingInfo.TYPE_WARNING)
		{
			const WARNING_METHOD = console.warn;
			console.warn = (...args) => {
				this.i_pushWarning(`DEVTOOLS_WARNING: ${JSON.stringify(args)}`);
				WARNING_METHOD.apply(console, args);
			}
		}

		if (this.info.loggingLevel >= LoggingInfo.TYPE_DEBUG)
		{
			const DEBUG_METHOD = console.debug;
			console.debug = (...args) => {
				this.i_pushDebug(`DEVTOOLS_DEBUG: ${JSON.stringify(args)}`);
				DEBUG_METHOD.apply(console, args);
			}
		}*/
	}

	constructor()
	{
		super(new LoggingInfo());
		/*this._fXMLHttpRequest_xmlhr = null;

		this._fRetrySendTimer_t = null;
		this._fSendTetryTime_num = 0;*/
	}

	/**
	 * Collects the information and decides whether to put this message in queue or to process it directly.
	 * Handles only messages with type less or equal to current loggingLevel.
	 * @abstract
	 * @param {Number} aType_int 
	 * @param {String} aMessage_str
	 */
	__pushMessage(aType_int, aMessage_str)
	{
		/*if (aType_int <= this.info.loggingLevel)
		{
			let lRequestDataToSend_obj = new Object();
			let lAppParams_apppi = APP.appParamsInfo;
			let lURLParams_obj = APP.urlBasedParams;
			let lPlatformInfo_obj = window.getPlatformInfo();

			lRequestDataToSend_obj.bankId = lAppParams_apppi.bankId || lURLParams_obj.BANKID;
			lRequestDataToSend_obj.browser = `${lPlatformInfo_obj.name}:${lPlatformInfo_obj.version}`;
			lRequestDataToSend_obj.class = this.info.i_getClassString(aType_int);
			lRequestDataToSend_obj.date = Date.now();
			lRequestDataToSend_obj.comment = aMessage_str;
			lRequestDataToSend_obj.gameId = lAppParams_apppi.gameId;
			lRequestDataToSend_obj.lang = lAppParams_apppi.lang || lURLParams_obj.LANG;
			lRequestDataToSend_obj.mode = lAppParams_apppi.mode || lURLParams_obj.MODE;
			lRequestDataToSend_obj.serverId = lAppParams_apppi.serverId || lURLParams_obj.GAMESERVERID;
			lRequestDataToSend_obj.sessionId = lAppParams_apppi.sessionId || lURLParams_obj.SID;
			lRequestDataToSend_obj.userAgent = lPlatformInfo_obj.ua;
			lRequestDataToSend_obj.version = APP.version;

			if (!this.info.isQueueEmpty)
			{
				this.info.i_pushToQueue(lRequestDataToSend_obj);
				this._processQueue();
			}
			else
			{
				this._processMessage(lRequestDataToSend_obj);
			}
		}*/
	}

	/**
	 * Tries to send the message.
	 * @param {Object} aMessageInfo_obj 
	 */
	_processMessage(aMessageInfo_obj)
	{
		/*try
		{
			let lWebSocketURL_str = APP.webSocketInteractionController.info.socketUrl || APP.appParamsInfo.lobbyWebSocket || APP.urlBasedParams.WEB_SOCKET_URL;
			lWebSocketURL_str = decodeURIComponent(lWebSocketURL_str).split("/")[2];
			if (lWebSocketURL_str)
			{
				let l_xmlhr = new XMLHttpRequest();
				let lHttpURL_str = `https://${lWebSocketURL_str}/common/logdebug.jsp`;
				l_xmlhr.open('POST', lHttpURL_str, true);
				l_xmlhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
				l_xmlhr.send(`logMessage=${JSON.stringify(aMessageInfo_obj)}`);
			}

			if (this.info.isQueueEmpty)
			{
				this._fRetrySendTimer_t && clearTimeout(this._fRetrySendTimer_t);
				this._fSendTetryTime_num = 0;
			}

		}
		catch (lError_obj)
		{
			if (!this._fSendTetryTime_num)
			{
				this._fSendTetryTime_num = 1000;
				this.info.i_pushToQueue(aMessageInfo_obj);
				this.i_pushError(`Logger: Sending message failed! ${lError_obj}`);
			}
			else
			{
				this._fRetrySendTimer_t = setTimeout(this._processMessage.bind(this, aMessageInfo_obj), this._fSendTetryTime_num);
				this._fSendTetryTime_num = Math.min(this._fSendTetryTime_num*2, 64000);
			}
		}*/
	}

	_processQueue()
	{
		/*while (!this.info.isQueueEmpty)
		{
			this._processMessage(this.info.messagesQueue.shift());
		}*/
	}

	destroy()
	{
		/*this._fXMLHttpRequest_xmlhr && this._fXMLHttpRequest_xmlhr.close && this._fXMLHttpRequest_xmlhr.close();
		this._fXMLHttpRequest_xmlhr = null;

		clearTimeout(this._fRetrySendTimer_t);
		this._fRetrySendTimer_t = null;
		this._fSendTetryTime_num = 0;
		*/

		super.destroy();
	}

}
export default LoggingController;