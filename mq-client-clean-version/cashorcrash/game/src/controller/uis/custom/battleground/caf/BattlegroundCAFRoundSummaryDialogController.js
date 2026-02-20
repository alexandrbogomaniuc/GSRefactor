import DialogController from '../../dialogs/DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';
import {GAME_MESSAGES} from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
//import LobbyExternalCommunicator from '../../../../../external/LobbyExternalCommunicator';
//import LobbyApp from '../../../../../LobbyAPP';
//import BattlegroundController from '../../../../custom/battleground/BattlegroundController';
import SecondaryScreenController from '../../secondary/SecondaryScreenController';
import RoomController from '../../../../gameplay/RoomController';
import GamePlayersController from '../../../../gameplay/players/GamePlayersController';

class BattlegroundCAFRoundSummaryDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };
	static get EVENT_DIALOG_ACTIVATED()	{ return DialogController.EVENT_DIALOG_ACTIVATED };
	static get EVENT_DIALOG_DEACTIVATED() { return DialogController.EVENT_DIALOG_DEACTIVATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);
	}

	__init ()
	{
		super.__init()

	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider()._roundAlreadyStartedDialogView;
	}

	__initModelLevel ()
	{
		super.__initModelLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		if (!APP.isCAFMode)
		{
			return;
		}

		let gamePlayersController = this._gamePlayersController = APP.gameController.gameplayController.gamePlayersController;
		gamePlayersController.on(GamePlayersController.EVENT_ACTIVATE_CAF_ROOM_MANAGER, this._eventActivateRoomManager, this);
		gamePlayersController.on(GamePlayersController.EVENT_SHOW_CAF_SUMMARY, this._eventShowSummary, this);
		// DEBUG...
		//window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}

	
	_eventShowSummary(event)
	{
		if(event.open && !this.info.isActive )
		{
			this.__activateDialog();
			
		}
	}


	_eventActivateRoomManager(event)
	{
		if(event.open && this.info.isActive )
		{
			this.__deactivateDialog();
			
		}
	}

	_eventActivate(event)
	{
		
		var doOpen = event.status; 
		console.log("activate round already started dialog " + doOpen);
		if(APP.isCAFRoomManager) return ; 
		

		if(doOpen){
			if(!this.info.isActive){
				this.__activateDialog();
			}
		}else{
			if(this.info.isActive){
				this.__deactivateDialog();
			}
		}
		
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	//DEBUG...
	/*keyDownHandler(keyCode)
	{
		if (keyCode.keyCode == 68) //d
		{
			this.__activateDialog();
		}
	}*/
	//...DEBUG

	_onServerEnterLobbyMessage(event)
	{
		this._startHandleEnvironmentMessages();
	}

	_startHandleEnvironmentMessages()
	{
		
		/*this._fPlayerController_pc = APP.playerController;
		this._fPlayerInfo_pi = this._fPlayerController_pc.info;
		console.log("start handling env messages " );
		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		APP.on(LobbyApp.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);

		let l_ssc = this._fSecondaryScreenController_ssc = APP.secondaryScreenController;
		l_ssc.on(SecondaryScreenController.EVENT_SCREEN_ACTIVATED, this.onSecondaryScreenActivated, this);
		l_ssc.on(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this.onSecondaryScreenDeactivated, this);*/
	}

	/*_onPlayerInfoUpdated(event)
	{
		if (APP.isCAFRoomManagerDefined && APP.isCAFRoomManager)
		{
			this._stopHandleEnvironmentMessages();
		}

		if (event.data.isKicked !== undefined)
		{
			if (this._fPlayerInfo_pi.isKicked)
			{
				this.__activateDialog();
			}
			else
			{
				if (this.info.isActive)
				{
					this.__deactivateDialog();
				}
			}
		}
	}*/

	_stopHandleEnvironmentMessages()
	{
		/*APP.off(LobbyApp.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this, true);
		webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.off(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		let l_ssc = this._fSecondaryScreenController_ssc;
		l_ssc.off(SecondaryScreenController.EVENT_SCREEN_ACTIVATED, this.onSecondaryScreenActivated, this);
		l_ssc.off(SecondaryScreenController.EVENT_SCREEN_DEACTIVATED, this.onSecondaryScreenDeactivated, this);*/
	}

	_onLobbyServerConnectionClosed(event)
	{
		this.__deactivateDialog();
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;

		/*switch (msgType)
		{
			case GAME_MESSAGES.SERVER_CONNECTION_CLOSED:
				this.__deactivateDialog();
				break;

			case GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST:
				let l_bi = APP.battlegroundController.info;
				if (l_bi.isRoundInProgress && !APP.playerController.info.isKicked)
				{
					this.__deactivateDialog();
					break;
				}
				
		}*/
	}

	_onBattlegroundRoundProgressStateChanged(event)
	{
		//if(!APP.playerController.info.isKicked)this.__deactivateDialog();
	}

	onSecondaryScreenActivated(event)
	{
		this.__deactivateDialog();
	}

	onSecondaryScreenDeactivated(event)
	{
		this.__activateDialog();
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		var view = this.__fView_uo;

		//buttons configuration...
		view.setCancelMode();
		//...buttons configuration

		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog()
	{
		super.__deactivateDialog();
	}

	__onDialogCancelButtonClicked(event)
	{
		super.__onGameDialogChangeWorldBuyInButtonClicked(event);
	}

	_onServerErrorMessage(event)
	{
		if (GameWebSocketInteractionController.isFatalError(event.errorType))
		{
			this.__deactivateDialog();
		}
	}

	__activateDialog()
	{
		const isCafMode = APP.isCAFMode; 
		const isKicked = APP.isKicked;
		if (
				!isCafMode
				|| isKicked
				
			)
		{
			return;
		}

		console.log('activate summary dialog super ')

		super.__activateDialog();
	}
}

export default BattlegroundCAFRoundSummaryDialogController