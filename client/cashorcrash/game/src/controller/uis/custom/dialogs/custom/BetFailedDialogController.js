import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';
import { CLIENT_MESSAGES } from '../../../../../model/interaction/server/GameWebSocketInteractionInfo';
import RoundController from '../../../../gameplay/RoundController';

class BetFailedDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initBetFailedDialogController();
	}

	_initBetFailedDialogController()
	{	
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().betFailedDialogView;
	}

	__initModelLevel()
	{
		super.__initModelLevel();
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onGameServerConnectionOpened, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);

		this._fRoundController_rc = APP.gameController.gameplayController.roundController;
		this._fRoundController_rc.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	//DEBUG...
	get debugMessages()
	{
		return ["TADialogBetFailedByTimePassed", "TADialogBetFailedByNotSeater", "TADialogBidFailedByNotSeater"];
	}
	//...DEBUG

	__validateViewLevel ()
	{
		var info = this.info;
		if (info.isActive)
		{
			var view = this.__fView_uo;

			let lMessageId_str = "TADialogBetFailedByNotSeater";

			if (this.info.isPlaceBetTimePassed)
			{
				lMessageId_str = "TADialogBetFailedByTimePassed";
			}
			else if (APP.isBattlegroundGame || APP.tripleMaxBlastModeController.info.isTripleMaxBlastMode)
			{
				lMessageId_str = "TADialogBidFailedByNotSeater";
			}

			//DEBUG...
			if (this.curDebugMessage !== undefined)
			{
				lMessageId_str = this.curDebugMessage;
			}
			//...DEBUG
			
			view.setMessage(lMessageId_str);
			view.setCancelMode();

			view.cancelButton.setOKCaption();
			
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__activateDialog ()
	{
		super.__activateDialog();
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;

		switch(serverData.code)
		{
			case GameWebSocketInteractionController.ERROR_CODES.NOT_SEATER:
				if(requestData && (requestData.class === CLIENT_MESSAGES.CRASH_BET || requestData.class === CLIENT_MESSAGES.CRASH_BETS))
				{
					this.info.isPlaceBetTimePassed = false;
					this.__activateDialog();
				}
				break;
			case GameWebSocketInteractionController.ERROR_CODES.PREV_OPERATION_IS_NOT_COMPLETE:
				if(serverData.rid !== -1)
				{
					this.info.isPlaceBetTimePassed = false;
					this.__activateDialog();
				}
				break;
			case GameWebSocketInteractionController.ERROR_CODES.BUYIN_NOT_ALLOWED_AT_CURRENT_GAME_STATE:
				if(APP.isBattlegroundGame || APP.gameController.gameplayController.info.gamePlayersInfo.betsInfo.isNoMoreBetsPeriodMode)
				{
					this.info.isPlaceBetTimePassed = !APP.isBattlegroundGame;
					this.__activateDialog();
				}
				break;
		}
	}

	_onGameServerConnectionOpened(event)
	{
		this.__deactivateDialog();
	}

	_onGameServerConnectionClosed(event)
	{
		this.__deactivateDialog();
	}

	_onRoundStateChanged(event)
	{
		let l_ri = this._fRoundController_rc.info;
		if ((this.info.isPlaceBetTimePassed && l_ri.isRoundPlayState) || (APP.isBattlegroundGame && !l_ri.isRoundPlayState))
		{
			this.__deactivateDialog();
		}
	}
}

export default BetFailedDialogController