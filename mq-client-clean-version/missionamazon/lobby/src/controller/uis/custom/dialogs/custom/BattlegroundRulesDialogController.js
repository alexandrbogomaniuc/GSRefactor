import DialogsController from '../DialogsController';
import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../interaction/server/LobbyWebSocketInteractionController';
import {CLIENT_MESSAGES} from '../../../../../model/interaction/server/LobbyWebSocketInteractionInfo';
import LobbyStateController from '../../../../state/LobbyStateController';
import LobbyScreen from '../../../../../main/LobbyScreen';
import ServerGetInteractionController from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/server/ServerGetInteractionController';
import DialogsInfo from '../../../../../model/uis/custom/dialogs/DialogsInfo';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

class BattlegroundRulesDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._fTournamentModeInfo_tni = null;
		
		this._initBattlegroundRulesDialogController();
	}

	_initBattlegroundRulesDialogController()
	{
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().battlegroundRulesDialogView;
	}

	__initModelLevel ()
	{
		super.__initModelLevel();

	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.once(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);

		APP.dialogsController.on(DialogsController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
		APP.dialogsController.on(DialogsController.EVENT_BATTLEGROUND_RULES_SHOW_REQUIRED, this.__activateDialog, this);


		// DEBUG...
		//window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}

	//DEBUG...
	/*keyDownHandler(keyCode)
	{
		if (keyCode.keyCode == 70) //f
		{
			this.__activateDialog();
		}
	}*/
	//...DEBUG

	_onDialogActivated(event)
	{
		if(event.dialogId !== DialogsInfo.DIALOG_ID_BATTLEGROUND_RULES)
		{
			this.__deactivateDialog();
		}
	}

	_onServerEnterLobbyMessage(event)
	{
		//this._startHandleEnvironmentMessages();
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		var info = this.info;
		var view = this.__fView_uo;
		var messageAssetId;

		//buttons configuration...
		view.setOkMode();
		//...buttons configuration

		//message configuration...
		view.setMessage("TABattlegroundGameRulesCaption");
		//...message configuration

		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog()
	{
		super.__deactivateDialog();
		APP.layout.clearHtmlOverlay();
	}

	__activateDialog()
	{
		super.__activateDialog();

		this.view.showHTMLContainer();

		let lBattlegroundRulesURL_str = APP.appParamsInfo.commonPathForActionGames + I18.getCommonTranslatableAssetsRelativePath() + "battleground/html/rules.html?" + APP.appParamsInfo.getCommonAssetsVersion();

		ServerGetInteractionController.sendRequest
			(
				lBattlegroundRulesURL_str,
				this._onRulesHTMLReceived.bind(this),
				this._onulesHTMLReceivedError.bind(this)
			);
	}

	_onRulesHTMLReceived(aRulesHTML_str)
	{
		this.view.setRulesHtml(aRulesHTML_str);
	}

	_onulesHTMLReceivedError()
	{
		
	}


	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);

		this.__deactivateDialog();
	}

	_startHandleEnvironmentMessages()
	{
		/*
		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onLobbyServerConnectionOpened, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onLobbyServerConnectionClosed, this);

		let lLobbyStateController_lsc = APP.lobbyStateController;
		lLobbyStateController_lsc.on(LobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyVisibilityChanged, this);
		*/
	}



	_onServerErrorMessage(event)
	{
		/*
		let serverData = event.messageData;
		let requestData = event.requestData;
		let requestClass = undefined;
		if (requestData && requestData.rid >= 0)
		{
			requestClass = requestData.class;
		}
		
		switch (serverData.code) 
		{
			case LobbyWebSocketInteractionController.ERROR_CODES.NOT_ENOUGH_MONEY:
				if (requestClass == CLIENT_MESSAGES.RE_BUY)
				{
					this.__activateDialog();
				}
				break;
		}
		*/
	}

	_onLobbyVisibilityChanged(event)
	{
		if (event.visible)
		{
		}
		else
		{
			this.__deactivateDialog();
		}
	}
}

export default BattlegroundRulesDialogController