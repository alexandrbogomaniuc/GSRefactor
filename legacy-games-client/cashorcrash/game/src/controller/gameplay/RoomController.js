import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import GameWebSocketInteractionController from '../interaction/server/GameWebSocketInteractionController';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { CLIENT_MESSAGES, SERVER_MESSAGES } from '../../model/interaction/server/GameWebSocketInteractionInfo';
import { ROOM_STATES } from '../../model/gameplay/RoomInfo';
import GamePlayersController from '../gameplay/players/GamePlayersController';
import { PLAYER_CAF_STATUS } from '../../model/uis/custom/dialogs/custom/BattlegroundCafRoomManagerDialogInfo';
import { PLAYER_PARAMS } from '../../model/uis/custom/dialogs/custom/BattlegroundCafRoomGuestDialogInfo';

/**
 * Controls entering to the room.
 * @class
 * @extends SimpleController
 */
class RoomController extends SimpleController
{
	static get EVENT_ON_ROOM_STATE_CHANGED() 							{return "EVENT_ON_ROOM_STATE_CHANGED";}
	static get EVENT_ON_ROOM_ID_CHANGED() 								{return "EVENT_ON_ROOM_ID_CHANGED";}
	static get EVENT_ON_RAKE_DEFINED()									{return "EVENT_ON_RAKE_DEFINED";}
	static get EVENT_ON_PRIVATE_BATTLEGROUND_START_GAME_URL_REQUEST()   {return "EVENT_ON_PRIVATE_BATTLEGROUND_START_GAME_URL_REQUEST"};
	static get EVENT_ACTIVATE_KICKED_DIALOG()							{return "EVENT_ACTIVATE_KICKED_DIALOG"};
	
	constructor(aOptInfo_usi, aOptParentController_usc)
	{
		super(aOptInfo_usi, aOptParentController_usc);
	}

	init()
	{
		super.init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onServerConnectionClosed, this);
		//webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_GET_START_GAME_URL_RESPONSE_MESSAGE, this._onServerStartGameUrlBattlegroundResponseMessage, this);


		this._fGamePlayersController_gpsc = APP.gameController.gameplayController.gamePlayersController;
		this._fGamePlayersController_gpsc.on(GamePlayersController.EVENT_ON_MASTER_PLAYER_OUT, this._onMasterPlayerOut, this);
		this._fGamePlayersController_gpsc.on(GamePlayersController.EVENT_ON_REJOIN, this._onRejoin, this);
		
	}

	_onServerStartGameUrlBattlegroundResponseMessage(event)
	{
		console.log("CAF: _onServerStartGameUrlBattlegroundResponseMessage");
		this._openRoom();
	}


	_onServerMessage(event)
	{
		let data = event.messageData;
		let requestData = event.requestData;
		switch(data.class)
		{
			case SERVER_MESSAGES.ENTER_GAME_RESPONSE:
				this._updateRoomIdIfRequired(data.roomId);

			
				if(APP.isCAFMode){
					// here is GetPrivateBattlegroundStartGameUrl
					//EVENT_ON_PRIVATE_BATTLEGROUND_START_GAME_URL_REQUEST
					this.emit(RoomController.EVENT_ON_PRIVATE_BATTLEGROUND_START_GAME_URL_REQUEST);
				}else{
					this._openRoom();
				}

				
				break;
			case SERVER_MESSAGES.CRASH_GAME_INFO:

				if (APP.isBattlegroundGame && APP.isCAFMode){
					//const friendssMock = [{"nickname":"Mock1","online":true},{"nickname":"Mock2","online":true},{"nickname":"Mock3","online":true},{"nickname":"Mock4","online":true},{"nickname":"Mock5","online":true},{"nickname":"Mock6","online":true},{"nickname":"Mock7","online":true},{"nickname":"Mock8","online":true},{"nickname":"Mock9","online":true},{"nickname":"Mock10","online":true},{"nickname":"Mock11","online":true},{"nickname":"Mock12","online":true},{"nickname":"Mock13","online":true},{"nickname":"Mock14","online":true},{"nickname":"Mock15","online":true},{"nickname":"Mock16","online":true},{"nickname":"Mock17","online":true},{"nickname":"Mock18","online":true},{"nickname":"Mock19","online":true},{"nickname":"Mock20","online":true},{"nickname":"Mock21","online":true},{"nickname":"Mock22","online":true},{"nickname":"Mock23","online":true}]				

					this._updateFriends(data.friends || []);
					//this._updateFriends(friendssMock);
					//const observersMock = [{"nickname":"Mock1","isKicked":false,"status":"WAITING","isOwner":true},{"nickname":"zeljko33","isKicked":false,"status":"WAITING"},{"nickname":"Mock3","isKicked":false,"status":"INVITED"},{"nickname":"Mock4","isKicked":false,"status":"INVITED"},{"nickname":"Mock5","isKicked":false,"status":"INVITED"},{"nickname":"Mock6","isKicked":false,"status":"INVITED"},{"nickname":"Mock7","isKicked":false,"status":"INVITED"},{"nickname":"Mock8","isKicked":false,"status":"INVITED"},{"nickname":"Mock9","isKicked":false,"status":"INVITED"},{"nickname":"Mock10","isKicked":false,"status":"INVITED"},{"nickname":"Mock11","isKicked":false,"status":"INVITED"},{"nickname":"Mock12","isKicked":false,"status":"INVITED"},{"nickname":"Mock13","isKicked":false,"status":"INVITED"},{"nickname":"Mock14","isKicked":false,"status":"INVITED"},{"nickname":"Mock15","isKicked":false,"status":"INVITED"},{"nickname":"Mock16","isKicked":false,"status":"INVITED"},{"nickname":"Mock17","isKicked":false,"status":"INVITED"},{"nickname":"Mock18","isKicked":false,"status":"INVITED"},{"nickname":"Mock19","isKicked":false,"status":"INVITED"},{"nickname":"Mock20","isKicked":false,"status":"INVITED"},{"nickname":"Mock21","isKicked":false,"status":"INVITED"},{"nickname":"Mock22","isKicked":false,"status":"INVITED"},{"nickname":"Mock23","isKicked":false,"status":"INVITED"}]				
					this._updateObservers(data.observers || []);
					APP.gameController.gameplayController.roomController.info.pendingInvite = false;
					APP.gameController.gameplayController.roomController.info.maxRoomPlayers = data.maxRoomPlayers;
					//this._updateObservers(observersMock);
					this.info.minSeats = data.minSeats;

					for(var i =0; i<this.info.observers.length; i++){
						const observer = this.info.observers[i];
						
						if(observer.nickname == APP.ownerNickname)
						{
							
							if(observer[PLAYER_PARAMS.STATUS]== PLAYER_CAF_STATUS.KICKED || observer[PLAYER_PARAMS.IS_KICKED] == true)
							{
								
								this.info.isKickedOutOfTheRoom = true;
								const gameControllerInfo = APP.gameController.info;
								gameControllerInfo.battlegroundBuyInConfirmed = false;
								this.emit(RoomController.EVENT_ACTIVATE_KICKED_DIALOG, {status:true});
							}else{
								
								if(APP.isKicked){
									this.info.isKickedOutOfTheRoom = false;
									this.emit(RoomController.EVENT_ACTIVATE_KICKED_DIALOG, {status:false});
								}
							}
						}
					}
				}

				

				this._updateRoomIdIfRequired(data.roomId);

				if (this.info.isRoomOpeningState)
				{
					this._updateRoomStateIfRequired(ROOM_STATES.OPENED);
				}
				
				if (data.rid != -1 && data.rakePercent !== undefined)
				{
					this._updateRoomRake(data.rakePercent);
				}
				break;
			case SERVER_MESSAGES.OK:
				if (requestData && requestData.class == CLIENT_MESSAGES.CLOSE_ROOM)
				{
					this._updateRoomStateIfRequired(ROOM_STATES.CLOSED);
				}
				break;
			case SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_REJECTED_RESPONSE:
				if (
					data.errorCode !== undefined 
					&& (
							APP.webSocketInteractionController.isFatalError(data.errorCode)
							|| data.errorCode === GameWebSocketInteractionController.ERROR_CODES.PREV_OPERATION_IS_NOT_COMPLETE
						)
					)
				{
					this.info.resetRoomState();
				}
				break;
			case SERVER_MESSAGES.SIT_IN_RESPONSE:
				if (data.rid != -1 && data.rakePercent !== undefined)
				{
					this._updateRoomRake(data.rakePercent);
				}
				break;
			case SERVER_MESSAGES.GET_START_GAME_URL_RESPONSE:
					this._updateRoomIdIfRequired(data.roomId);
					this._onServerStartGameUrlBattlegroundResponseMessage(event);
				break;
		}
	}

	_updateFriends(data)
	{
		this.info.friends = data;
	}

	_updateObservers(data)
	{
		this.info.observers  = data;
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let errorType = event.errorType;

		if (GameWebSocketInteractionController.isFatalError(errorType))
		{
			this.info.resetRoomState();
			return;
		}

		switch (serverData.code)
		{
			case GameWebSocketInteractionController.ERROR_CODES.NEED_SITOUT:
				if (requestData && requestData.class == CLIENT_MESSAGES.CLOSE_ROOM)
				{
					if (!this._fGamePlayersController_gpsc.info.isMasterSeatDefined)
					{
						this._closeRoom(true);
					}
				}
				break;

			case GameWebSocketInteractionController.ERROR_CODES.TOO_MANY_OBSERVERS:
			case GameWebSocketInteractionController.ERROR_CODES.TOO_MANY_PLAYER:
				this.info.resetRoomState();
				break;
		}
	}

	_onServerConnectionClosed(event)
	{
		this.info.resetRoomState();
	}

	_onMasterPlayerOut(event)
	{
		if (this._fGamePlayersController_gpsc.info.isMasterPlayerLeaveRoomTriggered)
		{
			this._closeRoom();
		}
	}

	_onRejoin()
	{
		this._openRoom()
	}

	/**
	 * Open room.
	 * @private
	 */
	_openRoom()
	{
		this._updateRoomStateIfRequired(ROOM_STATES.OPENING);
	}

	/**
	 * Close room.
	 * @private
	 */
	_closeRoom(aOptForced_bl=false)
	{
		this._updateRoomStateIfRequired(ROOM_STATES.CLOSING, aOptForced_bl);
	}

	//ROOM ID...
	_updateRoomIdIfRequired(aRoomId_num)
	{
		if (!this.info.isRoomIdDefined)
		{
			this._updateRoomId(aRoomId_num)
			return;
		}

		let lNewRoomId_num = aRoomId_num;
		let lCurRoomId_num = this.info.roomId;

		if (lNewRoomId_num !== lCurRoomId_num)
		{
			this._updateRoomId(lNewRoomId_num);
		}
	}

	_updateRoomId(aNewRoomId_num)
	{
		this.info.roomId = aNewRoomId_num;

		this.emit(RoomController.EVENT_ON_ROOM_ID_CHANGED);
	}
	//...ROOM ID

	//ROOM STATE...
	/**
	 * Update room state if changed.
	 * @param {string} aRoomState_str - Expected room state. Possible room states: RoomInfo#ROOM_STATES
	 * @param {boolean} aOptForced_bl - Forced update supposes to update state (and fire corresponding event) even is state is not actually changed.
	 * @private
	 */
	_updateRoomStateIfRequired(aRoomState_str, aOptForced_bl=false)
	{
		if (!this.info.isRoomStateDefined)
		{
			this._updateRoomState(aRoomState_str)
			return;
		}

		let lNewRoomState_str = aRoomState_str;
		let lCurRoomState_str = this.info.roomState;

		if (lNewRoomState_str !== lCurRoomState_str || aOptForced_bl)
		{
			this._updateRoomState(lNewRoomState_str);
		}
	}

	/**
	 * Set room state and fire event.
	 * @param {string} aNewRoomState_str - New room state. Possible room states: RoomInfo#ROOM_STATES
	 * @private
	 */
	_updateRoomState(aNewRoomState_str)
	{
		this.info.roomState = aNewRoomState_str;

		this.emit(RoomController.EVENT_ON_ROOM_STATE_CHANGED);

		console.log("Room State: " + this.info.roomState);

		let battlegroundCafRoomManagerDialogControllerInfo = APP.dialogsController._cafGuestController.info;
		
	}
	//...ROOM STATE

	/**
	 * Update room rake.
	 * @param {number} aNewRake_num 
	 * @private
	 */
	_updateRoomRake(aNewRake_num)
	{
		let lNewRake_num = +aNewRake_num;
		let lCurRake_num = this.info.rakePercent;

		if (this.info.rakePercent === undefined || lNewRake_num !== lCurRake_num)
		{
			this.info.rakePercent = lNewRake_num;
			this.emit(RoomController.EVENT_ON_RAKE_DEFINED);
		}
	}
}

export default RoomController;