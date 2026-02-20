import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { GAME_MESSAGES } from '../../../external/GameExternalCommunicator';
import { CLIENT_MESSAGES, SERVER_MESSAGES } from '../../../model/interaction/server/GameWebSocketInteractionInfo';
import GamePleaseWaitDialogInfo, { WAIT_MESSAGE_TYPES } from '../../../model/uis/custom/GamePleaseWaitDialogInfo';
import GameWebSocketInteractionController from '../../interaction/server/GameWebSocketInteractionController';
import GameStateController from '../../state/GameStateController';
import RoundResultScreenController from '../roundresult/RoundResultScreenController';

class GamePleaseWaitDialogController extends SimpleController
{
	constructor()
	{
		super(new GamePleaseWaitDialogInfo());
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_GAME_CLIENT_SENT_MESSAGE, this._onClientSentMessage, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		this._fGameStateController_gsc = APP.gameScreen.gameStateController;
		this._fGameStateController_gsc.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);

		this._fRoundResultScreenController_rrsc = APP.gameScreen.gameField.roundResultScreenController;
		this._fRoundResultScreenController_rrsc.on(RoundResultScreenController.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED, this._onRoundResultActivated, this);
		this._fRoundResultScreenController_rrsc.on(RoundResultScreenController.EVENT_ON_ROUND_RESULT_SCREEN_SKIPPED, this._onRoundResultScreenActivationSkipped, this);
	}

	_onClientSentMessage(aData_obj)
	{
		switch (aData_obj.class)
		{
			case CLIENT_MESSAGES.BET_LEVEL:
				this._sendOpenDialogExternalMessage(`${WAIT_MESSAGE_TYPES.BET_LEVEL_CHANGE_REQUEST}:${aData_obj.rid}`, 2000);
				break;
			case CLIENT_MESSAGES.SIT_OUT:
				this._sendOpenDialogExternalMessage(`${WAIT_MESSAGE_TYPES.SIT_OUT_REQUEST}:${aData_obj.rid}`, 500);
				break;
			case CLIENT_MESSAGES.BUY_IN:
				this._sendOpenDialogExternalMessage(`${WAIT_MESSAGE_TYPES.BUY_IN_REQUEST}:${aData_obj.rid}`, 2000);
				break;
			case CLIENT_MESSAGES.RE_BUY:
				this._sendOpenDialogExternalMessage(`${WAIT_MESSAGE_TYPES.RE_BUY_REQUEST}:${aData_obj.rid}`, 2000);
				break;
			case CLIENT_MESSAGES.SHOT:
				this._sendOpenDialogExternalMessage(`${WAIT_MESSAGE_TYPES.SHOT}:${aData_obj.rid}`, 2000);
				break;
			case CLIENT_MESSAGES.BULLET:
				this._sendOpenDialogExternalMessage(`${WAIT_MESSAGE_TYPES.BULLET}:${aData_obj.rid}`, 2000);
				break;
			case CLIENT_MESSAGES.SIT_IN:
				this._sendOpenDialogExternalMessage(`${WAIT_MESSAGE_TYPES.SIT_IN_REQUEST}:${aData_obj.rid}`, 2000);
				break;
		}
	}

	_onServerMessage(aEvent_obj)
	{
		let lServerMessageData_obj = aEvent_obj.messageData;
		let lRequestData_obj = aEvent_obj.requestData;

		switch (lServerMessageData_obj.class)
		{
			case SERVER_MESSAGES.BET_LEVEL_RESPONSE:
				if (lRequestData_obj && lRequestData_obj.rid > 0)
				{
					this._sendCloseDialogExternalMessage(`${WAIT_MESSAGE_TYPES.BET_LEVEL_CHANGE_REQUEST}:${lRequestData_obj.rid}`);
				}
				break;
			case SERVER_MESSAGES.NEW_ENEMY:
				// this._sendCloseDialogExternalMessage(WAIT_MESSAGE_TYPES.FIRST_ENEMY_CREATION);
				break;
			case SERVER_MESSAGES.SIT_OUT_RESPONSE:
				if (lRequestData_obj && lRequestData_obj.rid > 0)
				{
					this._sendCloseDialogExternalMessage(`${WAIT_MESSAGE_TYPES.SIT_OUT_REQUEST}:${lRequestData_obj.rid}`);
				}
				break;
			case SERVER_MESSAGES.OK:
				if (lRequestData_obj && lRequestData_obj.class == CLIENT_MESSAGES.CLOSE_ROOM)
				{
					this._sendCloseDialogExternalMessage(WAIT_MESSAGE_TYPES.STATE_PLAY_END);
					this._sendCloseDialogExternalMessage(WAIT_MESSAGE_TYPES.STATE_QUALIFY_END);
					this._sendCloseDialogExternalMessage(WAIT_MESSAGE_TYPES.STATE_WAIT_END);
				}
				break;
			case SERVER_MESSAGES.ROUND_RESULT:
				this._sendOpenDialogExternalMessage(WAIT_MESSAGE_TYPES.ROUND_RESULT_SCREEN, 10000);

				if (!this._fGameStateController_gsc.info.isQualifyState)
				{
					this._sendOpenDialogExternalMessage(WAIT_MESSAGE_TYPES.STATE_PLAY_END, 10000);
				}
				break;
			case SERVER_MESSAGES.BUY_IN_RESPONSE:
				if (lRequestData_obj && lRequestData_obj.rid > 0)
				{
					this._sendCloseDialogExternalMessage(`${WAIT_MESSAGE_TYPES.BUY_IN_REQUEST}:${lRequestData_obj.rid}`);
				}
				break;
			case SERVER_MESSAGES.RE_BUY_RESPONSE:
				if (lRequestData_obj && lRequestData_obj.rid > 0)
				{
					this._sendCloseDialogExternalMessage(`${WAIT_MESSAGE_TYPES.RE_BUY_REQUEST}:${lRequestData_obj.rid}`);
				}
				break;
			case SERVER_MESSAGES.MISS:
			case SERVER_MESSAGES.HIT:
				if (lRequestData_obj && lRequestData_obj.rid > 0)
				{
					this._sendCloseDialogExternalMessage(`${WAIT_MESSAGE_TYPES.SHOT}:${lRequestData_obj.rid}`);
				}
				break;
			case SERVER_MESSAGES.BULLET_RESPONSE:
				if (lRequestData_obj && lRequestData_obj.rid > 0)
				{
					this._sendCloseDialogExternalMessage(`${WAIT_MESSAGE_TYPES.BULLET}:${lRequestData_obj.rid}`);
				}
				break;
			case SERVER_MESSAGES.SIT_IN_RESPONSE:
				if (lRequestData_obj && lRequestData_obj.rid > 0)
				{
					this._sendCloseDialogExternalMessage(`${WAIT_MESSAGE_TYPES.SIT_IN_REQUEST}:${lRequestData_obj.rid}`);
				}
				break;
		}
	}

	_onServerErrorMessage(event)
	{
		let requestData = event.requestData;
		if (requestData)
		{
			switch (requestData.class)
			{
				case CLIENT_MESSAGES.BET_LEVEL:
					if (requestData.rid > -1)
					{
						this._sendCloseDialogExternalMessage(`${WAIT_MESSAGE_TYPES.BET_LEVEL_CHANGE_REQUEST}:${requestData.rid}`);
					}
					break;
				case CLIENT_MESSAGES.SIT_OUT:
					this._sendCloseDialogExternalMessage(`${WAIT_MESSAGE_TYPES.SIT_OUT_REQUEST}:${requestData.rid}`);
					break;
				case CLIENT_MESSAGES.BUY_IN:
					if (requestData.rid > -1)
					{
						this._sendCloseDialogExternalMessage(`${WAIT_MESSAGE_TYPES.BUY_IN_REQUEST}:${requestData.rid}`);
					}
					break;
				case CLIENT_MESSAGES.RE_BUY:
					if (requestData.rid > -1)
					{
						this._sendCloseDialogExternalMessage(`${WAIT_MESSAGE_TYPES.RE_BUY_REQUEST}:${requestData.rid}`);
					}
					break;
				case CLIENT_MESSAGES.SIT_IN:
					if (requestData.rid > -1)
					{
						this._sendCloseDialogExternalMessage(`${WAIT_MESSAGE_TYPES.SIT_IN_REQUEST}:${requestData.rid}`);
					}
					break;
				case CLIENT_MESSAGES.SHOT:
					if (requestData.rid > -1)
					{
						this._sendCloseDialogExternalMessage(`${WAIT_MESSAGE_TYPES.SHOT}:${requestData.rid}`);
					}
					break;
				case CLIENT_MESSAGES.BULLET:
					if (requestData.rid > -1)
					{
						this._sendCloseDialogExternalMessage(`${WAIT_MESSAGE_TYPES.BULLET}:${requestData.rid}`);
					}
					break;
			}
		}
	}

	_onGameStateChanged(e)
	{
		let l_gsi = this._fGameStateController_gsc.info;

		if (l_gsi.isQualifyState)
		{
			this._sendCloseDialogExternalMessage(WAIT_MESSAGE_TYPES.STATE_PLAY_END);
			
			this._sendOpenDialogExternalMessage(WAIT_MESSAGE_TYPES.STATE_QUALIFY_END, 9000);
		}
		else if (l_gsi.isWaitState)
		{
			this._sendCloseDialogExternalMessage(WAIT_MESSAGE_TYPES.STATE_QUALIFY_END);

			if (!APP.isBattlegroundGame)
			{
				this._sendOpenDialogExternalMessage(WAIT_MESSAGE_TYPES.STATE_WAIT_END, 4000);
			}
		}
		else if (l_gsi.isPlayState)
		{
			this._sendCloseDialogExternalMessage(WAIT_MESSAGE_TYPES.STATE_WAIT_END);

		/*
		// https://jira.dgphoenix.com/browse/DRAG-997?focusedCommentId=243122&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-243122
			this._sendOpenDialogExternalMessage(WAIT_MESSAGE_TYPES.FIRST_ENEMY_CREATION, 7000);
		*/
		}
	}

	_onRoundResultActivated()
	{
		this._sendCloseDialogExternalMessage(WAIT_MESSAGE_TYPES.ROUND_RESULT_SCREEN);
	}

	_onRoundResultScreenActivationSkipped()
	{
		this._sendCloseDialogExternalMessage(WAIT_MESSAGE_TYPES.ROUND_RESULT_SCREEN);
	}

	_sendOpenDialogExternalMessage(aEvent_str, aOptTime_num)
	{
		let lMessage_obj = new Object();
		lMessage_obj[aEvent_str] = aOptTime_num || 0;
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ON_PLEASE_WAIT_DIALOG_APPEAR_REQUIRED, lMessage_obj);
	}

	_sendCloseDialogExternalMessage(aEvent_str)
	{
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ON_PLEASE_WAIT_DIALOG_DISAPPEAR_REQUIRED, {eventType: aEvent_str});
	}
}

export default GamePleaseWaitDialogController;