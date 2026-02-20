import SimpleUIController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import BattlegroundGameInfo from '../../../model/uis/battleground/BattlegroundGameInfo';
import {GAME_MESSAGES, LOBBY_MESSAGES} from '../../../external/GameExternalCommunicator';
import GameExternalCommunicator from '../../../external/GameExternalCommunicator';
import GameWebSocketInteractionController from '../../interaction/server/GameWebSocketInteractionController';
import GameScreen from '../../../main/GameScreen';
import GameStateController from '../../state/GameStateController';
import { GameStateInfo, SUBROUND_STATE, ROUND_STATE } from '../../../model/state/GameStateInfo';
import GameCafRoomManagerController from './GameCafRoomManagerController';
import { SERVER_MESSAGES } from '../../../model/interaction/server/GameWebSocketInteractionInfo';

class BattlegroundGameController extends SimpleUIController
{
	static get EVENT_BATTLEGROUND_WAITING_TIME_IS_OUT()	 						{ return 'EVENT_BATTLEGROUND_WAITING_TIME_IS_OUT'; }

	get cafRoomManagerController()
	{
		return this._fCafRoomManagerController_gcrmc;
	}

	constructor()
	{
		super (new BattlegroundGameInfo());

		this._fCafRoomManagerController_gcrmc = null;
	}

	__init()
	{
		super.__init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);
		APP.currentWindow.on(GameScreen.EVENT_ON_SERVER_MESSAGE_GAME_STATE_CHANGED, this._onServerGameStateChangedMessage, this);

		APP.currentWindow.on(GameScreen.EVENT_BATTLEGROUND_TIME_TO_START_UPDATED, this._onBattlegroundTimeToStartUpdated, this, true);

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND, this._onCancelBattlegroundRound, this);

		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.NEED_UPDATE_BATTLEGROUND_MODE_INFO_ON_GAME_LEVEL);

		this._fCafRoomManagerController_gcrmc = new GameCafRoomManagerController();
		this._fCafRoomManagerController_gcrmc.init();
	}

	_onServerMessage(event)
	{
		let messageData = event.messageData;
		let messageClass = messageData.class;

		if (messageClass == SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE)
		{
			if (messageData.battlegroundInfo && messageData.battlegroundInfo.buyInConfirmed)
			{
				APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_BUY_IN_STATE_CHANGED, { buyInConfirmed: true });
			}
		}
	}

	_onServerGameStateChangedMessage(aEvent_obj)
	{
		switch (aEvent_obj.state)
		{
			case ROUND_STATE.PLAY:
				this.info.isPlayerClickedConfirmWasResetOnQualifyState = false;
				break;
			case ROUND_STATE.QUALIFY:
				if (!this.info.isPlayerClickedConfirmWasResetOnQualifyState)
				{
					this.info.isPlayerClickedConfirmPlayForNextBattlegroundRound = false;
					this.info.isPlayerClickedConfirmWasResetOnQualifyState = true;
				}
			default:
				break;
		}
	}

	_onLobbyExternalMessageReceived(event)
	{
		let msgType = event.type;
		let l_bgi = this.info;

		switch (msgType)
		{
			case LOBBY_MESSAGES.BATTLEGROUND_PLAY_AGAIN_BUTTON_CLICKED:
				this.info.isNotFirstBattlegroundRoundAfterLoading = false;
				this.info.isPlayerClickedConfirmPlayForNextBattlegroundRound = true;
				l_bgi.setTimeToStart(undefined);
				break;

			case LOBBY_MESSAGES.UPDATE_BATTLEGROUND_MODE:
				if (!this.info.isNotFirstBattlegroundRoundAfterLoading)
				{
					this.info.isNotFirstBattlegroundRoundAfterLoading = true;
					this.info.isPlayerClickedConfirmPlayForNextBattlegroundRound = !event.data.isConfirmBuyinDialogExpectedOnLastHand;
				}
				break;

			case LOBBY_MESSAGES.BATTLEGROUND_CONTINUE_WAITING:
				l_bgi.setTimeToStart(undefined);
				break;

			case LOBBY_MESSAGES.BATTLEGROUND_RE_BUY_CONFIRMED:
				l_bgi.setTimeToStart(undefined);
				break;
			case LOBBY_MESSAGES.UPDATE_BATTLEGROUND_END_TIME:
				l_bgi.setTimeToStart(event.data.endTime);
				break;
		}
	}

	_onBattlegroundTimeToStartUpdated(event)
	{
		this.info.setTimeToStart(event.timeToStart);
	}

	getFormattedTimeToStart(aOptIsHHRequired_bl)
	{
		let lMillisCount_int = this.info.getTimeToStartInMillis();

		if(lMillisCount_int <= 0)
		{
			this.emit(BattlegroundGameController.EVENT_BATTLEGROUND_WAITING_TIME_IS_OUT);
			return aOptIsHHRequired_bl ? "--:--:--" : "--:--"
		}

		return this.info.getFormattedTimeToStart(aOptIsHHRequired_bl);
	}

	_onCancelBattlegroundRound()
	{
		this.info.isPlayerClickedConfirmPlayForNextBattlegroundRound = false;
	}

	destroy()
	{
		super.destroy();
	}
}

export default BattlegroundGameController;