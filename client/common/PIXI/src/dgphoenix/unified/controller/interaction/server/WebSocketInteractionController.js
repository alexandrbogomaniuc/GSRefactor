import SimpleController from '../../base/SimpleController';
import {WebSocketInteractionInfo, SERVER_MESSAGES, ERROR_CODE_TYPES, ERROR_CODE_RANGES, SUPPORTED_ERROR_CODES, GAME_CLIENT_MESSAGES} from '../../../model/interaction/server/WebSocketInteractionInfo';
import { APP } from '../../main/globals';
import Timer from '../../time/Timer';
import Application from '../../main/Application';
import { IOS } from '../../../view/layout/features';

let testCounter = -1;
let FIRST_RECONNECT_INTERVAL_MAX_SECONDS = 5;
let MIN_RECONNECT_INTERVAL = 1000;

//STUBS...
const DOWNLOAD_BUTTON_ID = "save_stubs_button";
const NOTIFICATION_PANEL_ID = "stubs_note";

const getCircularReplacer = () => 
{
	const seen = new WeakSet();

	return (key, value) => 
	{
		if (
			typeof value === "object"
			&& value !== null
			)
		{
			if (seen.has(value))
			{
				return;
			}
			seen.add(value);
		}

		return value;
  };
};

//...STUBS

/**
 * @class
 * @extends SimpleController
 * @classdesc Base class for WebSocket connection: establish connection, send and receive messages
 */
class WebSocketInteractionController extends SimpleController
{
	static get STUBS_PATH() { return undefined /*"!debug/stubs/stubs.txt"*/};
	
	static get EVENT_ON_SERVER_MESSAGE() { return "EVENT_ON_SERVER_MESSAGE" };
	static get EVENT_ON_SERVER_ERROR_MESSAGE() { return "EVENT_ON_SERVER_ERROR_MESSAGE" };
	static get EVENT_ON_SERVER_OK_MESSAGE() { return "EVENT_ON_SERVER_OK_MESSAGE" };

	static get EVENT_ON_SERVER_CONNECTION_CLOSED() { return "EVENT_ON_SERVER_CONNECTION_CLOSED" };
	static get EVENT_ON_SERVER_CONNECTION_OPENED() { return "EVENT_ON_SERVER_CONNECTION_OPENED" };

	static get EVENT_ON_GAME_CLIENT_SENT_MESSAGE()	{return "EVENT_ON_GAME_CLIENT_SENT_MESSAGE"; }
	static get EVENT_ON_GAME_SESSION_FINISHED()	{return "EVENT_ON_GAME_SESSION_FINISHED"};
	static get FINISH_WEB_SESSION()	{return "FinishGameSession";}



	/**
	 * Finds the last request of specific type
	 * @param {String} aRequestClass_str - Target request type (request `class` property)
	 * @returns {Object} Request data
	 */
	i_getLastItemClassInRequestList(aRequestClass_str)
	{
		let lMax_int = -1;
		let lRequest_obj = {};

		for (var rid in this._requests_list)
		{
			
			let requestData = this._requests_list[rid];
			if (requestData && requestData.class == aRequestClass_str && requestData.rid > lMax_int)
			{
				lMax_int = requestData.rid;
				lRequest_obj = requestData;
			}
		}

		return  lRequest_obj;
	}

	isStubsModeAvailable()
	{
		return false;
	}

	isStubsMode()
	{
		return this._fIsStubsMode_bl;
	}

	getNextSubTime()
	{
		let lStubs_obj_arr = this._fStubs_obj_arr;

		if(this._fNextStubIndex_int >= lStubs_obj_arr.length)
		{
			return lStubs_obj_arr[lStubs_obj_arr.length - 2];
		}

		return lStubs_obj_arr[this._fNextStubIndex_int].date;
	}

	/**
	 * Indicaters whether an error message is fatal (by error severity type)
	 * @param {string} errorType 
	 * @returns {boolean}
	 */
	static isFatalError(errorType)
	{
		return errorType === ERROR_CODE_TYPES.FATAL_ERROR;
	}

	/**
	 * Indicaters whether an error message is general (by error severity type)
	 * @param {string} errorType 
	 * @returns {boolean}
	 */
	static isGeneralError(errorType)
	{
		return errorType === ERROR_CODE_TYPES.ERROR;
	}

	/**
	 * Indicaters whether an error message is warning (by error severity type)
	 * @param {string} errorType 
	 * @returns {boolean}
	 */
	static isWarning(errorType)
	{
		return errorType === ERROR_CODE_TYPES.WARNING;
	}

	/**
	 * Indicaters whether error code is an unknown wallet error or not.
	 * Workaround: consider all unknown Wallet error codes as an error BAD_BUYIN: 1010. Until the task is completed https://jira.dgphoenix.com/browse/DI-94
	 * @param {number} errorId - Target error code
	 * @returns {boolean}
	 */
	static isUnknownWalletError(errorId)
	{
		let lIsUnknownWalletError_bl = true;

		if (errorId >= ERROR_CODE_RANGES.WALLET_ERROR.from && errorId <= ERROR_CODE_RANGES.WALLET_ERROR.to)
		{
			switch(errorId)
			{
				case WebSocketInteractionController.ERROR_CODES.OPERATION_FAILED:
				case WebSocketInteractionController.ERROR_CODES.UNKNOWN_TRANSACTION_ID:
				case WebSocketInteractionController.ERROR_CODES.EXPIRED_WEBSITE_SESSION:
				case WebSocketInteractionController.ERROR_CODES.SW_PURCHASE_LIMIT_EXCEEDED:
				case WebSocketInteractionController.ERROR_CODES.INSUFFICIENT_FUNDS:
				case WebSocketInteractionController.ERROR_CODES.PREV_OPERATION_IS_NOT_COMPLETE:
					lIsUnknownWalletError_bl = false;
					break;
			}
		}
		else
		{
			lIsUnknownWalletError_bl = false;
		}

		return lIsUnknownWalletError_bl;
	}

	/**
	 * List of supported error codes
	 * @static
	 */
	static get ERROR_CODES ()
	{
		return SUPPORTED_ERROR_CODES;
	}

	/**
	 * Checks if there is any specific delayed request
	 * @param {string} aRequestClass_str - Request class name
	 * @param {object} aOptRequestParams_obj - Request params with specific values (optional parameter)
	 * @returns {boolean}
	 */
	hasDelayedRequests(aRequestClass_str, aOptRequestParams_obj=undefined)
	{
		return this._hasDelayedRequests(aRequestClass_str, aOptRequestParams_obj);
	}

	/**
	 * Checks if there is any specific unresponded request (request is sent but server still not responded)
	 * @param {string} aRequestClass_str - Request class name
	 * @param {object} aOptRequestParams_obj - Request params with specific values (optional parameter)
	 * @returns {boolean}
	 */
	hasUnRespondedRequest(aRequestClass_str, aOptRequestParams_obj=undefined)
	{
		return this._hasUnRespondedRequest(aRequestClass_str, aOptRequestParams_obj);
	}

	/**
	 * Checks if socket connection is opened
	 * @readonly
	 */
	get isConnectionOpened()
	{
		return this._isConnectionOpened;
	}

	constructor(optInfo)
	{
		super(optInfo ? optInfo : new WebSocketInteractionInfo());

		this._webSocket = null;
		this._requestUniqId = 0;
		this._requests_list = {};
		this._lastReconnectInterval = undefined;
		this._reconnectTimer = null;
		this._reconnectInProgress = false;
		this._recoverAfterServerShutdownRequired = false;
		this._requestsClassLastTimes_obj = {}; //keeps the time when the last request of particular type was sent
		this._blockedAfterCriticalError = false;
		this._delayedRequests = [];
		this._fIsStubsMode_bl = false;


		//STUBS...
		if(
			APP.isDebugMode &&
			this.isStubsModeAvailable()
			)
		{
			this._fIsStubsMode_bl = WebSocketInteractionController.STUBS_PATH !== undefined;
			this._fMessages_str_arr = [];//recordable game server responses for saving
			this._fStubs_obj_arr = [];//loadable server stubs
			this._fNextStubIndex_int = 0;


			if(this._fIsStubsMode_bl)
			{
				//LOADING STUBS...
				let lStubRequest_xhr = new XMLHttpRequest();
				lStubRequest_xhr.open('GET', WebSocketInteractionController.STUBS_PATH);
				lStubRequest_xhr.onreadystatechange = function()
				{

					if(!this._fStubs_obj_arr.length === 0)
					{
						return;
					}

					let lData_str = lStubRequest_xhr.responseText;
					let lRecoveredData_str_arr = lData_str.split("|");
					lRecoveredData_str_arr.pop();

					this._fStubs_obj_arr = [];
					for( let i = 0; i < lRecoveredData_str_arr.length; i++ )
					{
						let lMessage_obj =  JSON.parse(lRecoveredData_str_arr[i]);
						this._fStubs_obj_arr.push(lMessage_obj);
					}
				}.bind(this);

				lStubRequest_xhr.send();
				//...LOADING STUBS

				//STUBS MODE NOTIFICATION...
				let l_html = window.document.createElement('div');
				l_html.style.color = "white";
				l_html.style.position = "absolute";
				l_html.style.right = "0px";
				l_html.style.top = "0px";
				l_html.style.margin = "10px";
				l_html.style["padding-left"] = "10px";
				l_html.style["padding-right"] = "10px";
				l_html.style.height = "50px";
				l_html.style["background-color"] = "#ad0021";
				l_html.style["z-index"] = "999";

				l_html.style["text-align"] = "center";
				l_html.style["vertical-align"] = "middle";
				l_html.style["line-height"] = "50px";
				l_html.style["font-family"] = "calibri";
				l_html.style["font-weight"] = "bold";
				l_html.style["font-style"] = "italic";
				l_html.style["font-size"] = "40px";
				l_html.style["border-radius"] = "10px";
				l_html.style["cursor"] = "not-allowed";
				l_html.id = NOTIFICATION_PANEL_ID;

				l_html.innerText = "STUBS MODE";
				document.body.appendChild(l_html);
				//...STUBS MODE NOTIFICATION
			}
		}
		//...STUBS
	}

	//STUBS...
	setStubsTrackingMode()
	{
		if (
			APP.isDebugMode &&
			this.isStubsModeAvailable() &&
			!this._fIsStubsMode_bl &&
			!document.getElementById(DOWNLOAD_BUTTON_ID)
			)
		{
			this._fIsStubsTrackingRequired_bl = true;

			//DOWNLOAD BUTTON...
			let l_html = window.document.createElement('div');

			l_html.id = DOWNLOAD_BUTTON_ID;
			l_html.style.color = "white";
			l_html.style.position = "absolute";
			l_html.style.right = "0px";
			l_html.style.top = "0px";
			l_html.style.margin = "10px";
			l_html.style.width = "100px";
			l_html.style.height = "100px";
			l_html.style["background-color"] = "#7e5ad1";
			l_html.style["z-index"] = "999";

			l_html.style["text-align"] = "center";
			l_html.style["vertical-align"] = "middle";
			l_html.style["line-height"] = "100px";
			l_html.style["font-family"] = "calibri";
			l_html.style["font-weight"] = "bold";
			l_html.style["font-size"] = "80px";
			l_html.style["border-radius"] = "20px";
			l_html.style["cursor"] = "pointer";
			l_html.style["opacity"] = "1";

			l_html.innerText = "D";

		
			this.lDownloadButtonAnimationTimer_ref = null;
			
			l_html.addEventListener("click", this.onDownloadButtonClick.bind(this));

			document.body.appendChild(l_html);
			//...DOWNLOAD BUTTON
		}
	}


	onDownloadButtonClick()
	{
		this.saveStubs();

		//HTML BUTTON ANIMATION...
		let l_html = document.getElementById(DOWNLOAD_BUTTON_ID)

		let pos = 0;
		clearInterval(this.lDownloadButtonAnimationTimer_ref);

		this.lDownloadButtonAnimationTimer_ref = setInterval(
			function(){
				
				let lAlpha_num = Number(l_html.style.opacity);
				lAlpha_num -= 0.099;

				if(lAlpha_num <= 0)
				{
					clearInterval(this.lDownloadButtonAnimationTimer_ref);
					lAlpha_num = 1;
				}

				l_html.style["opacity"] = "" + lAlpha_num;

			}.bind(this), 1);

		//...HTML BUTTON ANIMATION
	}

	saveStubs()
	{
		//MERGING TEXT...
		let lData_str = "";

		for( let i = 0; i < this._fMessages_str_arr.length; i++ )
		{
			try
			{
				lData_str += this._fMessages_str_arr[i];
				lData_str += "|";
			}
			catch(err)
			{
				console.error(err);
				console.error("failed to stringify message #" + i +" of total ("+  this._fMessages_str_arr.length + "): ");
				console.error(this._fMessages_str_arr[i]);
				break;
			}	
		}
		//...MERGING TEXT

		//SAVING...
		let blob = new Blob([lData_str], {type: 'text/csv'});
		if(window.navigator.msSaveOrOpenBlob)
		{
			window.navigator.msSaveBlob(blob, "stubs.txt");
		}
		else
		{
			let l_html = window.document.createElement('a');
			l_html.href = window.URL.createObjectURL(blob);
			l_html.download = "stubs.txt";
			document.body.appendChild(l_html);
			l_html.click();
			document.body.removeChild(l_html);
		}
	    //...SAVING
	}


	_onStubsTick()
	{
		if(this._fNextStubIndex_int > this._fStubs_obj_arr.length - 1)
		{
			return;
		}

		let lCurrentStubsTime_num = this._currentStubsTime();
		let lStub_obj = this._fStubs_obj_arr[this._fNextStubIndex_int];

		if(lCurrentStubsTime_num >= lStub_obj.date)
		{
			this._processServerMessage(lStub_obj);
			this._fNextStubIndex_int++;

			let l_html = document.getElementById(NOTIFICATION_PANEL_ID);

			if(l_html)
			{
				l_html.innerText = "STUBS("+ this._fNextStubIndex_int + "/" + this._fStubs_obj_arr.length + ")";
			}
		}
	}

	get _currentStubsTime()
	{
		return Date.now();
	}
	//...STUBS

	/**
	 * Checks if socket connection recovering is in progress
	 * @readonly
	 */
	get recoveringConnectionInProgress ()
	{
		return this._reconnectInProgress;
	}

	__initModelLevel()
	{
		super.__initModelLevel();

		let socketUrl = APP.isDebugMode ? this._debugWebSocketUrl : this._webSocketUrl;
		socketUrl = decodeURIComponent(socketUrl);

		if (APP.isDebugMode && this._webSocketUrl.indexOf('dgphoenix') > -1)
		{
			socketUrl = socketUrl.replace('discreetgaming', 'dgphoenix');
		}

		this.info.socketUrl = socketUrl;
	}

	get _debugWebSocketUrl()
	{
		return ""; // must be overridden
	}

	get _webSocketUrl()
	{
		return APP.urlBasedParams.WEB_SOCKET_URL;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._establishConnection();
	}

	/**
	 * Checks if socket connection is opening
	 * @readonly
	 * @private
	 */
	get _isConnectionConnecting()
	{
		return this._webSocket && this._webSocket.readyState === WebSocket.CONNECTING;
	}

	/**
	 * Checks if socket connection is opened
	 * @readonly
	 * @private
	 */
	get _isConnectionOpened()
	{
		return this._webSocket && this._webSocket.readyState === WebSocket.OPEN;
	}

	/**
	 * Checks if socket connection is closing
	 * @readonly
	 * @private
	 */
	get _isConnectionClosing()
	{
		return this._webSocket && this._webSocket.readyState === WebSocket.CLOSING;
	}

	/**
	 * Checks if socket connection is closed
	 * @readonly
	 * @private
	 */
	get _isConnectionClosed()
	{
		return !this._webSocket || this._webSocket.readyState === WebSocket.CLOSED;
	}

	/**
	 * Sets server messages handling to be allowed
	 * @private
	 */
	_startServerMesagesHandling()
	{
		this.info.serverMessagesHandlingAllowed = true;


		//STUBS EXECUTION TIMER...
		if(
			this.isStubsModeAvailable() &&
			this._fIsStubsMode_bl
			)
		{
			APP.on(Application.EVENT_ON_TICK_TIME, this._onStubsTick, this);
		}
		//...STUBS EXECUTION TIMER
	}

	/**
	 * Sets server messages handling to be forbidden
	 * @private
	 */
	_stopServerMesagesHandling()
	{
		this.info.serverMessagesHandlingAllowed = false;
	}

	/**
	 * Stops reconnection and blocks next connection. New connection won't be established.
	 * @private
	 */
	_blockAfterCriticalError()
	{
		this._blockedAfterCriticalError = true;
		this._deactivateReconnectTimeout();
	}

	/**
	 * Establish new websocket connection and start messages handling
	 * @private
	 */
	_establishConnection()
	{
		this._closeConnectionIfPossible();
		
		if (this._blockedAfterCriticalError)
		{
			return;
		}

		let webSocket = new WebSocket(this.info.socketUrl);

		webSocket.onopen = this._onConnectionOpened.bind(this);
		webSocket.onclose = this._onConnectionClosed.bind(this);
		webSocket.onerror = this._onConnectionError.bind(this);
		webSocket.onmessage = this._onServerMessageReceived.bind(this);

		this._webSocket = webSocket;

		//https://youtrack.dgphoenix.com/issue/MSX-842
		// For some reasons it is needed only for iOS. Any other platform works fine with webSocket methods defined above.
		if (IOS) 
		{
			window.ononline = this._onOnlineRestored.bind(this);
			window.onoffline = this._onOffline.bind(this)
		}
	}

	_onOffline()
	{
		this._closeConnectionIfPossible();
		this.emit(WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, {wasClean: false});
	}

	_onOnlineRestored()
	{
		this._startRecoveringSocketConnection();
	}

	/**
	 * Close websocket connection
	 * @private
	 */
	_closeConnectionIfPossible()
	{
		this._requests_list = {};

		let webSocket = this._webSocket;
		
		if (!webSocket)
		{
			return;
		}
		
		this._clearSocketHandlers();
		this._clearDelayedRequests();
		
		if (this._isConnectionOpened)
		{
			webSocket.close();
		}

		this._webSocket = null;
	}

	/**
	 * Remove websocket handlers
	 */
	_clearSocketHandlers()
	{
		let webSocket = this._webSocket;

		if (webSocket)
		{
			webSocket.onopen = null;
			webSocket.onclose = null;
			webSocket.onerror = null;
			webSocket.onmessage = null;
		}
	}

	/**
	 * Remove delayed requests
	 */
	_clearDelayedRequests()
	{
		this._requestsClassLastTimes_obj = {};

		while (this._delayedRequests && this._delayedRequests.length)
		{
			let delayedRequestTimer = this._delayedRequests.pop();
			delayedRequestTimer.timer && delayedRequestTimer.timer.destructor();
		}
		this._delayedRequests = [];
	}

	/**
	 * Checks if there is any specific delayed request
	 * @private
	 * @param {string} aRequestClass_str - Request class name
	 * @param {object} aOptRequestParams_obj - Request params with specific values (optional parameter)
	 * @returns {boolean}
	 */
	_hasDelayedRequests(aRequestClass_str, aOptRequestParams_obj=undefined)
	{
		let lDelayedRequests = this._delayedRequests;
	
		if (!lDelayedRequests || !lDelayedRequests.length)
		{
			return false;
		}

		if (!aRequestClass_str)
		{
			return lDelayedRequests.length > 0;
		}

		for (let i=0; i<lDelayedRequests.length; i++)
		{
			let curDelayedRequestInfo = lDelayedRequests[i];
			if (curDelayedRequestInfo.class == aRequestClass_str)
			{
				if (!!aOptRequestParams_obj)
				{
					let lIsSuitableRequest_bl = true;
					for (let lParamName_str in aOptRequestParams_obj)
					{
						if (aOptRequestParams_obj[lParamName_str] !== curDelayedRequestInfo.data[lParamName_str])
						{
							lIsSuitableRequest_bl = false;
							break;
						}
					}

					if (lIsSuitableRequest_bl) return true;
				}
				else
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Removes delayed requests of specific class
	 * @protected
	 * @param {string} aRequestClass_str - Target request class
	 */
	_removeSpecificDelayedRequests(aRequestClass_str)
	{
		let lDelayedRequests = this._delayedRequests;

		if (!lDelayedRequests || !lDelayedRequests.length)
		{
			return;
		}

		for (let i=0; i<lDelayedRequests.length; i++)
		{
			let lCurDelayedRequest = lDelayedRequests[i];
			if (lCurDelayedRequest.class == aRequestClass_str)
			{
				lCurDelayedRequest.timer && lCurDelayedRequest.timer.destructor();
				lDelayedRequests.splice(i, 1);
				i--;
			}
		}
	}

	_onConnectionOpened()
	{
		console.log("[WSIC] _onConnectionOpened");
		this._stopReconnecting();
		this._recoverAfterServerShutdownRequired = false;

		this.info.connectionOpenClientTimeStamp = Date.now();

		this._startServerMesagesHandling();
		this.emit(WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED);
	}

	_onConnectionClosed(event)
	{
		let wasClean = event.wasClean;
		console.log("[WSIC] _onConnectionClosed, wasClean:", wasClean, "; code:", event.code);
		
		//MQBG-531 - Websocket Defined Status Code 1001 CLOSE_GOING_AWAY, client is leaving (browser tab closing)
		if(event.code == 1001)
		{
			this._processServerMessage({"code": 3,"msg": "New Lobby session is opening","date": Date.now(),"class": "Error","rid": -1});
		}

		this._stopServerMesagesHandling();
		this.emit(WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, {wasClean: wasClean});

		if (wasClean)
		{
			if (this._recoverAfterServerShutdownRequired)
			{
				this._startRecoveringSocketConnection();
			}
		}
		else
		{
			this._startRecoveringSocketConnection();
		}
	}

	_startRecoveringSocketConnection()
	{
		this._startReconnectingOnConnectionLost();
	}

	_onConnectionError(error)
	{
		console.log("[WSIC] _onConnectionError", error);
	}

	_onServerMessageReceived(message)
	{
		if (
			!this.info.serverMessagesHandlingAllowed &&
			!this._fIsStubsMode_bl
			)
		{
			return;
		}

		let data = JSON.parse(message.data);
		if (!data.class)
		{
			throw new Error (`Incorrect server message format: ${data}`);
		}

		//DEBUG...
		// if (data.class == "GetStartGameUrlResponse")
        // {
        //     testCounter++;
        //     if (testCounter == 1)
        //     {
        //         this._processServerMessage({"code": 1003,"msg": "ROOM_NOT_FOUND","date": 1496748898812,"class": "Error","rid": 1});
        //         return;
        //     }
        // }
		/*if (data.class == "GetRoomInfoResponse")
		{
			console.log("fake error ")
			this._processServerMessage({"code": 1004,"msg": "TOO_MANY_OBSERVERS","date": 1496748898812,"class": "Error","rid": 1});
			return;
		}*/

		// if (data.class == "GetStartGameUrlResponse")
		// {
		// 	testCounter++;
		// 	let code = testCounter > 1 ? 1004 : 1006;
		// 	this._processServerMessage({"code": code,"msg": "ROOM_NOT_FOUND","date": 1496748898812,"class": "Error","rid": 1});
		// 	return;
		// }

		// if (data.class == "BuyInResponse")
		// {
		// 	let code = 1026;//NOT_FATAL_BAD_BUYIN
		// 	this._processServerMessage({"code": code,"msg": "tst","date": Date.now(),"class": "Error","rid": data.rid});
		// 	return;
		// }

		// if (data.class == "EnterLobbyResponse")
		// {
		// 	// data.paytable.possibleBetLevels = [3, 5, 10];

		// 	setTimeout( () => { data.date += 5000; this._processServerMessage(data) }, 5000 )
		// 	return;
		// }

		// if (data.code === 3)
		// {
		// 	setTimeout( () => { console.log("code 3 process", data.code); data.date += 5000; this._processServerMessage(data) }, 5000 )
		// 	return;
		// }

		// if (data.class == "FullGameInfo")
		// {
		// 	data.betLevel = 3;	
		// 	if (data.seats)
		// 	{
		// 		for (let i=0; i<data.seats.length; i++)
		// 		{
		// 			data.seats[i].betLevel = 3;
		// 		}
		// 	}
		// }
		//...DEBUG
		
		this._processServerMessage(data);
	}

	_processServerMessage(messageData)
	{
		let requestData = null;
		let lIsStubMode_bl = this._fIsStubsMode_bl;
		
		if(!lIsStubMode_bl)
		{	
			if (messageData.rid && messageData.rid >= 0)
			{
				requestData = this._requests_list[messageData.rid];
				if (!!requestData)
				{
					requestData.responded = true;
				}
			}
		}

		if (!this.info.isLastServerMessageTimeDefined || messageData.date > this.info.lastServerMessageTime)
		{
			this.info.lastServerMessageTime = messageData.date;
		}

		if (this._isMessageCompleteDisregardRequired(messageData))
		{
			return;
		}

		if (APP.isDebugsrmsg)
		{
			console.log(`[E] RECI:: rid:${messageData.rid}, class:${messageData.class}, date:${messageData.date}`);
		}

		if(messageData.class == SERVER_MESSAGES.ENTER_LOBBY_RESPONSE)
		{
			APP.isLobbyApp();
			APP.on(Application.EVENT_ON_CLOSE_GAME_SESSION, this._finishGameSession, this);
		}

		this.emit(WebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, {messageData: messageData, requestData: requestData});

		let eventType = this._specifyEventMessageType(messageData);
		let eventData = this._specifyEventData(messageData, requestData);

		if (eventType === WebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE)
		{
			APP.logger.i_pushError(`WSIC. Internal error! ${JSON.stringify(messageData)}`);
		}

		if (
				lIsStubMode_bl ||
				(
					eventType !== undefined &&
					this._isServerMessageReceivingAvailable(messageData.class)
					&& this.info.serverMessagesHandlingAllowed
				)
			)
		{
			this.emit(eventType, eventData);
		}

		this._handleServerMessage(messageData, requestData);
	}

	_finishGameSession(event)
	{
		this._sendRequest(WebSocketInteractionController.FINISH_WEB_SESSION, APP.goToHomeParams);
	}

	_isMessageCompleteDisregardRequired(messageData)
	{
		return false;
	}

	_specifyEventMessageType(messageData)
	{
		let eventType;
		switch(messageData.class)
		{
			case SERVER_MESSAGES.ERROR:
				eventType = WebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE;
				break;
			case SERVER_MESSAGES.OK:
				eventType = WebSocketInteractionController.EVENT_ON_SERVER_OK_MESSAGE;
				break;
			case SERVER_MESSAGES.FINISH_GAME_SESSION_RESPONSE:
				eventType = WebSocketInteractionController.EVENT_ON_GAME_SESSION_FINISHED;
				break;
		}

		return eventType;
	}

	_specifyEventData(messageData, requestData)
	{
		let eventData = {messageData: messageData, requestData: requestData};
		switch(messageData.class)
		{
			case SERVER_MESSAGES.ERROR:
				eventData.errorType = this._specifyErrorCodeSeverity(messageData, requestData);
				break;
		}

		return eventData;
	}

	_specifyErrorCodeSeverity(messageData, requestData)
	{
		let errorCode = messageData.code;
		let errorCodeSeverity;
		if (
				(
					errorCode >= ERROR_CODE_RANGES.FATAL_ERROR.from 
					&& errorCode <= ERROR_CODE_RANGES.FATAL_ERROR.to
					&& errorCode != WebSocketInteractionController.ERROR_CODES.SERVER_SHUTDOWN
				)
				|| errorCode == WebSocketInteractionController.ERROR_CODES.BAD_REQUEST
				|| errorCode == WebSocketInteractionController.ERROR_CODES.BAD_BUYIN
				|| errorCode == WebSocketInteractionController.ERROR_CODES.BAD_STAKE
				|| errorCode == WebSocketInteractionController.ERROR_CODES.AVATAR_PART_NOT_AVAILABLE
				|| errorCode == WebSocketInteractionController.ERROR_CODES.OPERATION_FAILED
				|| errorCode == WebSocketInteractionController.ERROR_CODES.EXPIRED_WEBSITE_SESSION
				|| errorCode == WebSocketInteractionController.ERROR_CODES.UNKNOWN_TRANSACTION_ID
				|| WebSocketInteractionController.isUnknownWalletError(errorCode) //Workaround: consider all unknown Wallet error codes as an error BAD_BUYIN: 1010. Until the task is completed https://jira.dgphoenix.com/browse/DI-94
			)
		{
			errorCodeSeverity = ERROR_CODE_TYPES.FATAL_ERROR;
		}
		else if (
					(errorCode >= ERROR_CODE_RANGES.ERROR.from && errorCode <= ERROR_CODE_RANGES.ERROR.to)
					|| errorCode == WebSocketInteractionController.ERROR_CODES.SERVER_SHUTDOWN
					|| errorCode == WebSocketInteractionController.ERROR_CODES.SERVER_REBOOT
				)
		{
			errorCodeSeverity = ERROR_CODE_TYPES.ERROR;
		}
		else if (errorCode >= ERROR_CODE_RANGES.WARNING.from && errorCode <= ERROR_CODE_RANGES.WARNING.to)
		{
			errorCodeSeverity = ERROR_CODE_TYPES.WARNING;
		}
		else if (errorCode >= ERROR_CODE_RANGES.WALLET_ERROR.from && errorCode <= ERROR_CODE_RANGES.WALLET_ERROR.to)
		{
			errorCodeSeverity = ERROR_CODE_TYPES.ERROR;
		}
		else
		{
			throw new Error(`Can't specify error type: ${messageData.code}`);
		}

		return errorCodeSeverity;
	}

	/**
	 * Indicates whether error code is fatal or not (by error code)
	 * @param {number} errorCode - Target error code
	 * @param {*} aOptRequestData_obj - Request data (optional parameter)
	 * @returns {boolean}
	 */
	isFatalError(errorCode, aOptRequestData_obj)
	{
		let lErrorType_str = this._specifyErrorCodeSeverity({code: errorCode}, aOptRequestData_obj || undefined);

		return WebSocketInteractionController.isFatalError(lErrorType_str);
	}

	_isServerMessageReceivingAvailable(messageClass)
	{
		return true;
	}

	_handleServerMessage(messageData, requestData)
	{
		let msgClass = messageData.class;
		switch(msgClass)
		{
			case SERVER_MESSAGES.ERROR:
				console.log("General ServerError " + JSON.stringify(messageData));
				let errorType = this._specifyErrorCodeSeverity(messageData, requestData);
				if (WebSocketInteractionController.isFatalError(errorType))
				{
					this._handleFatalError(messageData.code, requestData);
				}
				else if (WebSocketInteractionController.isGeneralError(errorType))
				{
					this._handleGeneralError(messageData.code, requestData);
				}
				else if (WebSocketInteractionController.isWarning(errorType))
				{
					this._handleWarning(messageData.code, requestData);
				}
				break;
		}

		
		
		//STUBS...
		if(
			this._fIsStubsTrackingRequired_bl &&
			this.isStubsModeAvailable() &&
			!this._fIsStubsMode_bl
			)
		{
			//recording server messages to save them later if needed
			this._fMessages_str_arr.push(JSON.stringify(messageData, getCircularReplacer()));
		}
		//...STUBS
	}

	_handleFatalError(errorCode, requestData)
	{
		this._blockAfterCriticalError();
		this._stopServerMesagesHandling();
		this._closeConnectionIfPossible();
	}

	_handleGeneralError(errorCode, requestData)
	{
		let supported_codes = WebSocketInteractionController.ERROR_CODES;
		switch(errorCode)
		{
			case supported_codes.SERVER_SHUTDOWN:
			case supported_codes.SERVER_REBOOT:
				this._recoverAfterServerShutdownRequired = true;
				break;
			case supported_codes.REQUEST_FREQ_LIMIT_EXCEEDED:
				switch (requestData.class)
				{
					case GAME_CLIENT_MESSAGES.SHOT:
					case GAME_CLIENT_MESSAGES.MINE_COORDINATES:
						// nothing to do
						break;
					default:
						// re-send request
						this._presetResend(requestData);
						break;
				}
				break;
		}
	}

	_handleWarning(errorCode, requestData)
	{
	}

	_presetResend(requestData)
	{
		let requestDate = requestData.date;
		let timeDelay = this._calculateRequestSendDelay(requestData, requestData.date);
		
		if (isNaN(timeDelay))
		{
			this._resendRequest(requestData.class, requestData);
		}
		else
		{
			this._delayRequest(requestData.class, requestData, timeDelay);
		}
	}

	_resendRequest(requestClass, requestData)
	{
		requestData.date = undefined;
		this._sendRequest(requestClass, requestData);
	}

	_sendRequest(requestClass, requestData)
	{
		if(this._fIsStubsMode_bl)
		{
			return;
		}

		if (!this._isConnectionOpened)
		{
			return;
		}

		if(APP.areRequestsBlocked)
		{
			return;
		}

		requestData.class = requestClass;

		let timeDelay = this._calculateRequestSendDelay(requestData);
		if (!isNaN(timeDelay))
		{
			this._delayRequest(requestClass, requestData, timeDelay);
			return;
		}

		requestData.rid = ++this._requestUniqId;

		this.__performActionWithRequestOnGameLevel(requestData);

		if(requestData.date === undefined)
		{
			requestData.date = Date.now();
		}
		
		this._requests_list[requestData.rid] = requestData;

		if (APP.isDebugsrmsg)
		{
			console.log(`[E] SEND:: rid:${requestData.rid}, class:${requestData.class}, date:${requestData.date}`);
		}
		
		let lRequestDataToSend = Object.assign({}, requestData);
		
		delete lRequestDataToSend.excludeParams;
		
		let request = JSON.stringify(lRequestDataToSend);

		requestData.responded = false;

			this._webSocket.send(request);

		this._requestsClassLastTimes_obj[requestClass] = requestData.date;

		this.emit(WebSocketInteractionController.EVENT_ON_GAME_CLIENT_SENT_MESSAGE, lRequestDataToSend);
	}

	/**
	 * Calculates time delay before sending the request.
	 * @param {object} requestData - Request data that is delayed.
	 * @param {number} aRequestDate_int - Last request timestamp (if request was sent previously), optional parameter.
	 * @returns {number}
	 */
	_calculateRequestSendDelay(requestData, aRequestDate_int=undefined)
	{
		let requestClass = requestData.class;
		let timeDelay = undefined;

		let lastRequestSendTime = aRequestDate_int || this._requestsClassLastTimes_obj[requestClass];
		if (lastRequestSendTime)
		{
			let lCurClientTime_int = Date.now();
			let timeDiff = lCurClientTime_int - lastRequestSendTime;
			let requestTimeLimit = this.info.getRequestTimeLimit(requestClass);

			let lRequestDifferentTypesTimeLimit_obj = this.info.getRequestDifferentTypesTimeLimit(requestData);
			let lRequestDifferentTypesTimeLimit_int = !!lRequestDifferentTypesTimeLimit_obj ? lRequestDifferentTypesTimeLimit_obj.interval : undefined;
			let lRequestDifferentTypesMinDiff_int = undefined;
			if (lRequestDifferentTypesTimeLimit_int !== undefined)
			{
				let lRequestClasses_str_arr = lRequestDifferentTypesTimeLimit_obj.requestClasses;
				for (let i=0; i<lRequestClasses_str_arr.length; i++)
				{
					let lastPairRequestTime = this._requestsClassLastTimes_obj[lRequestClasses_str_arr[i]];
					if (lastPairRequestTime)
					{
						let lCurDiff_int = lCurClientTime_int - lastPairRequestTime;
						if (lRequestDifferentTypesMinDiff_int === undefined)
						{
							lRequestDifferentTypesMinDiff_int = lCurDiff_int;
						}
						else
						{
							lRequestDifferentTypesMinDiff_int = Math.min(lRequestDifferentTypesMinDiff_int, lCurDiff_int);
						}
					}
				}

				if (lRequestDifferentTypesMinDiff_int === undefined)
				{
					lRequestDifferentTypesMinDiff_int = lRequestDifferentTypesTimeLimit_int+1;
				}
			}

			if (timeDiff < requestTimeLimit)
			{
				timeDelay = Math.max(requestTimeLimit - timeDiff + 1, 0);
			}

			if (lRequestDifferentTypesTimeLimit_int !== undefined && lRequestDifferentTypesMinDiff_int < lRequestDifferentTypesTimeLimit_int)
			{
				timeDelay = Math.max(lRequestDifferentTypesTimeLimit_int - lRequestDifferentTypesMinDiff_int + 1, timeDelay || 0);
			}
		}

		return timeDelay;
	}

	__performActionWithRequestOnGameLevel()
	{
	}

	/**
	 * Gets timestamp of last sendt request of specific class.
	 * @param {string} aRequestClass_str - Target request class.
	 * @returns {number}
	 */
	getLastRequestSendTime(aRequestClass_str)
	{
		return this._requestsClassLastTimes_obj[aRequestClass_str] || undefined;
	}

	/**
	 * Gets rid of last sent request
	 */
	getLastRequestId()
	{
		return this._requestUniqId;
	}

	_delayRequest(requestClass, requestData, delayTime)
	{
		let requestInfo = {
			class: requestClass,
			data: requestData
		}

		if (!isNaN(delayTime))
		{
			let timer = new Timer(() => { 
									this._removeDelayedTimer(requestInfo);
									this._resendRequest(requestClass, requestData) 
								}, 
						delayTime);

			requestInfo.timer = timer;
		}
		this._delayedRequests.push(requestInfo);
	}

	_forceDelayedRequests()
	{
		let tmpDelayedRequests = this._delayedRequests.slice();
		this._delayedRequests = [];

		while (tmpDelayedRequests && tmpDelayedRequests.length)
		{
			let delayedRequestInfo = tmpDelayedRequests.shift();
			let delayedRequestTimer = delayedRequestInfo.timer;
			delayedRequestTimer && delayedRequestTimer.finish();
		}		
	}

	_removeDelayedTimer(requestInfo)
	{
		if (!requestInfo)
		{
			return;
		}

		let timerIndex = this._delayedRequests.indexOf(requestInfo);
		if (timerIndex >= 0)
		{
			this._delayedRequests.splice(timerIndex, 1);
		}

		requestInfo.timer && requestInfo.timer.destructor();
	}

	/**
	 * Checks if there is any specific unresponded request (request is sent but server still not responded)
	 * @private
	 * @param {string} aRequestClass_str - Request class name
	 * @param {object} aOptRequestParams_obj - Request params with specific values (optional parameter)
	 * @returns {boolean}
	 */
	_hasUnRespondedRequest(requestClass, aOptRequestParams_obj=undefined)
	{
		for (var rid in this._requests_list)
		{
			let requestData = this._requests_list[rid];
			if (requestData && requestData.class == requestClass && !requestData.responded)
			{
				if (!!aOptRequestParams_obj)
				{
					let lIsSuitableRequest_bl = true;
					for (let lParamName_str in aOptRequestParams_obj)
					{
						if (aOptRequestParams_obj[lParamName_str] !== requestData[lParamName_str])
						{
							lIsSuitableRequest_bl = false;
							break;
						}
					}
					
					if (lIsSuitableRequest_bl) return true;
				}
				else
				{
					return true;
				}
			}
		}
		return false;
	}

	_startReconnectingOnConnectionLost()
	{
		if (this._blockedAfterCriticalError)
		{
			console.log("[WSIC] _startReconnectingOnConnectionLost blocked after critical error");
			return;
		}

		this._reconnectInProgress = true;
		this._recoverAfterServerShutdownRequired = false;

		this._deactivateReconnectTimeout();					
		this._activateReconnectTimeout();
	}

	_activateReconnectTimeout()
	{
		var reconnectInterval = this._lastReconnectInterval = this._calculateReconnectInterval();
		console.log("[WSIC] _activateReconnectTimeout", reconnectInterval);
		this._reconnectTimer = setTimeout(this._onReconnectTimeoutCompleted.bind(this), reconnectInterval);
		APP.logger.i_pushDebug(`WSIC. _activateReconnectTimeout with interval ${this._lastReconnectInterval}`);
	}

	_calculateReconnectInterval()
	{
		let interval;
		if (isNaN(this._lastReconnectInterval))
		{
			interval = ~~(Math.random() * FIRST_RECONNECT_INTERVAL_MAX_SECONDS * 1000);
			if (interval < MIN_RECONNECT_INTERVAL)
			{
				interval = MIN_RECONNECT_INTERVAL;
			}
		}
		else
		{
			interval = this._lastReconnectInterval * 2;
		}

		return interval;
	}

	_onReconnectTimeoutCompleted()
	{
		this._reconnectOnConnectionLost();
	}
	
	


	_deactivateReconnectTimeout()
	{
		if (!this._reconnectTimer)
		{
			return;
		}

		clearTimeout(this._reconnectTimer);
	}

	_reconnectOnConnectionLost()
	{
		this._deactivateReconnectTimeout();
		this._establishConnection();
	}

	_stopReconnecting()
	{
		this._lastReconnectInterval = undefined;
		this._reconnectInProgress = false;
					
		this._deactivateReconnectTimeout();
	}

	_clearRequestList()
	{
		this._requests_list = {};
	}
}

export default WebSocketInteractionController