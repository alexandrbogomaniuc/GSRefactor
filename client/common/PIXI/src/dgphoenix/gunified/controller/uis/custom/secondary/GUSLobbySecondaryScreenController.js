import SimpleUIController from '../../../../../unified/controller/uis/base/SimpleUIController';
import GUSLobbySecondaryScreenView from '../../../../view/uis/secondary/GUSLobbySecondaryScreenView';
import GUSLobbySettingsScreenController from './settings/GUSLobbySettingsScreenController';
import GUSLobbyPaytableScreenController from './paytable/GUSLobbyPaytableScreenController';
import GUSLobbyEditProfileScreenController from './edit_profile/GUSLobbyEditProfileScreenController';
import { APP } from '../../../../../unified/controller/main/globals';
import GUSLobbyExternalCommunicator, { GAME_MESSAGES } from '../../../external/GUSLobbyExternalCommunicator';
import GUSLobbyCommonPanelController from '../commonpanel/GUSLobbyCommonPanelController';
import GUSLobbyFRBController from '../../../custom/frb/GUSLobbyFRBController';
import GUSLobbyTournamentModeController from '../../../custom/tournament/GUSLobbyTournamentModeController';
import GUSLobbyApplication from '../../../main/GUSLobbyApplication';
import GUSLobbyWebSocketInteractionController from '../../../interaction/server/GUSLobbyWebSocketInteractionController';
import GUSLobbyStateInfo from '../../../../model/state/GUSLobbyStateInfo';
import GUSGameBattlegroundContinueReadingDialogController from '../dialogs/custom/game/GUSGameBattlegroundContinueReadingDialogController';

class GUSLobbySecondaryScreenController extends SimpleUIController
{
	static get EVENT_SCREEN_ACTIVATED()		{ return "onSecondaryScreenActivated"; }
	static get EVENT_SCREEN_SHOWED()		{ return "onSecondaryScreenShowed"; }
	static get EVENT_SCREEN_DEACTIVATED()	{ return "onSecondaryScreenDeactivated"; }

	init(aViewParentContainer_sprt)
	{
		this._fViewParentContainer_sprt = aViewParentContainer_sprt;

		super.init();
	}

	get isSecondaryScreenActive()
	{
		return this.view && this.view.visible;
	}

	initView()
	{
		this._initView(this.__provideSecondaryScreenViewInstance());
	}

	__provideSecondaryScreenViewInstance()
	{
		return new GUSLobbySecondaryScreenView(this._fViewParentContainer_sprt);
	}

	get settingsScreenController()
	{
		return this._settingsScreenController;
	}

	get paytableScreenController()
	{
		return this._paytableScreenController;
	}

	get editProfileScreenController()
	{
		return this._editProfileScreenController;
	}

	//INIT...
	constructor(...args)
	{
		super(...args);

		this._fViewParentContainer_sprt = null;
		this._fSettingsScreenController_ssc = null;
		this._fPaytableScreenController_psc = null;
		this._fEditProfileScreenController_epsc = null;

		this._fCurrentScreenController_ssc = null;
		this._fTournamentModeInfo_tmi = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._settingsScreenController.init();
		this._paytableScreenController.init();
		this._editProfileScreenController.init();

		this._addEventListeners();
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_ssv = this.view;

		this._settingsScreenController.initView(lView_ssv.settingsScreenView);
		this._settingsScreenController.hideScreen();

		this._paytableScreenController.hideScreen();
		this._editProfileScreenController.initView(lView_ssv.editProfileScreenView);
		this._editProfileScreenController.hideScreen();
	}
	//...INIT

	_addEventListeners()
	{
		this._settingsScreenController.on(GUSLobbySettingsScreenController.EVENT_ON_OK_BUTTON_CLICKED, this._onSettingsScreenOkClicked, this);
		this._editProfileScreenController.on(GUSLobbyEditProfileScreenController.EVENT_ON_CANCEL_BUTTON_CLICKED, this._onEditProfileScreenCancelClicked, this);
		this._editProfileScreenController.on(GUSLobbyEditProfileScreenController.EVENT_ON_CLOSE_SCREEN, this._onEditProfileClose, this);

		APP.on(GUSLobbyApplication.EVENT_ON_SETTINGS_BUTTON_CLICKED, this._onSettingsButtonClicked, this);
		APP.on(GUSLobbyApplication.EVENT_ON_INFO_BUTTON_CLICKED, this._onInfoButtonClicked, this);
		APP.on(GUSLobbyApplication.EVENT_ON_PROFILE_BUTTON_CLICKED, this._onProfileButtonClicked, this);
		APP.on(GUSLobbyApplication.EVENT_ON_SOME_BONUS_STATE_CHANGED, this._onSomeBonusStateChanged, this);

		this._settingsScreenController.on(GUSLobbySettingsScreenController.EVENT_ON_CLOSE_BTN_CLICKED, this._onCloseButtonClicked, this);
		this._settingsScreenController.on(GUSLobbySettingsScreenController.EVENT_SCREEN_DEACTIVATED, this._hideSettingsScreenAccordingToRoundStart, this);

		this._paytableScreenController.on(GUSLobbySettingsScreenController.EVENT_ON_CLOSE_BTN_CLICKED, this._onCloseButtonClicked, this);

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onLobbyServerConnectionClosed, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		APP.externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameExternalMessageReceived.bind(this));

		APP.commonPanelController.on(GUSLobbyCommonPanelController.EVENT_BACK_TO_LOBBY_BUTTON_CLICKED, this._onCommonPanelBackToLobbyClicked, this);
		APP.commonPanelController.on(GUSLobbyCommonPanelController.EVENT_FIRE_SETTINGS_BUTTON_CLICKED, this._onCommonPanelFireSettingsClicked, this);

		APP.FRBController.on(GUSLobbyFRBController.EVENT_FRB_RESTART_REQUIRED, this._onFRBRestartRequired, this);

		let tournamentModeController = APP.tournamentModeController;
		this._fTournamentModeInfo_tmi = tournamentModeController.info;
		tournamentModeController.on(GUSLobbyTournamentModeController.EVENT_ON_TOURNAMENT_STATE_CHANGED, this._onTournamentModeStateChanged, this);

		if (APP.isBattlegroundGame)
		{
			APP.dialogsController.gameBattlegroundContinueReadingDialogController.on(
				GUSGameBattlegroundContinueReadingDialogController.EVENT_BATTLEGROUND_CONTINUE_READING_INFO_CLICKED, this._hideSceeen, this);
		}
	}

	_onFRBRestartRequired()
	{
		this._hideSceeen();
	}

	_onCommonPanelBackToLobbyClicked()
	{
		this._hideSceeen();
	}

	_onCommonPanelFireSettingsClicked()
	{
		this._hideSceeen();
	}

	_onSettingsScreenOkClicked()
	{
		this._hideSceeen();
	}

	_onEditProfileScreenCancelClicked()
	{
		this._hideSceeen();
	}

	_onEditProfileClose()
	{
		this._hideSceeen();
	}

	_onSettingsButtonClicked()
	{
		this._showScreen(this._settingsScreenController);
	}

	_onInfoButtonClicked()
	{
		this._showScreen(this._paytableScreenController);
	}

	_onProfileButtonClicked()
	{
		this._showScreen(this._editProfileScreenController);
	}

	_onCloseButtonClicked()
	{
		this._hideSceeen(true);
	}

	_onLobbyServerConnectionClosed(event)
	{
		if (event.wasClean)
		{
			return;
		}

		this._hideSceeen(true);
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;

		switch (serverData.code)
		{
			case GUSLobbyWebSocketInteractionController.ERROR_CODES.SERVER_SHUTDOWN:
				this._hideSceeen(true);
				break;
		}
	}

	_onSomeBonusStateChanged()
	{
		this._hideSceeen();
	}

	_onGameExternalMessageReceived(event)
	{
		let msgType = event.type;
		switch (msgType)
		{
			case GAME_MESSAGES.SERVER_CONNECTION_CLOSED:
				this._hideSceeen(true);
				break;
			case GAME_MESSAGES.BATTLEGROUND_ROUND_CANCELED:
				if(!event.data.isCountDownCanceled)
					this._hideSceeen(true);
				break;
			case GAME_MESSAGES.ROUND_RESULT_ACTIVATED:
				this._hideSceeen(true);
				break;
		}
	}

	//MENU BUTTONS...
	_onMenuInfoClicked()
	{
		this._showScreen(this._paytableScreenController);
	}

	_onMenuProfileClicked()
	{
		this._showScreen(this._editProfileScreenController);
	}

	_onMenuSettingsClicked()
	{
		this._showScreen(this._settingsScreenController);
	}

	_onMenuCloseClicked()
	{
		this._hideSceeen(true);
	}

	_onTournamentModeStateChanged()
	{
		let lTournamentModeInfo_tmi = this._fTournamentModeInfo_tmi;

		if (lTournamentModeInfo_tmi.isTournamentCompletedOrFailedState)
		{
			this._hideSceeen(true);
		}
	}
	//...MENU BUTTONS

	_showScreen(aScreenController_ssc)
	{
		if (!aScreenController_ssc || this._fCurrentScreenController_ssc === aScreenController_ssc)
		{
			return;
		}

		this._hideCurrentScreen();
		aScreenController_ssc.showScreen();
		this._fCurrentScreenController_ssc = aScreenController_ssc;

		this.emit(GUSLobbySecondaryScreenController.EVENT_SCREEN_SHOWED, { screenId: this._getCurrentScreenId() });

		let lView_ssv = this.view;
		if (lView_ssv)
		{
			if (!lView_ssv.visible)
			{
				lView_ssv.visible = true;
				this.emit(GUSLobbySecondaryScreenController.EVENT_SCREEN_ACTIVATED);
			}
		}
	}

	_hideSceeen(aOptCancelChangesOnScreen_bl)
	{
		this._hideCurrentScreen(aOptCancelChangesOnScreen_bl);

		let lView_ssv = this.view;
		if (lView_ssv && lView_ssv.visible)
		{
			lView_ssv.visible = false;
			this.emit(GUSLobbySecondaryScreenController.EVENT_SCREEN_DEACTIVATED);
		}
	}

	_hideCurrentScreen(aOptCancelChangesOnScreen_bl = false)
	{
		if (this._fCurrentScreenController_ssc)
		{
			if (aOptCancelChangesOnScreen_bl && this._fCurrentScreenController_ssc.cancelChanges)
			{
				this._fCurrentScreenController_ssc.cancelChanges();
			}
			this._fCurrentScreenController_ssc.hideScreen();
			this._fCurrentScreenController_ssc = null;
		}
	}

	_hideSettingsScreenAccordingToRoundStart(event)
	{
		let lView_ssv = this.view;
		if (lView_ssv && lView_ssv.visible)
		{
			lView_ssv.visible = false;
			this.emit(GUSLobbySecondaryScreenController.EVENT_SCREEN_DEACTIVATED, event);
			this._fCurrentScreenController_ssc = null;
		}
	}

	hideAllScreenButtons()
	{
		this.view.hideAllScreenButtons();
	}

	_getCurrentScreenId()
	{
		let lId_str = GUSLobbyStateInfo.SCREEN_NONE;

		if (this._fCurrentScreenController_ssc instanceof GUSLobbySettingsScreenController) lId_str = GUSLobbyStateInfo.SCREEN_SETTINGS;
		else if (this._fCurrentScreenController_ssc instanceof GUSLobbyPaytableScreenController) lId_str = GUSLobbyStateInfo.SCREEN_PAYTABLE;
		else if (this._fCurrentScreenController_ssc instanceof GUSLobbyEditProfileScreenController) lId_str = GUSLobbyStateInfo.SCREEN_PROFILE;

		return lId_str;
	}

	//SCREENS...
	get _settingsScreenController()
	{
		return this._fSettingsScreenController_ssc || (this._fSettingsScreenController_ssc = this.__provideSettingsScreenControllerInstance());
	}

	__provideSettingsScreenControllerInstance()
	{
		return new GUSLobbySettingsScreenController();
	}

	get _paytableScreenController()
	{
		return this._fPaytableScreenController_psc || (this._fPaytableScreenController_psc = this.__providePaytableScreenControllerInstance());
	}

	__providePaytableScreenControllerInstance()
	{
		return new GUSLobbyPaytableScreenController();
	}

	get _editProfileScreenController()
	{
		return this._fEditProfileScreenController_epsc || (this._fEditProfileScreenController_epsc = this.__provideEditProfileScreenControllerInstance());
	}

	__provideEditProfileScreenControllerInstance()
	{
		return new GUSLobbyEditProfileScreenController();
	}
	//...SCREENS

	destroy()
	{
		super.destroy();
	}
}

export default GUSLobbySecondaryScreenController