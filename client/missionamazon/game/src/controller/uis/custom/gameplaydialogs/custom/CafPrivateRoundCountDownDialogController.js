import GameplayDialogController from '../GameplayDialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';
import GameExternalCommunicator from '../../../../../external/GameExternalCommunicator';
import BattlegroundGameController from '../../../battleground/BattlegroundGameController';
import { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSGameExternalCommunicator';
import { ROUND_STATE } from '../../../../../model/state/GameStateInfo';
import GameStateController from '../../../../state/GameStateController';
import Game from '../../../../../Game';
import { SERVER_MESSAGES } from '../../../../../model/interaction/server/GameWebSocketInteractionInfo';

class CafPrivateRoundCountDownDialogController extends GameplayDialogController
{
	static get EVENT_ON_SCREEN_ACTIVATED() { return "onCafPrivateCountdownDialogActivated"; }
	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._fIsDialogResetRequired_bl = false;
		this._fIsSecondaryScreenActive_bl = false;

		this._initCafPrivateRoundCountDownDialogController();
	}

	_initCafPrivateRoundCountDownDialogController()
	{
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().cafPrivateRoomCountDownDialogView;
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		if(APP.isCAFMode && APP.playerController.info.isCAFRoomManager)
		{
			return;
		}

		APP.externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyMessageReceived, this);

		this._fGameStateController_gsc = APP.currentWindow.gameStateController;
		this._fGameStateController_gsc.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);

		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onGameServerConnectionOpened, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);
		APP.on(Game.EVENT_ON_ROUND_TIME_UPDATE, this._onRoundTimeUpdate, this );
		this.__activateDialogIfRequired();
	}

	_onRoundTimeUpdate(event)
	{
		this._roundStartTime = event.roundStartTime;
		this._roundEndTime = event.roundEndTime;
	}

	__initViewLevel()
	{
		super.__initViewLevel();
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		let info = this.info;
		let view = this.__fView_uo;

		if (this.info.isActive)
		{
			//buttons configuration...
			view.setCancelMode();

			if(this.isPVPObserver)
			{
				if (this._roundEndTime <= 0 || this._roundEndTime <= APP.currentWindow.currentTime)
					{
						view.updateTimeIndicator("00:00:00");
						view.startBlinkingAnimation();
					}
					else
					{
						view.updateTimeIndicator(this.getFormattedTimeToStart(this._roundEndTime, true));
						view.stopBlinkingAnimation();
					}
			}else{
				let lBattleGroundGameInfo_bgi = APP.gameScreen.battlegroundGameController.info;
				if (lBattleGroundGameInfo_bgi.getTimeToStartInMillis() <= 0)
				{
					view.updateTimeIndicator("00:00:00");
					view.startBlinkingAnimation();
				}
				else
				{
					view.updateTimeIndicator(APP.gameScreen.battlegroundGameController.getFormattedTimeToStart(true));
					view.stopBlinkingAnimation();
				}
			}

			
		}
		else
		{
			view.stopBlinkingAnimation();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	get isCAFObserver()
	{
		let l_gsi = this._fGameStateController_gsc.info;
		return APP.isCAFMode && !l_gsi.isPlayerSitIn && !APP.playerController.info.isCAFRoomManager;
	}

	get isPVPObserver()
	{
		let l_gsi = this._fGameStateController_gsc.info;
		return  !l_gsi.isPlayerSitIn && APP.isNewMatchMakingSupported;
	}


	_onGameStateChanged(event)
	{

		let l_gsi = this._fGameStateController_gsc.info;

		if (this.isCAFObserver && this.info.isActive && l_gsi.isWaitState)
		{
			this.__deactivateDialog();

			if (this._fIsSecondaryScreenActive_bl)
			{
				// should wait for secondary screen deactivation to send GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST
			}
			else
			{
				this._fIsDialogResetRequired_bl = false;
				APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST);
			}
		}else if(this.isPVPObserver && !APP.isCAFMode &&  l_gsi.isWaitState)
		{
			this.__deactivateDialog();
			if (this._fIsSecondaryScreenActive_bl)
				{
					// should wait for secondary screen deactivation to send GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST
				}
				else
				{
					this._fIsDialogResetRequired_bl = false;
					APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST);
				}
		}
		else
		{
			this.__activateDialogIfRequired();
		}
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		switch(data.class)
		{
			case SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE:
				this.__activateDialogIfRequired();
				break;
		}
	}

	_onGameServerConnectionOpened(event)
	{
		this._fIsDialogResetRequired_bl = false;
		this.__deactivateDialog();
	}

	_onGameServerConnectionClosed(event)
	{
		this._fIsDialogResetRequired_bl = false;
		this.__deactivateDialog();
	}

	_onLobbyMessageReceived(event)
	{
		switch (event.type)
		{
			case LOBBY_MESSAGES.SECONDARY_SCREEN_ACTIVATED:
				this._fIsSecondaryScreenActive_bl = true;
				this.__deactivateDialog();
				break;	
			case LOBBY_MESSAGES.SECONDARY_SCREEN_DEACTIVATED:
				this._fIsSecondaryScreenActive_bl = false;
				this.__activateDialogIfRequired();

				if (!this.info.isActive && this._fIsDialogResetRequired_bl)
				{
					this._fIsDialogResetRequired_bl = false;
					APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST);
				}
				break;
		}
	}

	__activateDialogIfRequired()
	{
		let l_gsi = this._fGameStateController_gsc.info;

		if(APP.isCAFMode){
			if (l_gsi.isPlayState && this.isCAFObserver && (!this.info.isActive || !this.info.isPresented) && !APP.playerController.info.isKicked)
			{
				this.__activateDialog();
		
				if (this._fIsSecondaryScreenActive_bl)
				{
					this.__deactivateDialog();
				}
			}
		}else{
			if (l_gsi.isPlayState && this.isPVPObserver && (!this.info.isActive || !this.info.isPresented) && !APP.playerController.info.isKicked)
				{
					this.__activateDialogPVP();
			
					if (this._fIsSecondaryScreenActive_bl)
					{
						this.__deactivateDialog();
					}
				}
		}
		
	}

	__activateDialogPVP()
	{
		if (!this.isPVPObserver || !APP.isBattlegroundGame) return;

		super.__activateDialog();

		this._fIsDialogResetRequired_bl = true;

		APP.off(Game.EVENT_ON_TICK_TIME, this._onTickTime, this); // to prevent multiple listeners
		APP.on(Game.EVENT_ON_TICK_TIME, this._onTickTime, this);
		this.emit(CafPrivateRoundCountDownDialogController.EVENT_ON_SCREEN_ACTIVATED);
	}


	__activateDialog()
	{
		if (!this.isCAFObserver) return;

		super.__activateDialog();

		this._fIsDialogResetRequired_bl = true;

		APP.off(Game.EVENT_ON_TICK_TIME, this._onTickTime, this); // to prevent multiple listeners
		APP.on(Game.EVENT_ON_TICK_TIME, this._onTickTime, this);
	}

	__deactivateDialog()
	{
		APP.off(Game.EVENT_ON_TICK_TIME, this._onTickTime, this);

		this.view && this.view.stopBlinkingAnimation();

		super.__deactivateDialog();
	}

	_onTickTime()
	{
		if (!this.info.isPresented)
		{
			return;
		}

		if (this.view)
		{
			this.__validateViewLevel();
		}
	}

	__onDialogCancelButtonClicked(event)
	{
		super.__onDialogCancelButtonClicked(event);
		
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_BACK_TO_MQB_LOBBY);
	}

	getTimeToStartInMillis(timeToStart)
	{
		if(timeToStart === undefined)
		{
			return undefined;
		}

		let lDelta_num = timeToStart - APP.currentWindow.currentTime;

		if(lDelta_num < 0)
		{
			return 0;
		}

		return lDelta_num;
	}

	getFormattedTimeToStart(timeToStart, aOptIsHHRequired_bl)
	{
		if(timeToStart === undefined || timeToStart === 0 )
		{
			return aOptIsHHRequired_bl ? "--:--:--" : "--:--"
		}

		let lSecondsCount_int = Math.round(this.getTimeToStartInMillis(timeToStart) / 1000);

		let hh = Math.floor(lSecondsCount_int / 60 / 60);
		let mm = Math.floor(lSecondsCount_int / 60 - hh * 60);
		let ss = lSecondsCount_int % 60;

		let ssPrefix_str = ss < 10 ? "0" : "";
		let mmPrefix_str = mm < 10 ? "0" : "";
		let hhPrefix_str = hh < 10 ? "0" : "";

		if (hh == 0 && mm == 0 && ss == 0)
		{
			return null;
		}

		if(aOptIsHHRequired_bl)
		{
			return hhPrefix_str + hh + ":" + mmPrefix_str + mm + ":" + ssPrefix_str + ss;
		}

		return mmPrefix_str + mm + ":" + ssPrefix_str + ss;
	}

}

export default CafPrivateRoundCountDownDialogController