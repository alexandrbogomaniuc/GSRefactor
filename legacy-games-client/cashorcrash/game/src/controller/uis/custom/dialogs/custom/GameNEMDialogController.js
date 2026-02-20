import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../../../../interaction/server/GameWebSocketInteractionController';
import BetsController from '../../../../gameplay/bets/BetsController';
import {BET_PLACE_REJECT_REASONS} from '../../../../gameplay/bets/BaseBetsController';
import { SERVER_MESSAGES } from '../../../../../model/interaction/server/GameWebSocketInteractionInfo';

class GameNEMDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);
		
		this._initGameNEMDialogController();
	}

	_initGameNEMDialogController()
	{
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gameNEMDialogView;
	}

	__initModelLevel ()
	{
		super.__initModelLevel();

		let modeValue = APP.urlBasedParams.MODE || APP.urlBasedParams.mode || "free";
		this.info.isRealMoneyMode = modeValue.toLowerCase() === "real";
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		this._startHandleEnvironmentMessages();
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	//DEBUG...
	get debugMessages()
	{
		return ["TADialogMessageNEMNotRealBetWinMode", "TADialogMessageCashier", "TADialogMessageLowBalanceForReal"];
	}
	//...DEBUG

	__validateViewLevel ()
	{
		var info = this.info;
		var view = this.__fView_uo;
		var messageAssetId;

		if (info.isActive)
		{
			var info = this.info;
			var view = this.__fView_uo;
			var messageAssetId;

			//buttons configuration...
			if (APP.appParamsInfo.buyInFuncDefined)
			{
				view.setOkCancelMode();
				view.okButton.setBuyInCaption();
			}
			else
			{
				view.setOkMode();
				view.okButton.setOKCaption();
			}
			//...buttons configuration

			//message configuration...
			if (this.info.isTechnicalBuyInState)
			{
				messageAssetId = "TADialogMessageNEMNotRealBetWinMode";
			}
			else if (APP.appParamsInfo.buyInFuncDefined)
			{
				messageAssetId = "TADialogMessageCashier";
			}
			else
			{
				messageAssetId = "TADialogMessageLowBalanceForReal";
			}

			//DEBUG...
			if (this.curDebugMessage !== undefined)
			{
				messageAssetId = this.curDebugMessage;
			}
			//...DEBUG

			view.setMessage(messageAssetId);
			//...message configuration
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog()
	{
		super.__deactivateDialog();
	}

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);

		this.__deactivateDialog();
	}

	_startHandleEnvironmentMessages()
	{
		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onGameServerConnectionOpened, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);

		let lBetsController_bsc = this._fBetsController_bsc = APP.gameController.gameplayController.gamePlayersController.betsController;
		lBetsController_bsc.on(BetsController.EVENT_ON_BET_REJECTED, this._onBetRejected, this);
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		switch(data.class)
		{
			case SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_REJECTED_RESPONSE:
				if (
						data.errorCode == GameWebSocketInteractionController.ERROR_CODES.NOT_ENOUGH_MONEY
						|| data.errorCode == GameWebSocketInteractionController.ERROR_CODES.INSUFFICIENT_FUNDS
					)
				{
					this.info.isTechnicalBuyInState = true;
					this.__activateDialog();
				}
				break;
		}
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let requestClass = undefined;
		if (requestData && requestData.rid >= 0)
		{
			requestClass = requestData.class;
		}
		
		switch (serverData.code) 
		{
			case GameWebSocketInteractionController.ERROR_CODES.NOT_ENOUGH_MONEY:
			case GameWebSocketInteractionController.ERROR_CODES.INSUFFICIENT_FUNDS:
				this.info.isTechnicalBuyInState = false;
				this.__activateDialog();
				break;
		}
	}

	_onBetRejected(event)
	{
		let lRejectReason_str = event.rejectReason;
		if (lRejectReason_str == BET_PLACE_REJECT_REASONS.NOT_ENOUGH_MONEY)
		{
			this.info.isTechnicalBuyInState = false;
			this.__activateDialog();
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
}

export default GameNEMDialogController