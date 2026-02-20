import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../../interaction/server/GameWebSocketInteractionController';
import PlayerInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import GUSGameExternalCommunicator, { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSGameExternalCommunicator';
import GameScreen from '../../../main/GameScreen';
import GamePlayerController from '../../custom/GamePlayerController';
import GameField from '../../../main/GameField';
import { CLIENT_MESSAGES } from '../../../model/interaction/server/GameWebSocketInteractionInfo';
import GameStateController from '../../state/GameStateController';

class GameCafRoomManagerController extends SimpleController
{
	static get EVENT_ON_BATTLEGROUND_START_PRIVATE_ROOM ()				{ return "EVENT_ON_BATTLEGROUND_START_PRIVATE_ROOM" };
	static get EVENT_ON_BATTLEGROUND_PLAYER_KICK_TRIGGERED () 			{ return "EVENT_ON_BATTLEGROUND_PLAYER_KICK_TRIGGERED" };
	static get EVENT_ON_BATTLEGROUND_PLAYER_REINVITE_TRIGGERED () 			{ return "EVENT_ON_BATTLEGROUND_PLAYER_REINVITE_TRIGGERED" };
	static get EVENT_ROOM_MANAGER_ROUND_START_MODE_ACTIVATED ()			{ return "EVENT_ROOM_MANAGER_ROUND_START_MODE_ACTIVATED" };
	static get EVENT_ROOM_MANAGER_ROUND_START_MODE_DEACTIVATED ()		{ return "EVENT_ROOM_MANAGER_ROUND_START_MODE_DEACTIVATED" };
	static get EVENT_ON_BATTLEGROUND_PLAYER_INVITE_TRIGGERED()			{return "EVENT_ON_BATTLEGROUND_PLAYER_INVITE_TRIGGERED"};

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initGameCafRoomManagerController();
	}

	_initGameCafRoomManagerController()
	{
	}

	__init ()
	{
		super.__init();
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
		
		let lPlayerInfo_pi = APP.playerController.info;
		if (lPlayerInfo_pi.isCAFRoomManagerDefined)
		{
			if (lPlayerInfo_pi.isCAFRoomManager)
			{
				this._startRoomManagerHandling();
			}
		}
		else
		{
			APP.playerController.on(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		}
	}

	_onPlayerInfoUpdated()
	{
		let lPlayerInfo_pi = APP.playerController.info;
		if (lPlayerInfo_pi.isCAFRoomManagerDefined)
		{
			APP.playerController.off(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);

			if (lPlayerInfo_pi.isCAFRoomManager)
			{
				this._startRoomManagerHandling();
			}
		}
	}

	_startRoomManagerHandling()
	{
		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND, this._onCancelBattlegroundRound, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onGameServerConnectionOpened, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSGameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyMessageReceived, this);

		if (APP.gameScreen.gameField && APP.gameScreen.gameField.screen)
		{
			this._startRoomManagerScreenMessagesHandling();
		}
		else
		{
			APP.gameScreen.gameField.once(GameField.EVENT_ON_GAME_FIELD_SCREEN_CREATED, this._onGameFieldScreenCreated, this);
		}
	}

	_onGameFieldScreenCreated(event)
	{
		this._startRoomManagerScreenMessagesHandling();
	}

	_startRoomManagerScreenMessagesHandling()
	{
		APP.gameScreen.on(GameScreen.EVENT_BATTLEGROUND_TIME_TO_START_UPDATED, this._onTimeToStartUpdated, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_SIT_IN_COUNT_DOWN_REQUIRED, this._onTimeToStartUpdated, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_BATTLEGROUND_ROUND_CANCELED, this._onCancelBattlegroundRound, this);
		APP.gameScreen.on(GameScreen.EVENT_BATTLEGROUND_REQUEST_TIME_TO_START, this._onTimeToStartUpdated, this);

		APP.gameScreen.gameField.battlegroundFinalCountingController.on(GameScreen.EVENT_ON_BATTLEGROUND_FINAL_COUNTING_STARTED, this._onFinalCounting, this);

		this._fGameStateController_gsc = APP.gameScreen.gameStateController;
		this._fGameStateController_gsc.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);
		
		this._validateRoundPlayState();
	}

	_onGameStateChanged(event)
	{
		this._validateRoundPlayState();
	}

	_validateRoundPlayState()
	{
		let l_pi = APP.playerController.info;

		if (
				this._fGameStateController_gsc.info.isPlayState
				&& l_pi.isMasterServerSeatIdDefined
				&& l_pi.masterServerSeatId >= 0
			)
		{
			this.__deactivateRoomManagerRoundStartMode();
		}
	}

	_onLobbyMessageReceived(event)
	{
		switch (event.type)
		{
			case LOBBY_MESSAGES.BATTLEGROUND_RE_BUY_CONFIRMED:
				this.__activateRoomManagerRoundStartMode();
				break;

			case LOBBY_MESSAGES.BACK_TO_LOBBY:
				this.__deactivateRoomManagerRoundStartMode();
				break;

			case LOBBY_MESSAGES.BATTLEGROUND_MANAGER_START_ROUND_CLICKED:
				this.emit(GameCafRoomManagerController.EVENT_ON_BATTLEGROUND_START_PRIVATE_ROOM);
				break;

			case LOBBY_MESSAGES.BATTLEGROUND_CANCEL_READY_CLICKED:
				this.__deactivateRoomManagerRoundStartMode(true);
				break;
			case LOBBY_MESSAGES.BATTLEGROUND_CAF_PLAYER_KICK_TRIGGERED:
				this.emit(GameCafRoomManagerController.EVENT_ON_BATTLEGROUND_PLAYER_KICK_TRIGGERED, {nickname: event.data.nickname});
				break;
			case LOBBY_MESSAGES.BATTLEGROUND_CAF_PLAYER_REINVITE_TRIGGERED:
				this.emit(GameCafRoomManagerController.EVENT_ON_BATTLEGROUND_PLAYER_REINVITE_TRIGGERED, {nickname: event.data.nickname});
				break;

			case LOBBY_MESSAGES.BATTLEGROUND_CAF_PLAYER_INVITE_TRIGGERED:
				console.log('invite inspect');
				this.emit(GameCafRoomManagerController.EVENT_ON_BATTLEGROUND_PLAYER_INVITE_TRIGGERED, {nicknames: event.data.nicknames});
				break;
			
		}
	}

	_onTimeToStartUpdated(event)
	{
		let l_pi = APP.playerController.info;

		if(
			!event.isPlayerClickedConfirmPlayForNextBattlegroundRound
			|| event.isRoundResultDisplayInProgress
			|| (this._fGameStateController_gsc.info.isPlayState && l_pi.isMasterServerSeatIdDefined && l_pi.masterServerSeatId >= 0)
		)
		{
			this.__deactivateRoomManagerRoundStartMode();
			return;
		}

		this.__activateRoomManagerRoundStartMode();
	}

	_onCancelBattlegroundRound()
	{
		this.__deactivateRoomManagerRoundStartMode(true);
	}

	__deactivateRoomManagerRoundStartMode(aIsReasonForDeactivateDialog)
	{
		this.emit(GameCafRoomManagerController.EVENT_ROOM_MANAGER_ROUND_START_MODE_DEACTIVATED, {isReasonForDeactivateDialog: aIsReasonForDeactivateDialog});
	}

	__activateRoomManagerRoundStartMode()
	{
		this.emit(GameCafRoomManagerController.EVENT_ROOM_MANAGER_ROUND_START_MODE_ACTIVATED);
	}

	_onFinalCounting()
	{
		this.__deactivateRoomManagerRoundStartMode();
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;

		switch (serverData.code)
		{
			case GameWebSocketInteractionController.ERROR_CODES.INSUFFICIENT_FUNDS:
			case GameWebSocketInteractionController.ERROR_CODES.NOT_ENOUGH_MONEY:
			case GameWebSocketInteractionController.ERROR_CODES.NOT_ALLOWED_START_ROUND:
				this.__deactivateRoomManagerRoundStartMode();
				APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_BUY_IN_REOPENING_REQUIRED);
				break;

			case GameWebSocketInteractionController.ERROR_CODES.SW_PURCHASE_LIMIT_EXCEEDED:
				this.__deactivateRoomManagerRoundStartMode();
				break;
		}
	}

	_onGameServerConnectionOpened(event)
	{
		this.__deactivateRoomManagerRoundStartMode();
	}

	_onGameServerConnectionClosed(event)
	{
		this.__deactivateRoomManagerRoundStartMode();
	}
}

export default GameCafRoomManagerController